package com.alibaba.otter.manager.biz.config.datamatrix;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.model.config.data.DataMatrix;

public interface DataMatrixService {

    public void create(DataMatrix DataMatrix);

    public void remove(Long DataMatrixId);

    public void modify(DataMatrix DataMatrix);

    public List<DataMatrix> listByIds(Long... identities);

    public List<DataMatrix> listAll();

    public DataMatrix findById(Long DataMatrixId);

    public DataMatrix findByGroupKey(String name);

    public int getCount(Map condition);

    public List<DataMatrix> listByCondition(Map condition);

}
