package com.alibaba.otter.manager.biz.config.datamatrix.dal;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO;

public interface DataMatrixDAO extends GenericDAO<DataMatrixDO> {

    public DataMatrixDO findByGroupKey(String groupKey);
}
