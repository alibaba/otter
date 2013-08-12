package com.alibaba.otter.shared.arbitrate.impl.setl.helper;

import java.util.Comparator;
import java.util.Map;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.google.common.collect.Maps;

/**
 * stage阶段的排序因子
 * 
 * @author jianghang 2011-9-22 上午09:57:17
 * @version 4.0.0
 */
public class StageComparator implements Comparator<String> {

    private static final Map<String, Integer> stageIndex = Maps.newHashMap();
    static {
        stageIndex.put(ArbitrateConstants.NODE_SELECTED, 1);
        stageIndex.put(ArbitrateConstants.NODE_EXTRACTED, 2);
        stageIndex.put(ArbitrateConstants.NODE_TRANSFORMED, 3);
        // stageIndex.put(ArbitrateConstants.NODE_LOADED, 4);
    }

    @Override
    public int compare(String o1, String o2) {
        int i1 = (stageIndex.get(o1) == null ? 0 : stageIndex.get(o1));
        int i2 = (stageIndex.get(o2) == null ? 0 : stageIndex.get(o2));

        if (i1 > i2) {
            return 1;
        } else if (i1 == i2) {
            return 0;
        } else {
            return -1;
        }
    }

}
