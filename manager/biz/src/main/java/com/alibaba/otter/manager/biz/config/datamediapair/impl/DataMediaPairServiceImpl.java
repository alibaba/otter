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

package com.alibaba.otter.manager.biz.config.datamediapair.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairGroupService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.DataMediaPairDAO;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.ExtensionData;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * @author simon
 */
public class DataMediaPairServiceImpl implements DataMediaPairService {

    private static final Logger        logger = LoggerFactory.getLogger(DataMediaPairServiceImpl.class);

    private DataMediaPairDAO           dataMediaPairDao;

    private DataMediaService           dataMediaService;

    private DataColumnPairService      dataColumnPairService;

    private DataColumnPairGroupService dataColumnPairGroupService;

    /**
     * 添加
     */
    public void create(DataMediaPair dataMediaPair) {
        createAndReturnId(dataMediaPair);
    }

    /**
     * 添加并返回插入的id
     */
    public Long createAndReturnId(DataMediaPair dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);

        try {
            DataMediaPairDO dataMediaPairDo = modelToDo(dataMediaPair);
            dataMediaPairDo.setId(0L);
            if (!dataMediaPairDao.checkUnique(dataMediaPairDo)) {
                String exceptionCause = "exist the same pair in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }

            dataMediaPairDao.insert(dataMediaPairDo);
            return dataMediaPairDo.getId();
        } catch (RepeatConfigureException rcf) {
            throw rcf;
        } catch (Exception e) {
            logger.error("ERROR ## create dataMediaPair has an exception!", e);
            throw new ManagerException(e);
        }

    }

    /**
     * 添加并返回插入的id
     */
    public boolean createIfNotExist(DataMediaPair dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);

