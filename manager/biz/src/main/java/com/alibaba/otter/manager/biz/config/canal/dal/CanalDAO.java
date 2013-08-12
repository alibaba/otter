package com.alibaba.otter.manager.biz.config.canal.dal;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO;

/**
 * @author sarah.lij 2012-7-25 下午05:05:37
 */
public interface CanalDAO extends GenericDAO<CanalDO> {

    public CanalDO findByName(String name);
}
