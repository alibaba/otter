package com.alibaba.otter.manager.biz.statistics.delay.param;

import java.io.Serializable;
import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;

public class DelayStatInfo implements Serializable {

    private static final long serialVersionUID = -6145961871313642767L;
    private List<DelayStat>   items;

    /**
     * 一段时间内堆积量的平均值统计
     */

    public Double getAvgDelayNumber() {
        Double avgDelayNumber = 0.0;
        if (items.size() != 0) {
            for (DelayStat item : items) {
                avgDelayNumber += item.getDelayNumber();
            }
            avgDelayNumber = avgDelayNumber / items.size();
        }
        return avgDelayNumber;
    }

    /**
     * 一段时间内延迟时间的平均值统计
     */

    public Double getAvgDelayTime() {
        Double avgDelayTime = 0.0;
        if (items.size() != 0) {
            for (DelayStat item : items) {
                avgDelayTime += item.getDelayTime();
            }
            avgDelayTime = avgDelayTime / items.size();
        }
        return avgDelayTime;
    }

    // ===================== setter / getter =========================

    public List<DelayStat> getItems() {
        return items;
    }

    public void setItems(List<DelayStat> items) {
        this.items = items;
    }

}
