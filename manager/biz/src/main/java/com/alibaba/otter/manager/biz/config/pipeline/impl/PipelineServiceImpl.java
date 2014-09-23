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

package com.alibaba.otter.manager.biz.config.pipeline.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.config.pipeline.dal.PipelineDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.PipelineNodeRelationDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineNodeRelationDO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineNodeRelationDO.Location;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPairComparable;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * @author simon
 */
public class PipelineServiceImpl implements PipelineService {

    private static final Logger     logger = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private PipelineDAO             pipelineDao;
    private PipelineNodeRelationDAO pipelineNodeRelationDao;
    private DataMediaPairService    dataMediaPairService;
    private NodeService             nodeService;
    private TransactionTemplate     transactionTemplate;
    private ArbitrateManageService  arbitrateManageService;
    private ArbitrateViewService    arbitrateViewService;

    /**
     * 添加
     */
    public void create(final Pipeline pipeline) {
        Assert.assertNotNull(pipeline);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    PipelineDO pipelineDo = modelToDo(pipeline);
                    pipelineDo.setId(0L);

                    if (!pipelineDao.checkUnique(pipelineDo)) {
                        String exceptionCause = "exist the same name pipeline under the channel("
                                                + pipelineDo.getChannelId() + ") in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                    pipelineDao.insert(pipelineDo);

                    List<PipelineNodeRelationDO> pipelineNodeRelationDos = new ArrayList<PipelineNodeRelationDO>();

                    for (Node node : pipeline.getSelectNodes()) {
                        PipelineNodeRelationDO pipelineNodeRelationDo = new PipelineNodeRelationDO();
                        pipelineNodeRelationDo.setPipelineId(pipelineDo.getId());
                        pipelineNodeRelationDo.setNodeId(node.getId());
                        pipelineNodeRelationDo.setLocation(Location.SELECT);
                        pipelineNodeRelationDos.add(pipelineNodeRelationDo);
                    }

                    for (Node node : pipeline.getExtractNodes()) {
                        PipelineNodeRelationDO pipelineNodeRelationDo = new PipelineNodeRelationDO();
                        pipelineNodeRelationDo.setPipelineId(pipelineDo.getId());
                        pipelineNodeRelationDo.setNodeId(node.getId());
                        pipelineNodeRelationDo.setLocation(Location.EXTRACT);
                        pipelineNodeRelationDos.add(pipelineNodeRelationDo);
                    }

                    for (Node node : pipeline.getLoadNodes()) {
                        PipelineNodeRelationDO pipelineNodeRelationDo = new PipelineNodeRelationDO();
                        pipelineNodeRelationDo.setPipelineId(pipelineDo.getId());
                        pipelineNodeRelationDo.setNodeId(node.getId());
                        pipelineNodeRelationDo.setLocation(Location.LOAD);
                        pipelineNodeRelationDos.add(pipelineNodeRelationDo);
                    }

                    pipelineNodeRelationDao.insertBatch(pipelineNodeRelationDos);
                    arbitrateManageService.pipelineEvent().init(pipelineDo.getChannelId(), pipelineDo.getId());
                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## create pipeline has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    /**
     * 修改
     */
    public void modify(Pipeline pipeline) {
        Assert.assertNotNull(pipeline);
        try {

            PipelineDO pipelineDo = modelToDo(pipeline);

            if (!pipelineDao.checkUnique(pipelineDo)) {
                String exceptionCause = "exist the same name pipeline under the channel(" + pipelineDo.getChannelId()
                                        + ") in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }

            pipelineNodeRelationDao.deleteByPipelineId(pipelineDo.getId());

            pipelineDao.update(pipelineDo);

            List<PipelineNodeRelationDO> pipelineNodeRelationDos = new ArrayList<PipelineNodeRelationDO>();

            for (Node node : pipeline.getSelectNodes()) {
                PipelineNodeRelationDO pipelineNodeRelationDo = new PipelineNodeRelationDO();
                pipelineNodeRelationDo.setPipelineId(pipelineDo.getId());
                pipelineNodeRelationDo.setNodeId(node.getId());
                pipelineNodeRelationDo.setLocation(Location.SELECT);
                pipelineNodeRelationDos.add(pipelineNodeRelationDo);
            }

            for (Node node : pipeline.getExtractNodes()) {
                PipelineNodeRelationDO pipelineNodeRelationDo = new PipelineNodeRelationDO();
                pipelineNodeRelationDo.setPipelineId(pipelineDo.getId());
                pipelineNodeRelationDo.setNodeId(node.getId());
                pipelineNodeRelationDo.setLocation(Location.EXTRACT);
                pipelineNodeRelationDos.add(pipelineNodeRelationDo);
            }

            for (Node node : pipeline.getLoadNodes()) {
                PipelineNodeRelationDO pipelineNodeRelationDo = new PipelineNodeRelationDO();
                pipelineNodeRelationDo.setPipelineId(pipelineDo.getId());
                pipelineNodeRelationDo.setNodeId(node.getId());
                pipelineNodeRelationDo.setLocation(Location.LOAD);
                pipelineNodeRelationDos.add(pipelineNodeRelationDo);
            }

            pipelineNodeRelationDao.insertBatch(pipelineNodeRelationDos);
        } catch (Exception e) {
            logger.error("ERROR ## modify the pipeline(" + pipeline.getId() + ") has an exception!");
            throw new ManagerException(e);
        }
    }

    /**
     * 删除
     */
    public void remove(final Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    PipelineDO pipelineDO = pipelineDao.findById(pipelineId);
                    if (pipelineDO != null) {
                        pipelineDao.delete(pipelineId);
                        pipelineNodeRelationDao.deleteByPipelineId(pipelineId);
                        // 删除历史cursor
                        String destination = pipelineDO.getParameters().getDestinationName();
                        short clientId = pipelineDO.getId().shortValue();
                        arbitrateViewService.removeCanal(destination, clientId);
                        arbitrateManageService.pipelineEvent().destory(pipelineDO.getChannelId(), pipelineId);
                    }
                } catch (Exception e) {
                    logger.error("ERROR ## remove the pipeline(" + pipelineId + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });
    }

    public int getCount() {
        return pipelineDao.getCount();
    }

    public int getCount(Map condition) {
        return pipelineDao.getCount(condition);
    }

    /*-------------------------------------查询方法----------------------------------------------*/
    /**
     * 根据pipelineId找到pipeline
     */
    public Pipeline findById(Long pipelineId) {

        Assert.assertNotNull(pipelineId);
        List<Pipeline> pipeline = listByIds(pipelineId);

        if (pipeline.size() != 1) {
            String exceptionCause = "query pipeline by pipelineId:" + pipelineId + " but return " + pipeline.size()
                                    + " pipeline!";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }
        return pipeline.get(0);
    }

    /**
     * 更具channelId找到所属所有pipeline
     */
    public List<Pipeline> listByChannelIds(Long... channelId) {
        Assert.assertNotNull(channelId);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        try {

            List<PipelineDO> pipelineDos = pipelineDao.listByChannelIds(channelId);
            if (pipelineDos.isEmpty()) {
                logger.debug("DEBUG ## query pipeline by channelId:" + channelId + " return null.");
                return pipelines;
            }
            pipelines = doToModel(pipelineDos);
        } catch (Exception e) {
            logger.error("ERROR ## query pipelines by channelIds:" + channelId.toString() + " has an exception!");
            throw new ManagerException(e);
        }
        return pipelines;
    }

    public List<Pipeline> listByChannelIdsWithoutOther(Long... channelIds) {
        Assert.assertNotNull(channelIds);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        try {

            List<PipelineDO> pipelineDos = pipelineDao.listByChannelIds(channelIds);
            if (pipelineDos.isEmpty()) {
                logger.debug("DEBUG ## query pipeline by channelId:" + channelIds + " return null.");
                return pipelines;
            }
            pipelines = doToModelWithoutOther(pipelineDos);
        } catch (Exception e) {
            logger.error("ERROR ## query pipelines by channelIds:" + channelIds.toString() + " has an exception!");
            throw new ManagerException(e);
        }
        return pipelines;
    }

    public List<Pipeline> listByChannelIdsWithoutColumn(Long... channelIds) {
        Assert.assertNotNull(channelIds);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        try {

            List<PipelineDO> pipelineDos = pipelineDao.listByChannelIds(channelIds);
            if (pipelineDos.isEmpty()) {
                logger.debug("DEBUG ## query pipeline by channelId:" + channelIds + " return null.");
                return pipelines;
            }
            pipelines = doToModelWithoutColumn(pipelineDos);
        } catch (Exception e) {
            logger.error("ERROR ## query pipelines by channelIds:" + channelIds.toString() + " has an exception!");
            throw new ManagerException(e);
        }
        return pipelines;
    }

    public List<Pipeline> listByNodeId(Long nodeId) {
        Assert.assertNotNull(nodeId);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        try {
            List<PipelineNodeRelationDO> relations = pipelineNodeRelationDao.listByNodeId(nodeId);
            if (relations.isEmpty()) {
                logger.debug("DEBUG ## query the relation by nodeId:" + nodeId
                             + " return null,maybe hasn't create any relations.");
                return pipelines;
            }

            List<Long> piplineIds = new ArrayList<Long>();
            for (PipelineNodeRelationDO relation : relations) {
                piplineIds.add(relation.getPipelineId());
            }

            List<PipelineDO> pipelineDos = pipelineDao.listByMultiId(piplineIds.toArray(new Long[piplineIds.size()]));
            if (pipelineDos.isEmpty()) {
                String exceptionCause = "query the pipelines by pipelineIds:" + piplineIds.toString() + " return null!";
                logger.error("ERROR ## " + exceptionCause);
                throw new ManagerException(exceptionCause);
            }

            pipelines = doToModel(pipelineDos);
        } catch (Exception e) {
            logger.error("ERROR ## query the pipelines by nodeId:" + nodeId + " has an exception!");
            throw new ManagerException(e);
        }

        return pipelines;
    }

    @Override
    public List<Pipeline> listByCondition(Map condition) {
        List<PipelineDO> pipelineDos = pipelineDao.listByCondition(condition);
        if (pipelineDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any pipelines by the condition:"
                         + JsonUtils.marshalToString(condition));
            return new ArrayList<Pipeline>();
        }
        return doToModel(pipelineDos);
    }

    @Override
    public List<Pipeline> listByIds(Long... identities) {

        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        try {
            List<PipelineDO> pipelineDos = new ArrayList<PipelineDO>();
            if (identities.length < 1) {
                pipelineDos = pipelineDao.listAll();
                if (pipelineDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any pipeline, maybe hasn't create any pipeline.");
                    return pipelines;
                }
            } else {
                pipelineDos = pipelineDao.listByMultiId(identities);
                if (pipelineDos.isEmpty()) {
                    String exceptionCause = "couldn't query any pipeline by pipelineIds:" + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            pipelines = doToModel(pipelineDos);
        } catch (Exception e) {
            logger.error("ERROR ## query pipelines has an exception!");
            throw new ManagerException(e);
        }
        return pipelines;
    }

    @Override
    public List<Pipeline> listAll() {
        return listByIds();
    }

    @Override
    public boolean hasRelation(Long nodeId) {
        List<PipelineNodeRelationDO> relations = pipelineNodeRelationDao.listByNodeId(nodeId);
        if (relations.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public List<Pipeline> listByDestinationWithoutOther(String destination) {
        Assert.assertNotNull(destination);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        try {

            List<PipelineDO> pipelineDos = pipelineDao.listByDestinationCondition(destination);
            if (pipelineDos.isEmpty()) {
                logger.debug("DEBUG ## query pipeline by destination:" + destination + " return null.");
                return pipelines;
            }
            pipelines = doToModelWithoutOther(pipelineDos);
        } catch (Exception e) {
            logger.error("ERROR ## query pipelines by destination:" + destination + " has an exception!");
            throw new ManagerException(e);
        }
        return pipelines;
    }

    /**
     * 用于DO对象转化为Model对象
     * 
     * @param pipelineDO
     * @return Pipeline
     */
    private Pipeline doToModel(PipelineDO pipelineDo) {
        Pipeline pipeline = new Pipeline();
        try {
            pipeline.setId(pipelineDo.getId());
            pipeline.setName(pipelineDo.getName());
            pipeline.setParameters(pipelineDo.getParameters());
            pipeline.setDescription(pipelineDo.getDescription());
            pipeline.setGmtCreate(pipelineDo.getGmtCreate());
            pipeline.setGmtModified(pipelineDo.getGmtModified());
            pipeline.setChannelId(pipelineDo.getChannelId());
            pipeline.getParameters().setMainstemClientId(pipeline.getId().shortValue());

            // 组装DatamediaPair
            List<DataMediaPair> pairs = dataMediaPairService.listByPipelineId(pipelineDo.getId());
            Collections.sort(pairs, new DataMediaPairComparable());
            pipeline.setPairs(pairs);

            // 组装Node
            List<PipelineNodeRelationDO> relations = pipelineNodeRelationDao.listByPipelineIds(pipelineDo.getId());

            List<Long> totalNodeIds = new ArrayList<Long>();

            for (PipelineNodeRelationDO relation : relations) {
                Long nodeId = relation.getNodeId();
                if (!totalNodeIds.contains(nodeId)) {
                    totalNodeIds.add(nodeId);
                }
            }

            List<Node> totalNodes = nodeService.listByIds(totalNodeIds.toArray(new Long[totalNodeIds.size()]));
            List<Node> selectNodes = new ArrayList<Node>();
            List<Node> extractNodes = new ArrayList<Node>();
            List<Node> loadNodes = new ArrayList<Node>();

            for (Node node : totalNodes) {
                for (PipelineNodeRelationDO relation : relations) {
                    if (node.getId().equals(relation.getNodeId())) {
                        if (relation.getLocation().isSelect()) {
                            selectNodes.add(node);
                        } else if (relation.getLocation().isExtract()) {
                            extractNodes.add(node);
                        } else if (relation.getLocation().isLoad()) {
                            loadNodes.add(node);
                        }
                    }
                }
            }

            pipeline.setSelectNodes(selectNodes);
            pipeline.setExtractNodes(extractNodes);
            pipeline.setLoadNodes(loadNodes);

        } catch (Exception e) {
            logger.error("ERROR ## change the pipeline Do to Model has an exception");
            throw new ManagerException(e);
        }

        return pipeline;
    }

    private Pipeline doToModelWithoutColumn(PipelineDO pipelineDo) {
        Pipeline pipeline = new Pipeline();
        try {
            pipeline.setId(pipelineDo.getId());
            pipeline.setName(pipelineDo.getName());
            pipeline.setParameters(pipelineDo.getParameters());
            pipeline.setDescription(pipelineDo.getDescription());
            pipeline.setGmtCreate(pipelineDo.getGmtCreate());
            pipeline.setGmtModified(pipelineDo.getGmtModified());
            pipeline.setChannelId(pipelineDo.getChannelId());
            pipeline.getParameters().setMainstemClientId(pipeline.getId().shortValue());

            // 组装DatamediaPair
            List<DataMediaPair> pairs = dataMediaPairService.listByPipelineIdWithoutColumn(pipelineDo.getId());
            Collections.sort(pairs, new DataMediaPairComparable());
            pipeline.setPairs(pairs);

            // 组装Node
            List<PipelineNodeRelationDO> relations = pipelineNodeRelationDao.listByPipelineIds(pipelineDo.getId());

            List<Long> totalNodeIds = new ArrayList<Long>();

            for (PipelineNodeRelationDO relation : relations) {
                Long nodeId = relation.getNodeId();
                if (!totalNodeIds.contains(nodeId)) {
                    totalNodeIds.add(nodeId);
                }
            }

            List<Node> totalNodes = nodeService.listByIds(totalNodeIds.toArray(new Long[totalNodeIds.size()]));
            List<Node> selectNodes = new ArrayList<Node>();
            List<Node> extractNodes = new ArrayList<Node>();
            List<Node> loadNodes = new ArrayList<Node>();

            for (Node node : totalNodes) {
                for (PipelineNodeRelationDO relation : relations) {
                    if (node.getId().equals(relation.getNodeId())) {
                        if (relation.getLocation().isSelect()) {
                            selectNodes.add(node);
                        } else if (relation.getLocation().isExtract()) {
                            extractNodes.add(node);
                        } else if (relation.getLocation().isLoad()) {
                            loadNodes.add(node);
                        }
                    }
                }
            }

            pipeline.setSelectNodes(selectNodes);
            pipeline.setExtractNodes(extractNodes);
            pipeline.setLoadNodes(loadNodes);

        } catch (Exception e) {
            logger.error("ERROR ## change the pipeline Do to Model has an exception");
            throw new ManagerException(e);
        }

        return pipeline;
    }

    private Pipeline doToModelWithoutOther(PipelineDO pipelineDo) {
        Pipeline pipeline = new Pipeline();
        try {
            pipeline.setId(pipelineDo.getId());
            pipeline.setName(pipelineDo.getName());
            pipeline.setParameters(pipelineDo.getParameters());
            pipeline.setDescription(pipelineDo.getDescription());
            pipeline.setGmtCreate(pipelineDo.getGmtCreate());
            pipeline.setGmtModified(pipelineDo.getGmtModified());
            pipeline.setChannelId(pipelineDo.getChannelId());
            pipeline.getParameters().setMainstemClientId(pipeline.getId().shortValue());

        } catch (Exception e) {
            logger.error("ERROR ## change the pipeline Do to Model has an exception");
            throw new ManagerException(e);
        }

        return pipeline;
    }

    private List<Pipeline> doToModel(List<PipelineDO> pipelineDos) {
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        for (PipelineDO pipelineDo : pipelineDos) {
            pipelines.add(doToModel(pipelineDo));
        }
        return pipelines;
    }

    private List<Pipeline> doToModelWithoutOther(List<PipelineDO> pipelineDos) {
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        for (PipelineDO pipelineDo : pipelineDos) {
            pipelines.add(doToModelWithoutOther(pipelineDo));
        }
        return pipelines;
    }

    private List<Pipeline> doToModelWithoutColumn(List<PipelineDO> pipelineDos) {
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        for (PipelineDO pipelineDo : pipelineDos) {
            pipelines.add(doToModelWithoutColumn(pipelineDo));
        }
        return pipelines;
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param pipeline
     * @return PipelineDO
     */
    private PipelineDO modelToDo(Pipeline pipeline) {
        PipelineDO pipelineDO = new PipelineDO();

        try {
            pipelineDO.setId(pipeline.getId());
            pipelineDO.setName(pipeline.getName());
            pipelineDO.setParameters(pipeline.getParameters());
            pipelineDO.setDescription(pipeline.getDescription());
            pipelineDO.setChannelId(pipeline.getChannelId());
            pipelineDO.setGmtCreate(pipeline.getGmtCreate());
            pipelineDO.setGmtModified(pipeline.getGmtModified());

        } catch (Exception e) {
            logger.error("ERROR ## change the pipeline Model to Do has an exception");
            throw new ManagerException(e);
        }

        return pipelineDO;
    }

    /* ------------------------setter / getter--------------------------- */
    public void setPipelineDao(PipelineDAO pipelineDao) {
        this.pipelineDao = pipelineDao;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPipelineNodeRelationDao(PipelineNodeRelationDAO pipelineNodeRelationDao) {
        this.pipelineNodeRelationDao = pipelineNodeRelationDao;
    }

    public void setDataMediaPairService(DataMediaPairService dataMediaPairService) {
        this.dataMediaPairService = dataMediaPairService;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setArbitrateViewService(ArbitrateViewService arbitrateViewService) {
        this.arbitrateViewService = arbitrateViewService;
    }

}
