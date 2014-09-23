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

package com.alibaba.otter.manager.biz.config.record.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.record.dal.LogRecordDAO;
import com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO;

/**
 * 类IbatisLogRecordDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-6-15 下午1:52:15
 */
public class IbatisLogRecordDAO extends SqlMapClientDaoSupport implements LogRecordDAO {

    public LogRecordDO insert(LogRecordDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().insert("insertLogRecord", entityObj);
        return entityObj;
    }

    public void delete(Long identity) {
        Assert.assertNotNull(identity);
        getSqlMapClientTemplate().delete("deleteLogRecordById", identity);

    }

    public void update(LogRecordDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().update("updateLogRecord", entityObj);

    }

    public List<LogRecordDO> listAll() {
        List<LogRecordDO> logRecordDos = getSqlMapClientTemplate().queryForList("listLogRecords");
        return logRecordDos;
    }

    public List<LogRecordDO> listByCondition(Map condition) {

        List<LogRecordDO> logRecordDos = getSqlMapClientTemplate().queryForList("listLogRecordsWithCondition",
                                                                                condition);
        return logRecordDos;
    }

    public List<LogRecordDO> listByMultiId(Long... identities) {
        // TODO Auto-generated method stub
        return null;
    }

    public LogRecordDO findById(Long identity) {
        Assert.assertNotNull(identity);
        return (LogRecordDO) getSqlMapClientTemplate().queryForObject("findLogRecordById", identity);
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getLogRecordCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getLogRecordCountWithPIdAndSearchKey",
                                                                           condition);
        return count.intValue();
    }

    public boolean checkUnique(LogRecordDO entityObj) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<LogRecordDO> listByPipelineId(Long pipelineId) {
        List<LogRecordDO> logRecordDos = getSqlMapClientTemplate().queryForList("listLogRecordsByPipelineId",
                                                                                pipelineId);
        return logRecordDos;
    }

    public List<LogRecordDO> listByPipelineIdWithoutContent(Long pipelineId) {
        List<LogRecordDO> logRecordDos = getSqlMapClientTemplate().queryForList("listLogRecordsByPipelineIdWithoutContent",
                                                                                pipelineId);
        return logRecordDos;
    }

}
