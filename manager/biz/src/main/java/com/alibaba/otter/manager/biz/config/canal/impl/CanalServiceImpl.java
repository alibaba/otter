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

package com.alibaba.otter.manager.biz.config.canal.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.manager.biz.config.canal.dal.CanalDAO;
import com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO;
import com.alibaba.otter.manager.biz.config.node.impl.NodeServiceImpl;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * @author sarah.lij 2012-7-25 下午04:04:43
 */
public class CanalServiceImpl implements CanalService {

    private static final Logger      logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    private CanalDAO                 canalDao;
    private TransactionTemplate      transactionTemplate;
    private AutoKeeperClusterService autoKeeperClusterService;
    private ArbitrateViewService     arbitrateViewService;

    /**
     * 添加
     */
    public void create(final Canal canal) {
        Assert.assertNotNull(canal);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    CanalDO canalDO = modelToDo(canal);
                    canalDO.setId(0L);
                    if (!canalDao.checkUnique(canalDO)) {
                        String exceptionCause = "exist the same repeat canal in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                    canalDao.insert(canalDO);
                    canal.setId(canalDO.getId());
                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## create canal has an exception!");
                    throw new ManagerException(e);
                }
            }
        });
    }

    /**
     * 删除
     */
    public void remove(final Long canalId) {
        Assert.assertNotNull(canalId);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    Canal canal = findById(canalId);
                    canalDao.delete(canalId);
                    arbitrateViewService.removeCanal(canal.getName()); // 删除canal节点信息
                } catch (Exception e) {
                    logger.error("ERROR ## remove canal(" + canalId + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    /**
     * 修改
     */
    public void modify(final Canal canal) {
        Assert.assertNotNull(canal);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    CanalDO canalDo = modelToDo(canal);
                    if (canalDao.checkUnique(canalDo)) {
                        canalDao.update(canalDo);
                    } else {
                        String exceptionCause = "exist the same repeat canal in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## modify canal(" + canal.getId() + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    public List<Canal> listByIds(Long... identities) {

        List<Canal> canals = new ArrayList<Canal>();
        try {
            List<CanalDO> canalDos = null;
            if (identities.length < 1) {
                canalDos = canalDao.listAll();
                if (canalDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any canal, maybe hasn't create any canal.");
                    return canals;
                }
            } else {
                canalDos = canalDao.listByMultiId(identities);
                if (canalDos.isEmpty()) {
                    String exceptionCause = "couldn't query any canal by canalIds:" + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            canals = doToModel(canalDos);
        } catch (Exception e) {
            logger.error("ERROR ## query channels has an exception!");
            throw new ManagerException(e);
        }

        return canals;
    }

    public List<Canal> listAll() {
        return listByIds();
    }

    public Canal findById(Long canalId) {
        Assert.assertNotNull(canalId);
        List<Canal> canals = listByIds(canalId);
        if (canals.size() != 1) {
            String exceptionCause = "query canalId:" + canalId + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        return canals.get(0);
    }

    public Canal findByName(String name) {
        Assert.assertNotNull(name);
        CanalDO canalDo = canalDao.findByName(name);
        if (canalDo == null) {
            String exceptionCause = "query name:" + name + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        return doToModel(canalDo);
    }

    @Override
    public int getCount(Map condition) {
        return canalDao.getCount(condition);
    }

    @Override
    public List<Canal> listByCondition(Map condition) {
        List<CanalDO> canalDos = canalDao.listByCondition(condition);
        if (canalDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any canal by the condition:" + JsonUtils.marshalToString(condition));
            return new ArrayList<Canal>();
        }

        return doToModel(canalDos);
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param canal
     * @return CanalDO
     */
    private CanalDO modelToDo(Canal canal) {
        CanalDO canalDo = new CanalDO();
        try {
            canalDo.setId(canal.getId());
            canalDo.setName(canal.getName());
            canalDo.setStatus(canal.getStatus());
            canalDo.setDescription(canal.getDesc());
            canalDo.setParameters(canal.getCanalParameter());
            canalDo.setGmtCreate(canal.getGmtCreate());
            canalDo.setGmtModified(canal.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the canal Model to Do has an exception");
            throw new ManagerException(e);
        }
        return canalDo;
    }

    /**
     * 用于DO对象转化为Model对象
     * 
     * @param canalDo
     * @return Canal
     */
    private Canal doToModel(CanalDO canalDo) {
        Canal canal = new Canal();
        try {
            canal.setId(canalDo.getId());
            canal.setName(canalDo.getName());
            canal.setStatus(canalDo.getStatus());
            canal.setDesc(canalDo.getDescription());
            CanalParameter parameter = canalDo.getParameters();
            AutoKeeperCluster zkCluster = autoKeeperClusterService.findAutoKeeperClusterById(parameter.getZkClusterId());
            if (zkCluster != null) {
                parameter.setZkClusters(Arrays.asList(StringUtils.join(zkCluster.getServerList(), ',')));
            }
            canal.setCanalParameter(canalDo.getParameters());
            canal.setGmtCreate(canalDo.getGmtCreate());
            canal.setGmtModified(canalDo.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the canal Do to Model has an exception");
            throw new ManagerException(e);
        }

        return canal;
    }

    private List<Canal> doToModel(List<CanalDO> canalDos) {
        List<Canal> canals = new ArrayList<Canal>();
        for (CanalDO canalDo : canalDos) {
            canals.add(doToModel(canalDo));
        }
        return canals;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setCanalDao(CanalDAO canalDao) {
        this.canalDao = canalDao;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setAutoKeeperClusterService(AutoKeeperClusterService autoKeeperClusterService) {
        this.autoKeeperClusterService = autoKeeperClusterService;
    }

    public void setArbitrateViewService(ArbitrateViewService arbitrateViewService) {
        this.arbitrateViewService = arbitrateViewService;
    }

}
