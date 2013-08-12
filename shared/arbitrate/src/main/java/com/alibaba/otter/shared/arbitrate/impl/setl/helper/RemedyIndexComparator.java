package com.alibaba.otter.shared.arbitrate.impl.setl.helper;

import java.util.Comparator;

import com.alibaba.otter.shared.arbitrate.model.RemedyIndexEventData;

/**
 * Remedy 排序，根据processId (process和startTime/endTime一定保持一致的排序性）
 * 
 * @author jianghang 2012-4-13 下午02:20:35
 * @version 4.0.2
 */
public class RemedyIndexComparator implements Comparator<RemedyIndexEventData> {

    public int compare(RemedyIndexEventData index1, RemedyIndexEventData index2) {
        return index1.getProcessId().compareTo(index2.getProcessId());
    }
}
