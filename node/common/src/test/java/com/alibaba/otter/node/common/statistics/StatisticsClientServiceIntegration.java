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

package com.alibaba.otter.node.common.statistics;

import java.util.Arrays;
import java.util.Date;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.BaseOtterTest;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayCount;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

public class StatisticsClientServiceIntegration extends BaseOtterTest {

    @SpringBeanByName
    private StatisticsClientService statisticsClientService;

    @Test
    public void testSend() {
        sendDelayStat();
        sendThroughputs();
        sendTableStats();

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            want.fail();
        }
    }

    private void sendDelayStat() {
        DelayCount count = new DelayCount();
        count.setPipelineId(1L);
        count.setNumber(100L);
        count.setTime(5L);

        statisticsClientService.sendIncDelayCount(count);
        statisticsClientService.sendDecDelayCount(count);
    }

    private void sendThroughputs() {
        Date now = new Date();

        ThroughputStat rowStat = new ThroughputStat();
        rowStat.setType(ThroughputType.ROW);
        rowStat.setStartTime(new Date(now.getTime() - 600 * 1000L));
        rowStat.setEndTime(now);
        rowStat.setPipelineId(1L);
        rowStat.setNumber(100L);
        rowStat.setSize(100L);

        ThroughputStat fileStat = new ThroughputStat();
        fileStat.setType(ThroughputType.FILE);
        fileStat.setStartTime(new Date(now.getTime() - 800 * 1000L));
        fileStat.setEndTime(now);
        fileStat.setPipelineId(1L);
        fileStat.setNumber(101L);
        fileStat.setSize(101L);

        statisticsClientService.sendThroughputs(Arrays.asList(rowStat, fileStat));
    }

    private void sendTableStats() {
        TableStat stat1 = new TableStat();
        stat1.setPipelineId(1L);
        stat1.setDataMediaPairId(1L);
        stat1.setFileCount(100L);
        stat1.setFileSize(100L);
        stat1.setInsertCount(100L);
        stat1.setUpdateCount(100L);
        stat1.setDeleteCount(100L);

        TableStat stat2 = new TableStat();
        stat2.setPipelineId(1L);
        stat2.setDataMediaPairId(2L);
        stat2.setFileCount(101L);
        stat2.setFileSize(101L);
        stat2.setInsertCount(101L);
        stat2.setUpdateCount(101L);
        stat2.setDeleteCount(101L);

        statisticsClientService.sendTableStats(Arrays.asList(stat1, stat2));
    }
}
