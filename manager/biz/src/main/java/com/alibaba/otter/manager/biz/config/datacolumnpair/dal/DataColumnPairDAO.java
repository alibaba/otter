package com.alibaba.otter.manager.biz.config.datacolumnpair.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairDO;

/**
 * 类DataColumnPairDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:08:55
 */
public interface DataColumnPairDAO extends GenericDAO<DataColumnPairDO> {

    public List<DataColumnPairDO> listByDataMediaPairId(Long dataMediaPairId);

    public List<DataColumnPairDO> listByDataMediaPairIds(Long... dataMediaPairIds);

    public void insertBatch(List<DataColumnPairDO> dataColumnPairDos);

    public void deleteByDataMediaPairId(Long dataMediaPairId);
}
