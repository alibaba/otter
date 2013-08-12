package com.alibaba.otter.shared.common.model.config.data;

/**
 * @author jianghang 2013-1-6 下午05:32:11
 * @version 4.1.6
 */
public enum ColumnPairMode {
    INCLUDE, EXCLUDE;

    public boolean isInclude() {
        return this == INCLUDE;
    }

    public boolean isExclude() {
        return this == EXCLUDE;
    }
}
