package com.alibaba.otter.manager.biz.statistics.throughput.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jianghang 2011-9-8 下午01:23:59
 */
public class RealtimeThroughputCondition extends ThroughputCondition {

    private List<AnalysisType> analysisType;

    /**
     * analysisType的最大value
     */
    public int getMax() {
        List<Integer> value = new ArrayList<Integer>();
        for (AnalysisType at : analysisType) {
            value.add(at.getValue());
        }
        return Collections.max(value);
    }

    /**
     * analysisType的最小value
     */
    public int getMin() {
        List<Integer> value = new ArrayList<Integer>();
        for (AnalysisType at : analysisType) {
            value.add(at.getValue());
        }
        return Collections.min(value);
    }

    // ===================== setter / getter =========================

    public List<AnalysisType> getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(List<AnalysisType> analysisType) {
        this.analysisType = analysisType;
    }

}
