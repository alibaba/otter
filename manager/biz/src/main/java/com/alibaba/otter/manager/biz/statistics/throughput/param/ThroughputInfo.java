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

import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;

/**
 * @author jianghang 2011-9-8 下午01:27:33
 */
public class ThroughputInfo {

    private Long                 seconds = 60L;
    private List<ThroughputStat> items;

    /**
     * 对应number的数据统计平均值
     */
    public Long getTps() {
        Long tps = 0L;
        if (items.size() != 0) {
            for (ThroughputStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    tps += item.getNumber();
                } else {
                    tps += item.getNumber() * 1000 / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }
            if (seconds != 0) {
                tps = tps / seconds;
            }
        }
        return tps;
    }

    /**
     * 对应size的数据统计平均值
     */
    public Long getQuantity() {
        Long quantity = 0L;
        if (items.size() != 0) {
            for (ThroughputStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    quantity += item.getSize();
                } else {
                    quantity += item.getSize() * 1000 / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }

            if (seconds != 0) {
                quantity = quantity / items.size();
            }
        }
        return quantity;
    }

    /**
     * 对应number的数据统计
     */
    public Long getNumber() {
        Long number = 0L;
        if (items.size() != 0) {
            for (ThroughputStat item : items) {
                number += item.getNumber();
            }
        }
        return number;
    }

    /**
     * 对应size的数据统计
     */
    public Long getSize() {
        Long size = 0L;
        if (items.size() != 0) {
            for (ThroughputStat item : items) {
                size += item.getSize();
            }
        }
        return size;
    }

    // ===================== setter / getter =========================

    public List<ThroughputStat> getItems() {
        return items;
    }

    public void setItems(List<ThroughputStat> items) {
        this.items = items;
    }

    public Long getSeconds() {
        return seconds;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }

}
