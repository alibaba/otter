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
