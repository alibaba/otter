package com.alibaba.otter.shared.etl.model;

import java.util.Comparator;

/**
 * 按照EventColumn的index进行排序.
 * 
 * @author xiaoqing.zhouxq 2012-3-8 上午11:38:25
 */
public class EventColumnIndexComparable implements Comparator<EventColumn> {

    public int compare(EventColumn o1, EventColumn o2) {
        if (o1.getIndex() < o2.getIndex()) {
            return -1;
        } else if (o1.getIndex() == o2.getIndex()) {
            return 0;
        } else {
            return 1;
        }
    }

}
