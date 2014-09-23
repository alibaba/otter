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

package com.alibaba.otter.node.etl.load.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.common.statistics.StatisticsClientService;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker.LoadCounter;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker.LoadThroughput;
import com.alibaba.otter.node.etl.load.loader.db.DataBatchLoader;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * loader执行工厂，可根据不同的数据load进行路由到指定的{@linkplain OtterLoader}进行处理
 * 
 * @author simon 2012-7-3 下午4:16:58
 * @version 4.1.0
 */
public class OtterLoaderFactory {

    private DataBatchLoader         dataBatchLoader;
    private LoadStatsTracker        loadStatsTracker;
    private StatisticsClientService statisticsClientService;

    public List<LoadContext> load(DbBatch dbBatch) {
        try {
            return dataBatchLoader.load(dbBatch);
        } finally {
            try {
                sendStat(dbBatch.getRowBatch().getIdentity());
            } finally {
                loadStatsTracker.removeStat(dbBatch.getRowBatch().getIdentity());
            }
        }

    }

    private void sendStat(Identity identity) {
        LoadThroughput throughput = loadStatsTracker.getStat(identity);
        Collection<LoadCounter> counters = throughput.getStats();
        Date endTime = new Date();
        // 处理table stat
        long fileSize = 0L;
        long fileCount = 0L;
        long rowSize = 0L;
        long rowCount = 0L;
        long mqSize = 0L;
        long mqCount = 0L;
        List<TableStat> tableStats = new ArrayList<TableStat>();
        for (LoadCounter counter : counters) {
            TableStat stat = new TableStat();
            stat.setPipelineId(identity.getPipelineId());
            stat.setDataMediaPairId(counter.getPairId());
            stat.setFileCount(counter.getFileCount().longValue());
            stat.setFileSize(counter.getFileSize().longValue());
            stat.setInsertCount(counter.getInsertCount().longValue());
            stat.setUpdateCount(counter.getUpdateCount().longValue());
            stat.setDeleteCount(counter.getDeleteCount().longValue());
            stat.setStartTime(new Date(throughput.getStartTime()));
            stat.setEndTime(endTime);
            // 5项中有一项不为空才通知
            if (!(stat.getFileCount().equals(0L) && stat.getFileSize().equals(0L) && stat.getInsertCount().equals(0L)
                  && stat.getDeleteCount().equals(0L) && stat.getUpdateCount().equals(0L))) {
                tableStats.add(stat);
            }

            fileSize += counter.getFileSize().longValue();
            fileCount += counter.getFileCount().longValue();
            rowSize += counter.getRowSize().longValue();
            rowCount += counter.getRowCount().longValue();

            mqSize += counter.getMqSize().longValue();
            mqCount += counter.getMqCount().longValue();
        }
        if (!CollectionUtils.isEmpty(tableStats)) {
            statisticsClientService.sendTableStats(tableStats);
        }

        List<ThroughputStat> throughputStats = new ArrayList<ThroughputStat>();
        if (!(rowCount == 0 && rowSize == 0)) {
            // 处理Throughput stat
            ThroughputStat rowThroughputStat = new ThroughputStat();
            rowThroughputStat.setType(ThroughputType.ROW);
            rowThroughputStat.setPipelineId(identity.getPipelineId());
            rowThroughputStat.setNumber(rowCount);
            rowThroughputStat.setSize(rowSize);
            rowThroughputStat.setStartTime(new Date(throughput.getStartTime()));
            rowThroughputStat.setEndTime(endTime);
            throughputStats.add(rowThroughputStat);
        }
        if (!(fileCount == 0 && fileSize == 0)) {
            ThroughputStat fileThroughputStat = new ThroughputStat();
            fileThroughputStat.setType(ThroughputType.FILE);
            fileThroughputStat.setPipelineId(identity.getPipelineId());
            fileThroughputStat.setNumber(fileCount);
            fileThroughputStat.setSize(fileSize);
            fileThroughputStat.setStartTime(new Date(throughput.getStartTime()));
            fileThroughputStat.setEndTime(endTime);
            throughputStats.add(fileThroughputStat);
        }

        // add by 2012-07-06 for mq loader
        if (!(mqCount == 0 && mqSize == 0)) {
            ThroughputStat mqThroughputStat = new ThroughputStat();
            mqThroughputStat.setType(ThroughputType.MQ);
            mqThroughputStat.setPipelineId(identity.getPipelineId());
            mqThroughputStat.setNumber(mqCount);
            mqThroughputStat.setSize(mqSize);
            mqThroughputStat.setStartTime(new Date(throughput.getStartTime()));
            mqThroughputStat.setEndTime(endTime);
            throughputStats.add(mqThroughputStat);
        }

        if (!CollectionUtils.isEmpty(throughputStats)) {
            statisticsClientService.sendThroughputs(throughputStats);
        }
    }

    public void setStartTime(Identity identity, Long startTime) {
        // 初始一下startTime
        loadStatsTracker.getStat(identity).setStartTime(startTime);
    }

    // ================= setter / getter ================

    public void setStatisticsClientService(StatisticsClientService statisticsClientService) {
        this.statisticsClientService = statisticsClientService;
    }

    public void setDataBatchLoader(DataBatchLoader dataBatchLoader) {
        this.dataBatchLoader = dataBatchLoader;
    }

    public void setLoadStatsTracker(LoadStatsTracker loadStatsTracker) {
        this.loadStatsTracker = loadStatsTracker;
    }

}
