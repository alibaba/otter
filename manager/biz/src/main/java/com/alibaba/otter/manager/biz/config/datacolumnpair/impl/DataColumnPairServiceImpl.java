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

package com.alibaba.otter.manager.biz.config.datacolumnpair.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.DataColumnPairDAO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairDO;
import com.alibaba.otter.shared.common.model.config.data.Column;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;

/**
 * 类DataColumnPairServiceImpl.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:11:42
 */
public class DataColumnPairServiceImpl implements DataColumnPairService {

    private static final Logger logger = LoggerFactory.getLogger(DataColumnPairServiceImpl.class);

    private DataColumnPairDAO   dataColumnPairDao;

    public void create(ColumnPair entityObj) {
        Assert.assertNotNull(entityObj);

        try {
            DataColumnPairDO dataColumnPairDo = modelToDo(entityObj);
            dataColumnPairDao.insert(dataColumnPairDo);
        } catch (RepeatConfigureException rcf) {
            throw rcf;
        } catch (Exception e) {
            logger.error("ERROR ## create dataColumnPair has an exception!");
            throw new ManagerException(e);
        }

    }

    public void createBatch(List<ColumnPair> dataColumnPairs) {
        Assert.assertNotNull(dataColumnPairs);

        try {

            List<DataColumnPairDO> dataColumnPairDos = new ArrayList<DataColumnPairDO>();

            for (ColumnPair columnPair : dataColumnPairs) {
                DataColumnPairDO dataColumnPairDo = modelToDo(columnPair);
                dataColumnPairDos.add(dataColumnPairDo);
            }
            dataColumnPairDao.insertBatch(dataColumnPairDos);
        } catch (RepeatConfigureException rcf) {
            throw rcf;
        } catch (Exception e) {
            logger.error("ERROR ## create dataColumnPair has an exception!");
            throw new ManagerException(e);
        }
    }

    public void remove(Long identity) {
        Assert.assertNotNull(identity);
        try {
            dataColumnPairDao.delete(identity);
        } catch (Exception e) {
            logger.error("ERROR ## remove dataColumnPair has an exception!");
            throw new ManagerException(e);
        }
    }

    public void modify(ColumnPair entityObj) {
        Assert.assertNotNull(entityObj);

        try {
            DataColumnPairDO dataColumnPairDo = modelToDo(entityObj);
            dataColumnPairDao.update(dataColumnPairDo);
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## modify dataColumnPair has an exception!");
            throw new ManagerException(e);
        }
    }

    public ColumnPair findById(Long identity) {
        Assert.assertNotNull(identity);
        DataColumnPairDO columePairDo = dataColumnPairDao.findById(identity);
        if (columePairDo == null) {
            return null;
        }
        return doToModel(columePairDo);

    }

    public List<ColumnPair> listByIds(Long... identities) {
        return null;
    }

    public List<ColumnPair> listAll() {
        return null;
    }

    public List<ColumnPair> listByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        List<ColumnPair> dataColumnPairs = new ArrayList<ColumnPair>();
        try {
            List<DataColumnPairDO> dataColumnPairDos = dataColumnPairDao.listByDataMediaPairId(dataMediaPairId);
            if (dataColumnPairDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataColumnPair, maybe hasn't create any dataColumnPair.");
                return dataColumnPairs;
            }
            dataColumnPairs = doToModel(dataColumnPairDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataColumnPair by dataMediaId:" + dataMediaPairId + " has an exception!");
            throw new ManagerException(e);
        }

        return dataColumnPairs;
    }

