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

package com.alibaba.otter.manager.biz.config.datamedia.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.common.DataSourceCreator;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamedia.dal.DataMediaDAO;
import com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.data.mq.MqDataMedia;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.meta.DdlUtils;

/**
 * @author simon
 */
public class DataMediaServiceImpl implements DataMediaService {

    private static final Logger    logger = LoggerFactory.getLogger(DataMediaServiceImpl.class);

    private DataMediaDAO           dataMediaDao;

    private DataMediaSourceService dataMediaSourceService;

    private DataSourceCreator      dataSourceCreator;

    @Override
    public List<String> queryColumnByMediaId(Long dataMediaId) {
        return queryColumnByMedia(findById(dataMediaId));
    }

    @Override
    public List<String> queryColumnByMedia(DataMedia dataMedia) {
        List<String> columnResult = new ArrayList<String>();
        if (dataMedia.getSource().getType().isNapoli()) {
            return columnResult;
        }

        DataSource dataSource = dataSourceCreator.createDataSource(dataMedia.getSource());
        // 针对multi表，直接获取第一个匹配的表结构
        String schemaName = dataMedia.getNamespaceMode().getSingleValue();
        String tableName = dataMedia.getNameMode().getSingleValue();
        try {
            Table table = DdlUtils.findTable(new JdbcTemplate(dataSource), schemaName, schemaName, tableName);
            for (Column column : table.getColumns()) {
                columnResult.add(column.getName());
            }
        } catch (Exception e) {
            logger.error("ERROR ## DdlUtils find table happen error!", e);
        }

        return columnResult;
    }

