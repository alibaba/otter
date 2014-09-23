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

package com.alibaba.otter.manager.biz.config.datamatrix.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.DataMatrixDAO;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO;
import com.alibaba.otter.manager.biz.config.node.impl.NodeServiceImpl;
import com.alibaba.otter.shared.common.model.config.data.DataMatrix;
import com.alibaba.otter.shared.common.utils.JsonUtils;

public class DataMatrixServiceImpl implements DataMatrixService {

    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    private DataMatrixDAO       dataMatrixDao;
    private TransactionTemplate transactionTemplate;

    /**
     * 添加
     */
    public void create(final DataMatrix matrix) {
        Assert.assertNotNull(matrix);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    DataMatrixDO matrixlDO = modelToDo(matrix);
                    matrixlDO.setId(0L);
                    if (!dataMatrixDao.checkUnique(matrixlDO)) {
                        String exceptionCause = "exist the same repeat canal in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                    dataMatrixDao.insert(matrixlDO);
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
    public void remove(final Long matrixId) {
        Assert.assertNotNull(matrixId);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    dataMatrixDao.delete(matrixId);
                } catch (Exception e) {
                    logger.error("ERROR ## remove canal(" + matrixId + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    /**
     * 修改
     */
    public void modify(final DataMatrix matrix) {
        Assert.assertNotNull(matrix);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    DataMatrixDO matrixDo = modelToDo(matrix);
                    if (dataMatrixDao.checkUnique(matrixDo)) {
                        dataMatrixDao.update(matrixDo);
                    } else {
                        String exceptionCause = "exist the same repeat matrix in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## modify canal(" + matrix.getId() + ") has an exception!");
                    throw new ManagerException(e);
                }
            }
        });

    }

    public List<DataMatrix> listByIds(Long... identities) {
        List<DataMatrix> matrixs = new ArrayList<DataMatrix>();
        try {
            List<DataMatrixDO> matrixDos = null;
            if (identities.length < 1) {
                matrixDos = dataMatrixDao.listAll();
                if (matrixDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any canal, maybe hasn't create any canal.");
                    return matrixs;
                }
            } else {
                matrixDos = dataMatrixDao.listByMultiId(identities);
                if (matrixDos.isEmpty()) {
                    String exceptionCause = "couldn't query any canal by matrixIds:" + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            matrixs = doToModel(matrixDos);
        } catch (Exception e) {
            logger.error("ERROR ## query channels has an exception!");
            throw new ManagerException(e);
        }

        return matrixs;
    }

    public List<DataMatrix> listAll() {
        return listByIds();
    }

    public DataMatrix findById(Long matrixId) {
        Assert.assertNotNull(matrixId);
        List<DataMatrix> canals = listByIds(matrixId);
        if (canals.size() != 1) {
            String exceptionCause = "query matrixId:" + matrixId + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        return canals.get(0);
    }

    public DataMatrix findByGroupKey(String groupKey) {
        Assert.assertNotNull(groupKey);
        DataMatrixDO matrixDo = dataMatrixDao.findByGroupKey(groupKey);
        if (matrixDo == null) {
            String exceptionCause = "query name:" + groupKey + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        return doToModel(matrixDo);
    }

    @Override
    public int getCount(Map condition) {
        return dataMatrixDao.getCount(condition);
    }

    @Override
    public List<DataMatrix> listByCondition(Map condition) {
        List<DataMatrixDO> matrixDos = dataMatrixDao.listByCondition(condition);
        if (matrixDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any canal by the condition:" + JsonUtils.marshalToString(condition));
            return new ArrayList<DataMatrix>();
        }

        return doToModel(matrixDos);
    }

    /**
     * 用于Model对象转化为DO对象
     */
    private DataMatrixDO modelToDo(DataMatrix matrix) {
        DataMatrixDO matrixDo = new DataMatrixDO();
        try {
            matrixDo.setId(matrix.getId());
            matrixDo.setGroupKey(matrix.getGroupKey());
            matrixDo.setDescription(matrix.getDescription());
            matrixDo.setMaster(matrix.getMaster());
            matrixDo.setSlave(matrix.getSlave());
            matrixDo.setGmtCreate(matrix.getGmtCreate());
            matrixDo.setGmtModified(matrix.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the matrix Model to Do has an exception");
            throw new ManagerException(e);
        }
        return matrixDo;
    }

    /**
     * 用于DO对象转化为Model对象
     */
    private DataMatrix doToModel(DataMatrixDO matrixDo) {
        DataMatrix matrix = new DataMatrix();
        try {
            matrix.setId(matrixDo.getId());
            matrix.setGroupKey(matrixDo.getGroupKey());
            matrix.setDescription(matrixDo.getDescription());
            matrix.setMaster(matrixDo.getMaster());
            matrix.setSlave(matrixDo.getSlave());
            matrix.setGmtCreate(matrixDo.getGmtCreate());
            matrix.setGmtModified(matrixDo.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the canal Do to Model has an exception");
            throw new ManagerException(e);
        }

        return matrix;
    }

    private List<DataMatrix> doToModel(List<DataMatrixDO> matrixDos) {
        List<DataMatrix> matrixs = new ArrayList<DataMatrix>();
        for (DataMatrixDO matrixDo : matrixDos) {
            matrixs.add(doToModel(matrixDo));
        }
        return matrixs;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setDataMatrixDao(DataMatrixDAO dataMatrixDao) {
        this.dataMatrixDao = dataMatrixDao;
    }

}
