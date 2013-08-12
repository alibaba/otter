package com.alibaba.otter.manager.biz.statistics.throughput.param;

/**
 * @author jianghang 2011-9-8 下午01:22:51
 */
public enum AnalysisType {
    ONE_MINUTE(1), FIVE_MINUTE(5), FIFTEEN_MINUTE(15);

    private final int value;

    public int getValue() {
        return value;
    }

    AnalysisType(int value){
        this.value = value;
    }
}
