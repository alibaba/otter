package com.alibaba.otter.manager.biz.config.datacolumnpair.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairGroupDO;

/**
 * 类DataColumnPairGroupDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:09:09
 */
public interface DataColumnPairGroupDAO extends GenericDAO<DataColumnPairGroupDO> {

    public void deleteByDataMediaPairId(Long dataMediaPairId);

    public List<DataColumnPairGroupDO> ListByDataMediaPairId(Long dataMediaPairId);

    public List<DataColumnPairGroupDO> ListByDataMediaPairIds(Long... dataMediaPairIds);
}
