package com.alibaba.otter.manager.biz.config.canal.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.canal.dal.CanalDAO;
import com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO;

/**
 * @author sarah.lij 2012-7-25 下午05:12:29
 */
public class IbatisCanalDAO extends SqlMapClientDaoSupport implements CanalDAO {

    public CanalDO insert(CanalDO canal) {
        Assert.assertNotNull(canal);
        getSqlMapClientTemplate().insert("insertCanal", canal);
        return canal;
    }

    public void delete(Long canalId) {
        Assert.assertNotNull(canalId);
        getSqlMapClientTemplate().delete("deleteCanalById", canalId);
    }

    public void update(CanalDO canal) {
        Assert.assertNotNull(canal);
        getSqlMapClientTemplate().update("updateCanal", canal);
    }

    public List<CanalDO> listAll() {
        return (List<CanalDO>) getSqlMapClientTemplate().queryForList("listCanals");
    }

    public List<CanalDO> listByMultiId(Long... identities) {
        List<CanalDO> canalDos = getSqlMapClientTemplate().queryForList("listCanalByIds", identities);
        return canalDos;
    }

    public boolean checkUnique(CanalDO canal) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkCanalUnique", canal);
        return count == 0 ? true : false;
    }

    public CanalDO findByName(String name) {
        Assert.assertNotNull(name);
        return (CanalDO) getSqlMapClientTemplate().queryForObject("findCanalByName", name);
    }

    public CanalDO findById(Long identity) {
        throw new UnsupportedOperationException();
    }

    public int getCount() {
        return 0;
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getCanalCount", condition);
        return count.intValue();
    }

    public List<CanalDO> listByCondition(Map condition) {
        List<CanalDO> canalDos = getSqlMapClientTemplate().queryForList("listCanals", condition);
        return canalDos;
    }
}