    /**
     * 添加
     */
    @Override
    public void create(DataMedia dataMedia) {
        Assert.assertNotNull(dataMedia);
        try {
            DataMediaDO dataMediaDo = modelToDo(dataMedia);
            dataMediaDo.setId(0L);
            if (!dataMediaDao.checkUnique(dataMediaDo)) {
                String exceptionCause = "exist the same name dataMedia in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }

            dataMediaDao.insert(dataMediaDo);
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## create dataMedia has an exception!");
            throw new ManagerException(e);
        }
    }

    /**
     * 添加
     */
    @Override
    public Long createReturnId(DataMedia dataMedia) {
        Assert.assertNotNull(dataMedia);
        try {
            DataMediaDO dataMediaDo = modelToDo(dataMedia);
            dataMediaDo.setId(0L);
            DataMediaDO dataMediaDoInDb = dataMediaDao.checkUniqueAndReturnExist(dataMediaDo);
            if (dataMediaDoInDb == null) {
                dataMediaDo = dataMediaDao.insert(dataMediaDo);
            } else {
                dataMediaDo = dataMediaDoInDb;
            }
            return dataMediaDo.getId();
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## create dataMedia has an exception!");
            throw new ManagerException(e);
        }
    }

    /**
     * 删除
     */
    @Override
    public void remove(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        try {
            dataMediaDao.delete(dataMediaId);
        } catch (Exception e) {
            logger.error("ERROR ## remove dataMedia has an exception!");
            throw new ManagerException(e);
        }

    }

    /**
     * 修改
     */
    @Override
    public void modify(DataMedia dataMedia) {
        Assert.assertNotNull(dataMedia);
        try {
            DataMediaDO dataMediaDo = modelToDo(dataMedia);
            if (dataMediaDao.checkUnique(dataMediaDo)) {
                dataMediaDao.update(dataMediaDo);
            } else {
                String exceptionCause = "exist the same name dataMedia in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## modify dataMedia has an exception!");
            throw new ManagerException(e);
        }
    }

    /**
     * 查出所有的DataMedia
     */
    @Override
    public List<DataMedia> listAll() {
        return listByIds();
    }

    @Override
    public List<DataMedia> listByCondition(Map condition) {
        List<DataMedia> dataMedias = new ArrayList<DataMedia>();
        try {
            List<DataMediaDO> dataMediaDos = dataMediaDao.listByCondition(condition);
            if (dataMediaDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataMedias by the condition:"
                             + JsonUtils.marshalToString(condition));
                return dataMedias;
            }
            dataMedias = doToModel(dataMediaDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMedias by condition has an exception!");
            throw new ManagerException(e);
        }

        return dataMedias;
    }

    /**
     * 根据dataMediaId查询出dataMedia Model
     */
    @Override
    public DataMedia findById(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        List<DataMedia> dataMedias = listByIds(dataMediaId);
        if (dataMedias.size() != 1) {
            String exceptionCause = "query dataMediaId:" + dataMediaId + " but return " + dataMedias.size()
                                    + " dataMedia.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }
        return dataMedias.get(0);

    }

    @Override
    public List<DataMedia> listByIds(Long... identities) {
        List<DataMedia> dataMedias = new ArrayList<DataMedia>();
        try {
            List<DataMediaDO> dataMediaDos = null;
            if (identities.length < 1) {
                dataMediaDos = dataMediaDao.listAll();
                if (dataMediaDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any dataMedia, maybe hasn't create any dataMedia.");
                    return dataMedias;
                }
            } else {
                dataMediaDos = dataMediaDao.listByMultiId(identities);
                if (dataMediaDos.isEmpty()) {
                    String exceptionCause = "couldn't query any dataMedia by dataMediaIds:"
                                            + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            dataMedias = doToModel(dataMediaDos);
        } catch (Exception e) {
            logger.error("ERROR ## query dataMedias has an exception!");
            throw new ManagerException(e);
        }

        return dataMedias;
    }

    @Override
    public List<DataMedia> listByDataMediaSourceId(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        List<DataMediaDO> dataMediaDos = null;
        try {
            dataMediaDos = dataMediaDao.listByDataMediaSourceId(dataMediaSourceId);
            if (dataMediaDos.isEmpty()) {
                logger.debug("DEBUG ## couldn't query any dataMedia, maybe hasn't create any dataMedia.");
                return new ArrayList<DataMedia>();
            }
        } catch (Exception e) {
            logger.error("ERROR ## query dataMedias by sourceId:" + dataMediaSourceId + " has an exception!");
            throw new ManagerException(e);
        }
        return doToModel(dataMediaDos);
    }

    @Override
    public int getCount() {
        return dataMediaDao.getCount();
    }

    @Override
    public int getCount(Map condition) {
        return dataMediaDao.getCount(condition);
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param dataMedia
     * @return DataMediaDO
     */
    private DataMediaDO modelToDo(DataMedia dataMedia) {

        DataMediaDO dataMediaDo = new DataMediaDO();

        try {
            dataMediaDo.setId(dataMedia.getId());
            dataMediaDo.setName(dataMedia.getName());
            dataMediaDo.setNamespace(dataMedia.getNamespace());
            dataMediaDo.setDataMediaSourceId(dataMedia.getSource().getId());
            // if (dataMedia instanceof DbDataMedia) {
            // dataMediaDo.setProperties(JsonUtils.marshalToString((DbDataMedia) dataMedia));
            // }
            dataMediaDo.setProperties(JsonUtils.marshalToString(dataMedia));
            dataMediaDo.setGmtCreate(dataMedia.getGmtCreate());
            dataMediaDo.setGmtModified(dataMedia.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the dataMedia Model to Do has an exception");
            throw new ManagerException(e);
        }

        return dataMediaDo;
    }

    /**
     * 用于DO对象转化为Model对象
     * 
     * @param dataMediaDo
     * @return DataMedia
     */
    private DataMedia doToModel(DataMediaDO dataMediaDo) {
        DataMedia dataMedia = null;
        try {
            DataMediaSource dataMediaSource = dataMediaSourceService.findById(dataMediaDo.getDataMediaSourceId());
            if (dataMediaSource.getType().isMysql() || dataMediaSource.getType().isOracle()) {
                dataMedia = JsonUtils.unmarshalFromString(dataMediaDo.getProperties(), DbDataMedia.class);
                dataMedia.setSource(dataMediaSource);
            } else if (dataMediaSource.getType().isNapoli() || dataMediaSource.getType().isMq()) {
                dataMedia = JsonUtils.unmarshalFromString(dataMediaDo.getProperties(), MqDataMedia.class);
                dataMedia.setSource(dataMediaSource);
            }

            dataMedia.setId(dataMediaDo.getId());
            dataMedia.setGmtCreate(dataMediaDo.getGmtCreate());
            dataMedia.setGmtModified(dataMediaDo.getGmtModified());

        } catch (Exception e) {
            logger.error("ERROR ## change the dataMedia Do to Model has an exception");
            throw new ManagerException(e);
        }

        return dataMedia;
    }

    private List<DataMedia> doToModel(List<DataMediaDO> dataMediaDos) {
        List<DataMedia> dataMedias = new ArrayList<DataMedia>();
        for (DataMediaDO dataMediaDo : dataMediaDos) {
            dataMedias.add(doToModel(dataMediaDo));
        }

        return dataMedias;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setDataMediaDao(DataMediaDAO dataMediaDao) {
        this.dataMediaDao = dataMediaDao;
    }

    public void setDataMediaSourceService(DataMediaSourceService dataMediaSourceService) {
        this.dataMediaSourceService = dataMediaSourceService;
    }

    public void setDataSourceCreator(DataSourceCreator dataSourceCreator) {
        this.dataSourceCreator = dataSourceCreator;
    }

}
