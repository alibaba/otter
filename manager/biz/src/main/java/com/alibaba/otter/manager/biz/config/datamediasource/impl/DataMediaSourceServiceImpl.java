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

package com.alibaba.otter.manager.biz.config.datamediasource.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.biz.config.datamediasource.dal.DataMediaSourceDAO;
import com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * @author simon
 */
public class DataMediaSourceServiceImpl implements DataMediaSourceService {

    private static final Logger logger = LoggerFactory.getLogger(DataMediaSourceServiceImpl.class);

    private DataMediaSourceDAO  dataMediaSourceDao;

    /**
     * 添加
     */
    public void create(DataMediaSource dataMediaSource) {
        Assert.assertNotNull(dataMediaSource);
        try {
            DataMediaSourceDO dataMediaSourceDo = modelToDo(dataMediaSource);
            dataMediaSourceDo.setId(0L);

            if (!dataMediaSourceDao.checkUnique(dataMediaSourceDo)) {
                String exceptionCause = "exist the same name source in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }

            dataMediaSourceDao.insert(dataMediaSourceDo);

        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## create dataMediaSource has an exception!");
            throw new ManagerException(e);
        }
    }

    /**
     * 删除
     */
    public void remove(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);

        try {
            dataMediaSourceDao.delete(dataMediaSourceId);
        } catch (Exception e) {
            logger.error("ERROR ## remove dataMediaSource has an exception!");
            throw new ManagerException(e);
        }

    }

    /**
     * 修改
     */
    public void modify(DataMediaSource dataMediaSource) {
        Assert.assertNotNull(dataMediaSource);

        try {
            DataMediaSourceDO dataMediaSourceDo = modelToDo(dataMediaSource);
            if (dataMediaSourceDao.checkUnique(dataMediaSourceDo)) {
                dataMediaSourceDao.update(dataMediaSourceDo);
            } else {
                String exceptionCause = "exist the same name source in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## modify dataMediaSource has an exception!");
            throw new ManagerException(e);
        }
    }

    /**
     * 查出所有的DataMediaSource
     */
    public List<DataMediaSource> listAll() {

        return listByIds();
    }

    @Override
    public List<DataMediaSource> listByCondition(Map condition) {
        List<DataMediaSource> dataMediaSources = new ArrayList<DataMediaSource>();
        try {
            List<DataMediaSourceDO> dataMediaSourceDos = dataMediaSourceDao.listByCondition(condition);
            if (dataMediaSourceDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any DataMediaSources by the condition:"
                             + JsonUtils.marshalToString(condition));
                return dataMediaSources;
            }
            dataMediaSources = doToModel(dataMediaSourceDos);
        } catch (Exception e) {
            logger.error("ERROR ## query DataMediaSources by condition has an exception!");
            throw new ManagerException(e);
        }

        return dataMediaSources;
    }

    public List<DataMediaSource> listByIds(Long... identities) {

        List<DataMediaSourceDO> dataMediaSourceDos = null;
        if (identities.length < 1) {
            dataMediaSourceDos = dataMediaSourceDao.listAll();
            if (dataMediaSourceDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataMediaSource, maybe hasn't create any dataMediaSource.");
                return new ArrayList<DataMediaSource>();
            }
        } else {
            dataMediaSourceDos = dataMediaSourceDao.listByMultiId(identities);
            if (dataMediaSourceDos.isEmpty()) {
                String exceptionCause = "couldn't query any dataMediaSource by dataMediaSourceIds:"
                                        + Arrays.toString(identities);
                logger.error("ERROR ## " + exceptionCause);
                throw new ManagerException(exceptionCause);
            }
        }

        return doToModel(dataMediaSourceDos);
    }

    /**
     * 根据DataMediaSourceId找到对应的DataMediaSource
     */
    public DataMediaSource findById(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        List<DataMediaSource> dataMediaSources = listByIds(dataMediaSourceId);
        if (dataMediaSources.size() != 1) {
            String exceptionCause = "query dataMediaSourceId:" + dataMediaSourceId + " but return "
                                    + dataMediaSources.size() + " dataMediaSource.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }
        return dataMediaSources.get(0);

    }

    public int getCount() {
        return dataMediaSourceDao.getCount();
    }

    public int getCount(Map condition) {
        return dataMediaSourceDao.getCount(condition);
    }

    /**
     * 类型：数据库类型 Mysql和Oracle 用于Model对象转化为DO对象
     * 
     * @param dataMediaSource
     * @return DataMediaSourceDO
     */
    private DataMediaSourceDO modelToDo(DataMediaSource dataMediaSource) {
        DataMediaSourceDO dataMediaSourceDo = new DataMediaSourceDO();
        try {
            dataMediaSourceDo.setId(dataMediaSource.getId());
            dataMediaSourceDo.setName(dataMediaSource.getName());
            dataMediaSourceDo.setType(dataMediaSource.getType());
            if (dataMediaSource instanceof DbMediaSource) {
                dataMediaSourceDo.setProperties(JsonUtils.marshalToString((DbMediaSource) dataMediaSource));
            } else if (dataMediaSource instanceof MqMediaSource) {
                dataMediaSourceDo.setProperties(JsonUtils.marshalToString((MqMediaSource) dataMediaSource));
            }

            dataMediaSourceDo.setGmtCreate(dataMediaSource.getGmtCreate());
            dataMediaSourceDo.setGmtModified(dataMediaSource.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the dataMediaSource Model to Do has an exception");
            throw new ManagerException(e);
        }

        return dataMediaSourceDo;
    }

    /**
     * 类型：数据库类型 Mysql和Oracle 用于DO对象转化为Model对象
     * 
     * @param dataMediaSourceDo
     * @return DataMediaSource
     */
    private DataMediaSource doToModel(DataMediaSourceDO dataMediaSourceDo) {

        DataMediaSource dataMediaSource = new DbMediaSource();
        try {
            if (dataMediaSourceDo.getType().isMysql() || dataMediaSourceDo.getType().isOracle()) {
                dataMediaSource = JsonUtils.unmarshalFromString(dataMediaSourceDo.getProperties(), DbMediaSource.class);
            } else if (dataMediaSourceDo.getType().isNapoli() || dataMediaSourceDo.getType().isMq()) {
                dataMediaSource = JsonUtils.unmarshalFromString(dataMediaSourceDo.getProperties(), MqMediaSource.class);
            }

            dataMediaSource.setId(dataMediaSourceDo.getId());
            dataMediaSource.setGmtCreate(dataMediaSourceDo.getGmtCreate());
            dataMediaSource.setGmtModified(dataMediaSourceDo.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the dataMediaSource Do to Model has an exception");
            throw new ManagerException(e);
        }

        return dataMediaSource;
    }

    private List<DataMediaSource> doToModel(List<DataMediaSourceDO> dataMediaSourceDos) {
        List<DataMediaSource> dataMediaSources = new ArrayList<DataMediaSource>();
        for (DataMediaSourceDO dataMediaSourceDo : dataMediaSourceDos) {
            dataMediaSources.add(doToModel(dataMediaSourceDo));
        }
        return dataMediaSources;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setDataMediaSourceDao(DataMediaSourceDAO dataMediaSourceDao) {
        this.dataMediaSourceDao = dataMediaSourceDao;
    }
}
