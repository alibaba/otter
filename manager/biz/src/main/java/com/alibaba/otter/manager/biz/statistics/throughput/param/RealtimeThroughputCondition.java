/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
