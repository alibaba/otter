package com.alibaba.otter.manager.biz.config.datacolumnpair;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;

/**
 * 类DataColumnPairGroupService.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:08:24
 */
public interface DataColumnPairGroupService extends GenericService<ColumnGroup> {

    public void removeByDataMediaPairId(Long dataMediaPairId);

    public List<ColumnGroup> listByDataMediaPairId(Long dataMediaPairId);

    public Map<Long, List<ColumnGroup>> listByDataMediaPairIds(Long... dataMediaPairId);
}
