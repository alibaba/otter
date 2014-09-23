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

package com.alibaba.otter.manager.biz.statistics.delay.param;

import java.util.Date;

/**
 * @author jianghang 2013-3-12 下午06:12:38
 * @version 4.1.7
 */
public class TopDelayStat {

    private String   channelName;
    private String   pipelineName;
    private Long     channelId;
    private Long     pipelineId;
    private Long     delayTime;
    private Date     lastUpdate;                     // 延迟统计最后一次更新时间
    private Long     statTime;                       // stat统计时间范围,分钟为单位
    private DataStat dbStat   = new DataStat(0L, 0L);
    private DataStat fileStat = new DataStat(0L, 0L);

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getStatTime() {
        return statTime;
    }

    public void setStatTime(Long statTime) {
        this.statTime = statTime;
    }

    public DataStat getDbStat() {
        return dbStat;
    }

    public void setDbStat(DataStat dbStat) {
        this.dbStat = dbStat;
    }

    public DataStat getFileStat() {
        return fileStat;
    }

    public void setFileStat(DataStat fileStat) {
        this.fileStat = fileStat;
    }

    /**
     * 获取延迟统计最后更新时间距当前时间的差值
     */
    public Long getLastUpdateDelay() {
        return (new Date().getTime() - lastUpdate.getTime()) / 1000;
    }

    public static class DataStat {

        public DataStat(Long number, Long size){
            this.number = number;
            this.size = size;
        }

        private Long number;
        private Long size;

        public Long getNumber() {
            return number;
        }

        public void setNumber(Long number) {
            this.number = number;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

    }
}
