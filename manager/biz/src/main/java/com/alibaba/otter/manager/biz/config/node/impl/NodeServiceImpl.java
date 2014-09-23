/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.manager.biz.config.node.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.node.dal.NodeDAO;
import com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.node.NodeParameter;
import com.alibaba.otter.shared.common.model.config.node.NodeStatus;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * @author simon
 */
public class NodeServiceImpl implements NodeService {

    private static final Logger      logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    private NodeDAO                  nodeDao;
    private TransactionTemplate      transactionTemplate;
    private ArbitrateManageService   arbitrateManageService;
    private AutoKeeperClusterService autoKeeperClusterService;

    /**
     * 添加
     */
    public void create(final Node node) {
        Assert.assertNotNull(node);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    NodeDO nodeDo = modelToDo(node);
                    nodeDo.setId(0L);
                    if (!nodeDao.checkUnique(nodeDo)) {
                        String exceptionCause = "exist the same repeat node in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                    nodeDao.insert(nodeDo);

                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## create node has an exception!");
                    throw new ManagerException(e);
                }
            }
        });
    }

    /**
     * 删除
     */
    public void remove(final Long nodeId) {
        Assert.assertNotNull(nodeId);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    nodeDao.delete(nodeId);
                } catch (Exception e) {
                    logger.error("ERROR ## remove node(" + nodeId + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    /**
     * 修改
     */
    public void modify(final Node node) {
        Assert.assertNotNull(node);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    NodeDO nodeDo = modelToDo(node);
                    if (nodeDao.checkUnique(nodeDo)) {
                        nodeDao.update(nodeDo);
                    } else {
                        String exceptionCause = "exist the same repeat node in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## modify node(" + node.getId() + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    /**
     * 查找出所有node
     */
    public List<Node> listAll() {

        return listByIds();
    }

    /**
     * 根据nodeid到找node
     */
    public Node findById(Long nodeId) {
        Assert.assertNotNull(nodeId);
        List<Node> nodes = listByIds(nodeId);
        if (nodes.size() != 1) {
            String exceptionCause = "query nodeId:" + nodeId + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        return nodes.get(0);

    }

    public List<Node> listByIds(Long... identities) {
        List<Node> nodes = new ArrayList<Node>();
        try {
            List<NodeDO> nodeDos = null;
            if (identities.length < 1) {
                nodeDos = nodeDao.listAll();
                if (nodeDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any node, maybe hasn't create any channel.");
                    return nodes;
                }
            } else {
                nodeDos = nodeDao.listByMultiId(identities);
                if (nodeDos.isEmpty()) {
                    String exceptionCause = "couldn't query any node by nodeIds:" + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            // 验证zk的node信息
            List<Long> nodeIds = arbitrateManageService.nodeEvent().liveNodes();
            for (NodeDO nodeDo : nodeDos) {
                if (nodeIds.contains(nodeDo.getId())) {
                    nodeDo.setStatus(NodeStatus.START);
                } else {
                    nodeDo.setStatus(NodeStatus.STOP);
                }
            }

            nodes = doToModel(nodeDos);
        } catch (Exception e) {
            logger.error("ERROR ## query nodes has an exception!");
            throw new ManagerException(e);
        }

        return nodes;
    }

    public int getCount() {
        return nodeDao.getCount();
    }

    public int getCount(Map condition) {
        return nodeDao.getCount(condition);
    }

    public List<Node> listByCondition(Map condition) {
        List<NodeDO> nodeDos = nodeDao.listByCondition(condition);
        if (nodeDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any node by the condition:" + JsonUtils.marshalToString(condition));
            return new ArrayList<Node>();
        }
        // 验证zk的node信息
        List<Long> nodeIds = arbitrateManageService.nodeEvent().liveNodes();
        for (NodeDO nodeDo : nodeDos) {
            if (null != nodeIds && nodeIds.contains(nodeDo.getId())) {
                nodeDo.setStatus(NodeStatus.START);
            } else {
                nodeDo.setStatus(NodeStatus.STOP);
            }
        }
        return doToModel(nodeDos);
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param node
     * @return NodeDO
     */
    private NodeDO modelToDo(Node node) {
        NodeDO nodeDo = new NodeDO();
        try {
            nodeDo.setId(node.getId());
            nodeDo.setIp(node.getIp());
            nodeDo.setName(node.getName());
            nodeDo.setPort(node.getPort());
            nodeDo.setDescription(node.getDescription());
            nodeDo.setStatus(node.getStatus());
            nodeDo.setParameters(node.getParameters());
            nodeDo.setGmtCreate(node.getGmtCreate());
            nodeDo.setGmtModified(node.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the node Model to Do has an exception");
            throw new ManagerException(e);
        }
        return nodeDo;
    }

    /**
     * 用于DO对象转化为Model对象
     * 
     * @param nodeDo
     * @return Node
     */
    private Node doToModel(NodeDO nodeDo) {
        Node node = new Node();
        try {
            node.setId(nodeDo.getId());
            node.setIp(nodeDo.getIp());
            node.setName(nodeDo.getName());
            node.setPort(nodeDo.getPort());
            node.setDescription(nodeDo.getDescription());
            node.setStatus(nodeDo.getStatus());
            // 处理下zk集群
            NodeParameter parameter = nodeDo.getParameters();
            if (parameter.getZkCluster() != null) {
                AutoKeeperCluster zkCluster = autoKeeperClusterService.findAutoKeeperClusterById(parameter.getZkCluster().getId());
                parameter.setZkCluster(zkCluster);
            }

            node.setParameters(parameter);
            node.setGmtCreate(nodeDo.getGmtCreate());
            node.setGmtModified(nodeDo.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the node Do to Model has an exception");
            throw new ManagerException(e);
        }

        return node;
    }

    private List<Node> doToModel(List<NodeDO> nodeDos) {
        List<Node> nodes = new ArrayList<Node>();
        for (NodeDO nodeDo : nodeDos) {
            nodes.add(doToModel(nodeDo));
        }

        return nodes;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setNodeDao(NodeDAO nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setAutoKeeperClusterService(AutoKeeperClusterService autoKeeperClusterService) {
        this.autoKeeperClusterService = autoKeeperClusterService;
    }

}