        try {
            DataMediaPairDO dataMediaPairDo = modelToDo(dataMediaPair);
            dataMediaPairDo.setId(0L);
            if (!dataMediaPairDao.checkUnique(dataMediaPairDo)) {
                return false;
            }
            dataMediaPairDao.insert(dataMediaPairDo);
            return true;
        } catch (Exception e) {
            logger.error("ERROR ## create dataMediaPair has an exception!", e);
            throw new ManagerException(e);
        }

    }

    /**
     * 删除
     */
    public void remove(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);

        try {
            dataMediaPairDao.delete(dataMediaPairId);
        } catch (Exception e) {
            logger.error("ERROR ## remove dataMediaPair has an exception!", e);
            throw new ManagerException(e);
        }
    }

    /**
     * 修改
     */
    public void modify(DataMediaPair dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);

        try {
            DataMediaPairDO dataMediaPairDo = modelToDo(dataMediaPair);
            if (dataMediaPairDao.checkUnique(dataMediaPairDo)) {
                dataMediaPairDao.update(dataMediaPairDo);
            } else {
                String exceptionCause = "exist the same pair in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## modify dataMediaPair has an exception!", e);
            throw new ManagerException(e);
        }
    }

    /*-----------------------------------查询方法，整合-----------------------------------------*/

    public List<DataMediaPair> listByIds(Long... identities) {
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();
        try {
            List<DataMediaPairDO> dataMediaPairDos = null;
            if (identities.length < 1) {
                dataMediaPairDos = dataMediaPairDao.listAll();
                if (dataMediaPairDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any dataMediaPair, maybe hasn't create any dataMediaPair.");
                    return dataMediaPairs;
                }
            } else {
                dataMediaPairDos = dataMediaPairDao.listByMultiId(identities);
                if (dataMediaPairDos.isEmpty()) {
                    String exceptionCause = "couldn't query any dataMediaPair by dataMediaPairIds:"
                                            + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            dataMediaPairs = doToModel(dataMediaPairDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMediaPairs has an exception!", e);
            throw new ManagerException(e);
        }

        return dataMediaPairs;

    }

    /**
     * 查找所有额DataMediaPair
     */
    public List<DataMediaPair> listAll() {
        return listByIds();
    }

    @Override
    public List<DataMediaPair> listByCondition(Map condition) {
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();

        try {
            List<DataMediaPairDO> dataMediaPairDos = dataMediaPairDao.listByCondition(condition);
            if (dataMediaPairDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any DataMediaPairs by the condition:"
                             + JsonUtils.marshalToString(condition));
                return dataMediaPairs;
            }
            dataMediaPairs = doToModel(dataMediaPairDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMediaPairs by condition has an exception!", e);
            throw new ManagerException(e);
        }
        return dataMediaPairs;
    }

    /**
     * 根据对应的dataMediaPairId找到对应的dataMediaPair
     */
    public DataMediaPair findById(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        List<DataMediaPair> dataMediaPairs = listByIds(dataMediaPairId);
        if (dataMediaPairs.size() != 1) {
            String exceptionCause = "query dataMediaPairId:" + dataMediaPairId + " but return " + dataMediaPairs.size()
                                    + " Pairs.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }
        return dataMediaPairs.get(0);
    }

    /**
     * 根据PipelineId找到该枝干下的所有DataMediaPairs
     */
    public List<DataMediaPair> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();
        try {
            List<DataMediaPairDO> dataMediaPairDos = dataMediaPairDao.listByPipelineId(pipelineId);
            if (dataMediaPairDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataMediaPair, maybe hasn't create any dataMediaPair.");
                return dataMediaPairs;
            }
            dataMediaPairs = doToModel(dataMediaPairDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMediaPairs by pipelineId:" + pipelineId + " has an exception!", e);
            throw new ManagerException(e);
        }

        return dataMediaPairs;
    }

    @Override
    public List<DataMediaPair> listByPipelineIdWithoutColumn(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();
        try {
            List<DataMediaPairDO> dataMediaPairDos = dataMediaPairDao.listByPipelineId(pipelineId);
            if (dataMediaPairDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataMediaPair, maybe hasn't create any dataMediaPair.");
                return dataMediaPairs;
            }
            dataMediaPairs = doToModelWithoutOther(dataMediaPairDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMediaPairs by pipelineId:" + pipelineId + " has an exception!", e);
            throw new ManagerException(e);
        }

        return dataMediaPairs;
    }

    @Override
    public List<DataMediaPair> listByDataMediaId(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();
        try {
            List<DataMediaPairDO> dataMediaPairDos = dataMediaPairDao.listByDataMediaId(dataMediaId);
            if (dataMediaPairDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataMediaPair, maybe hasn't create any dataMediaPair.");
                return dataMediaPairs;
            }
            dataMediaPairs = doToModel(dataMediaPairDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMediaPairs by dataMediaId:" + dataMediaId + " has an exception!", e);
            throw new ManagerException(e);
        }

        return dataMediaPairs;
    }

    public int getCount() {
        return dataMediaPairDao.getCount();
    }

    public int getCount(Map condition) {
        return dataMediaPairDao.getCount(condition);
    }

    /*-------------------------------------------------------------*/

    private DataMediaPair doToModel(DataMediaPairDO dataMediaPairDo, List<ColumnPair> columnPairs,
                                    List<ColumnGroup> columnGroups) {
        DataMediaPair dataMediaPair = new DataMediaPair();
        try {
            dataMediaPair.setId(dataMediaPairDo.getId());
            dataMediaPair.setPipelineId(dataMediaPairDo.getPipelineId());
            dataMediaPair.setPullWeight(dataMediaPairDo.getPullWeight());
            dataMediaPair.setPushWeight(dataMediaPairDo.getPushWeight());
            if (StringUtils.isNotBlank(dataMediaPairDo.getFilter())) {
                dataMediaPair.setFilterData(JsonUtils.unmarshalFromString(dataMediaPairDo.getFilter(),
                                                                          ExtensionData.class));
            }

            if (StringUtils.isNotBlank(dataMediaPairDo.getResolver())) {
                dataMediaPair.setResolverData(JsonUtils.unmarshalFromString(dataMediaPairDo.getResolver(),
                                                                            ExtensionData.class));
            }
            dataMediaPair.setColumnPairs(columnPairs);
            dataMediaPair.setColumnGroups(columnGroups);
            dataMediaPair.setColumnPairMode(dataMediaPairDo.getColumnPairMode());
            dataMediaPair.setGmtCreate(dataMediaPairDo.getGmtCreate());
            dataMediaPair.setGmtModified(dataMediaPairDo.getGmtModified());

            // 组装DataMedia
            List<DataMedia> dataMedias = dataMediaService.listByIds(dataMediaPairDo.getSourceDataMediaId(),
                                                                    dataMediaPairDo.getTargetDataMediaId());
            if (null == dataMedias || dataMedias.size() != 2) {
                // 抛出异常
                return dataMediaPair;
            }

            for (DataMedia dataMedia : dataMedias) {
                if (dataMedia.getId().equals(dataMediaPairDo.getSourceDataMediaId())) {
                    dataMediaPair.setSource(dataMedia);
                } else if (dataMedia.getId().equals(dataMediaPairDo.getTargetDataMediaId())) {
                    dataMediaPair.setTarget(dataMedia);
                }
            }
        } catch (Exception e) {
            logger.error("ERROR ## change the dataMediaPair Do to Model has an exception", e);
            throw new ManagerException(e);
        }

        return dataMediaPair;
    }

    private List<DataMediaPair> doToModel(List<DataMediaPairDO> dataMediaPairDos) {
        List<Long> dataMediaPairIds = new ArrayList<Long>();
        for (DataMediaPairDO dataMediaPairDo : dataMediaPairDos) {
            dataMediaPairIds.add(dataMediaPairDo.getId());
        }
        Map<Long, List<ColumnPair>> columnPairMap = dataColumnPairService.listByDataMediaPairIds(dataMediaPairIds.toArray(new Long[dataMediaPairIds.size()]));
        Map<Long, List<ColumnGroup>> columnPairGroupMap = dataColumnPairGroupService.listByDataMediaPairIds(dataMediaPairIds.toArray(new Long[dataMediaPairIds.size()]));
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();
        for (DataMediaPairDO dataMediaPairDo : dataMediaPairDos) {
            List<ColumnPair> columnPairs = columnPairMap.get(dataMediaPairDo.getId()) == null ? new ArrayList<ColumnPair>() : columnPairMap.get(dataMediaPairDo.getId());
            List<ColumnGroup> columnGroups = columnPairGroupMap.get(dataMediaPairDo.getId()) == null ? new ArrayList<ColumnGroup>() : columnPairGroupMap.get(dataMediaPairDo.getId());
            dataMediaPairs.add(doToModel(dataMediaPairDo, columnPairs, columnGroups));
        }

        return dataMediaPairs;
    }

    private List<DataMediaPair> doToModelWithoutOther(List<DataMediaPairDO> dataMediaPairDos) {
        List<DataMediaPair> dataMediaPairs = new ArrayList<DataMediaPair>();
        for (DataMediaPairDO dataMediaPairDo : dataMediaPairDos) {
            dataMediaPairs.add(doToModel(dataMediaPairDo, null, null));
        }

        return dataMediaPairs;
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param dataMediaPair
     * @return DataMediaPairDO
     */
    private DataMediaPairDO modelToDo(DataMediaPair dataMediaPair) {
        DataMediaPairDO dataMediaPairDo = new DataMediaPairDO();
        try {
            dataMediaPairDo.setId(dataMediaPair.getId());
            dataMediaPairDo.setPipelineId(dataMediaPair.getPipelineId());
            dataMediaPairDo.setSourceDataMediaId(dataMediaPair.getSource().getId());
            dataMediaPairDo.setTargetDataMediaId(dataMediaPair.getTarget().getId());
            dataMediaPairDo.setFilter(JsonUtils.marshalToString(dataMediaPair.getFilterData()));
            dataMediaPairDo.setResolver(JsonUtils.marshalToString(dataMediaPair.getResolverData()));
            dataMediaPairDo.setPullWeight(dataMediaPair.getPullWeight());
            dataMediaPairDo.setPushWeight(dataMediaPair.getPushWeight());
            dataMediaPairDo.setColumnPairMode(dataMediaPair.getColumnPairMode());
            dataMediaPairDo.setGmtCreate(dataMediaPair.getGmtCreate());
            dataMediaPairDo.setGmtModified(dataMediaPair.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the dataMediaPair Model to Do has an exception", e);
            throw new ManagerException(e);
        }

        return dataMediaPairDo;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setDataMediaPairDao(DataMediaPairDAO dataMediaPairDao) {
        this.dataMediaPairDao = dataMediaPairDao;
    }

    public void setDataMediaService(DataMediaService dataMediaService) {
        this.dataMediaService = dataMediaService;
    }

    public void setDataColumnPairService(DataColumnPairService dataColumnPairService) {
        this.dataColumnPairService = dataColumnPairService;
    }

    public void setDataColumnPairGroupService(DataColumnPairGroupService dataColumnPairGroupService) {
        this.dataColumnPairGroupService = dataColumnPairGroupService;
    }
}
