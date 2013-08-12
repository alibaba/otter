package com.alibaba.otter.shared.etl.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据记录集合对象
 * 
 * @author jianghang 2012-10-31 下午05:51:42
 * @version 4.1.2
 */
public class RowBatch extends BatchObject<EventData> {

    private static final long serialVersionUID = -6117067964148581257L;

    private List<EventData>   datas            = new LinkedList<EventData>();

    public List<EventData> getDatas() {
        return datas;
    }

    public void setDatas(List<EventData> datas) {
        this.datas = datas;
    }

    public void merge(EventData data) {
        this.datas.add(data);
    }

}
