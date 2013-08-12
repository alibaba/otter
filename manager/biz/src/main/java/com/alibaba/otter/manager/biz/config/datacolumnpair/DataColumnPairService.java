package com.alibaba.otter.manager.biz.config.datacolumnpair;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;

/**
 * 类DataColumnPairService.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:07:47
 */
public interface DataColumnPairService extends GenericService<ColumnPair> {

    public List<ColumnPair> listByDataMediaPairId(Long dataMediaPairId);

    public Map<Long, List<ColumnPair>> listByDataMediaPairIds(Long... dataMediaPairIds);

    public void createBatch(List<ColumnPair> dataColumnPairs);

    public void removeByDataMediaPairId(Long dataMediaPairId);
}
