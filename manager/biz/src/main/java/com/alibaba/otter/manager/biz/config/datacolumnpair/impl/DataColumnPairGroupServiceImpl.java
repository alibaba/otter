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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairGroupService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.DataColumnPairGroupDAO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairGroupDO;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * @author simon 2012-4-20 下午4:11:32
 */
public class DataColumnPairGroupServiceImpl implements DataColumnPairGroupService {

    private static final Logger    logger = LoggerFactory.getLogger(DataColumnPairGroupServiceImpl.class);

    private DataColumnPairGroupDAO dataColumnPairGroupDao;

    private DataColumnPairService  dataColumnPairService;

    @Override
    public void create(ColumnGroup entityObj) {
        Assert.assertNotNull(entityObj);

        try {
            DataColumnPairGroupDO dataColumnPairGroupDo = modelToDo(entityObj);
            dataColumnPairGroupDao.insert(dataColumnPairGroupDo);
        } catch (RepeatConfigureException rcf) {
            throw rcf;
        } catch (Exception e) {
            logger.error("ERROR ## create dataColumnPairGroup has an exception!");
            throw new ManagerException(e);
        }
    }

    @Override
    public List<ColumnGroup> listByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        List<DataColumnPairGroupDO> dataColumnPairGroupDos = dataColumnPairGroupDao.ListByDataMediaPairId(dataMediaPairId);
        if (CollectionUtils.isEmpty(dataColumnPairGroupDos)) {
            return new ArrayList<ColumnGroup>();
        }

        return doToModel(dataColumnPairGroupDos);
    }

    @Override
    public Map<Long, List<ColumnGroup>> listByDataMediaPairIds(Long... dataMediaPairIds) {
        Assert.assertNotNull(dataMediaPairIds);
        Map<Long, List<ColumnGroup>> dataColumnGroups = new HashMap<Long, List<ColumnGroup>>();
        try {
            List<DataColumnPairGroupDO> dataColumnPairGroupDos = dataColumnPairGroupDao.ListByDataMediaPairIds(dataMediaPairIds);
            if (CollectionUtils.isEmpty(dataColumnPairGroupDos)) {
                logger.debug("DEBUG ## couldn't query any dataColumnPairGroup, maybe hasn't create any dataColumnPairGroup.");
                return dataColumnGroups;
            }

            for (DataColumnPairGroupDO dataColumnPairGroupDo : dataColumnPairGroupDos) {
                List<ColumnGroup> columnGroups = dataColumnGroups.get(dataColumnPairGroupDo.getDataMediaPairId());
                if (columnGroups != null) {
                    if (!columnGroups.contains(doToModel(dataColumnPairGroupDo))) {
                        columnGroups.add(doToModel(dataColumnPairGroupDo));
                    }
                } else {
                    columnGroups = new ArrayList<ColumnGroup>();
                    columnGroups.add(doToModel(dataColumnPairGroupDo));
                    dataColumnGroups.put(dataColumnPairGroupDo.getDataMediaPairId(), columnGroups);
                }
            }
        } catch (Exception e) {
            logger.error("ERROR ## query dataColumnPairGroup by dataMediaId:" + dataMediaPairIds + " has an exception!");
            throw new ManagerException(e);
        }

        return dataColumnGroups;
    }

    @Override
    public void remove(Long identity) {

    }

    @Override
    public void removeByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        dataColumnPairGroupDao.deleteByDataMediaPairId(dataMediaPairId);
    }

    @Override
    public void modify(ColumnGroup entityObj) {

    }

    @Override
    public ColumnGroup findById(Long identity) {
        return null;
    }

    @Override
    public List<ColumnGroup> listByIds(Long... identities) {
        return null;
    }

    @Override
    public List<ColumnGroup> listAll() {
        return null;
    }

    @Override
    public List<ColumnGroup> listByCondition(Map condition) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getCount(Map condition) {
        return 0;
    }

    /*-------------------------------------------------------------*/
    /**
     * 用于DO对象转化为Model对象
     */
    private ColumnGroup doToModel(DataColumnPairGroupDO dataColumnPairGroupDo) {
        ColumnGroup columnGroup = new ColumnGroup();
        columnGroup.setId(dataColumnPairGroupDo.getId());
        List<ColumnPair> columnPairs = new ArrayList<ColumnPair>();
        if (StringUtils.isNotBlank(dataColumnPairGroupDo.getColumnPairContent())) {
            columnPairs = JsonUtils.unmarshalFromString(dataColumnPairGroupDo.getColumnPairContent(),
                                                        new TypeReference<ArrayList<ColumnPair>>() {
                                                        });
        }

        columnGroup.setColumnPairs(columnPairs);
        columnGroup.setDataMediaPairId(dataColumnPairGroupDo.getDataMediaPairId());
        columnGroup.setGmtCreate(dataColumnPairGroupDo.getGmtCreate());
        columnGroup.setGmtModified(dataColumnPairGroupDo.getGmtModified());

        return columnGroup;
    }

    private List<ColumnGroup> doToModel(List<DataColumnPairGroupDO> dataColumnPairGroupDos) {
        List<ColumnGroup> columnGroups = new ArrayList<ColumnGroup>();
        for (DataColumnPairGroupDO dataColumnPairGroupDO : dataColumnPairGroupDos) {
            columnGroups.add(doToModel(dataColumnPairGroupDO));
        }

        return columnGroups;
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param dataColumnPair
     * @return DataMediaPairDO
     */
    private DataColumnPairGroupDO modelToDo(ColumnGroup columnGroup) {
        DataColumnPairGroupDO dataColumnPairGroupDo = new DataColumnPairGroupDO();
        dataColumnPairGroupDo.setId(columnGroup.getId());
        dataColumnPairGroupDo.setColumnPairContent(JsonUtils.marshalToString(columnGroup.getColumnPairs()));
        dataColumnPairGroupDo.setDataMediaPairId(columnGroup.getDataMediaPairId());
        dataColumnPairGroupDo.setGmtCreate(columnGroup.getGmtCreate());
        dataColumnPairGroupDo.setGmtModified(columnGroup.getGmtModified());

        return dataColumnPairGroupDo;
    }

    public DataColumnPairGroupDAO getDataColumnPairGroupDao() {
        return dataColumnPairGroupDao;
    }

    public void setDataColumnPairGroupDao(DataColumnPairGroupDAO dataColumnPairGroupDao) {
        this.dataColumnPairGroupDao = dataColumnPairGroupDao;
    }

    public DataColumnPairService getDataColumnPairService() {
        return dataColumnPairService;
    }

    public void setDataColumnPairService(DataColumnPairService dataColumnPairService) {
        this.dataColumnPairService = dataColumnPairService;
    }

}