    public Map<Long, List<ColumnPair>> listByDataMediaPairIds(Long... dataMediaPairIds) {
        Assert.assertNotNull(dataMediaPairIds);
        Map<Long, List<ColumnPair>> dataColumnPairs = new HashMap<Long, List<ColumnPair>>();
        try {
            List<DataColumnPairDO> dataColumnPairDos = dataColumnPairDao.listByDataMediaPairIds(dataMediaPairIds);
            if (dataColumnPairDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataColumnPair, maybe hasn't create any dataColumnPair.");
                return dataColumnPairs;
            }
            for (DataColumnPairDO dataColumnPairDo : dataColumnPairDos) {
                List<ColumnPair> columnPairs = dataColumnPairs.get(dataColumnPairDo.getDataMediaPairId());
                if (columnPairs != null) {
                    if (!columnPairs.contains(doToModel(dataColumnPairDo))) {
                        columnPairs.add(doToModel(dataColumnPairDo));
                    }
                } else {
                    columnPairs = new ArrayList<ColumnPair>();
                    columnPairs.add(doToModel(dataColumnPairDo));
                    dataColumnPairs.put(dataColumnPairDo.getDataMediaPairId(), columnPairs);
                }
            }

        } catch (Exception e) {
            logger.error("ERROR ## query dataColumnPair by dataMediaId:" + dataMediaPairIds + " has an exception!");
            throw new ManagerException(e);
        }

        return dataColumnPairs;
    }

    public void removeByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        try {
            dataColumnPairDao.deleteByDataMediaPairId(dataMediaPairId);
        } catch (Exception e) {
            logger.error("ERROR ## remove dataColumnPair has an exception!");
            throw new ManagerException(e);
        }
    }

    public List<ColumnPair> listByCondition(Map condition) {
        return null;
    }

    public int getCount() {
        return 0;
    }

    public int getCount(Map condition) {
        return 0;
    }

    /*-------------------------------------------------------------*/
    /**
     * 用于DO对象转化为Model对象
     */
    private ColumnPair doToModel(DataColumnPairDO dataColumnPairDo) {

        Column sourceColumn = dataColumnPairDo.getSourceColumnName() == null ? null : new Column(
                                                                                                 dataColumnPairDo.getSourceColumnName());
        Column targetColumn = dataColumnPairDo.getTargetColumnName() == null ? null : new Column(
                                                                                                 dataColumnPairDo.getTargetColumnName());
        ColumnPair columnPair = new ColumnPair(sourceColumn, targetColumn);
        columnPair.setId(dataColumnPairDo.getId());
        columnPair.setDataMediaPairId(dataColumnPairDo.getDataMediaPairId());
        columnPair.setGmtCreate(dataColumnPairDo.getGmtCreate());
        columnPair.setGmtModified(dataColumnPairDo.getGmtModified());

        return columnPair;
    }

    private List<ColumnPair> doToModel(List<DataColumnPairDO> dataColumnPairDos) {

        List<ColumnPair> columnPairs = new ArrayList<ColumnPair>();
        for (DataColumnPairDO dataColumnPairDo : dataColumnPairDos) {
            columnPairs.add(doToModel(dataColumnPairDo));
        }

        return columnPairs;
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param dataColumnPair
     * @return DataMediaPairDO
     */
    private DataColumnPairDO modelToDo(ColumnPair dataColumnPair) {
        DataColumnPairDO dataColumnPairDo = new DataColumnPairDO();
        dataColumnPairDo.setId(dataColumnPair.getId());
        dataColumnPairDo.setSourceColumnName(dataColumnPair.getSourceColumn() == null ? null : dataColumnPair.getSourceColumn().getName());
        dataColumnPairDo.setTargetColumnName(dataColumnPair.getTargetColumn() == null ? null : dataColumnPair.getTargetColumn().getName());
        dataColumnPairDo.setDataMediaPairId(dataColumnPair.getDataMediaPairId());
        dataColumnPairDo.setGmtCreate(dataColumnPair.getGmtCreate());
        dataColumnPairDo.setGmtModified(dataColumnPair.getGmtModified());

        return dataColumnPairDo;
    }

    public DataColumnPairDAO getDataColumnPairDao() {
        return dataColumnPairDao;
    }

    public void setDataColumnPairDao(DataColumnPairDAO dataColumnPairDao) {
        this.dataColumnPairDao = dataColumnPairDao;
    }

}
