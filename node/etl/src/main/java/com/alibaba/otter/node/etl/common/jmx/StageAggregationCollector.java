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

package com.alibaba.otter.node.etl.common.jmx;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * 统计每个stage的运行信息
 * 
 * @author jianghang 2012-5-29 下午02:32:08
 * @version 4.0.2
 */
public class StageAggregationCollector {

    private Map<Long, Map<StageType, StageAggregation>> collector;
    private AtomicBoolean                               profiling = new AtomicBoolean(true);

    public StageAggregationCollector(){
        this(1024);
    }

    public StageAggregationCollector(final int bufferSize){
        collector = OtterMigrateMap.makeComputingMap(new Function<Long, Map<StageType, StageAggregation>>() {

            public Map<StageType, StageAggregation> apply(Long input) {
                return OtterMigrateMap.makeComputingMap(new Function<StageType, StageAggregation>() {

                    public StageAggregation apply(StageType input) {
                        return new StageAggregation(bufferSize);
                    }
                });
            }
        });
    }

    public void push(Long pipelineId, StageType stage, AggregationItem aggregationItem) {
        collector.get(pipelineId).get(stage).push(aggregationItem);
    }

    public String histogram(Long pipelineId, StageType stage) {
        return collector.get(pipelineId).get(stage).histogram();
    }

    public boolean isProfiling() {
        return profiling.get();
    }

    public void setProfiling(boolean profiling) {
        this.profiling.set(profiling);
    }

}
