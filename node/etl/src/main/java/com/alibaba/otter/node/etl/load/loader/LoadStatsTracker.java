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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.otter.shared.etl.model.Identity;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.google.common.collect.OtterMigrateMap;

/**
 * 统计跟踪器
 * 
 * @author jianghang 2011-11-18 上午11:15:10
 * @version 4.0.0
 */
public class LoadStatsTracker {

    private Map<Identity, LoadThroughput> throughputs;

    public LoadStatsTracker(){
        throughputs = OtterMigrateMap.makeComputingMap(new Function<Identity, LoadThroughput>() {

            public LoadThroughput apply(Identity identity) {
                return new LoadThroughput(identity);
            }
        });
    }

    public LoadThroughput getStat(Identity identity) {
        return throughputs.get(identity);
    }

    public void removeStat(Identity identity) {
        throughputs.remove(identity);
    }

    public static class LoadThroughput {

        private Identity               identity;
        private Long                   startTime;
        private Map<Long, LoadCounter> counters;

        public LoadThroughput(Identity identity){
            counters = MigrateMap.makeComputingMap(new Function<Long, LoadCounter>() {

                public LoadCounter apply(Long pairId) {
                    return new LoadCounter(pairId);
                }
            });
        }

        public LoadCounter getStat(Long pairId) {
            return counters.get(pairId);
        }

        public Collection<LoadCounter> getStats() {
            return counters.values();
        }

        public Identity getIdentity() {
            return identity;
        }

        public void setIdentity(Identity identity) {
            this.identity = identity;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

    }

    public static class LoadCounter {

        private Long       pairId;
        private AtomicLong fileSize    = new AtomicLong(0); // 文件大小
        private AtomicLong fileCount   = new AtomicLong(0); // 文件数量
        private AtomicLong rowSize     = new AtomicLong(0);
        private AtomicLong rowCount    = new AtomicLong(0);
        private AtomicLong mqCount     = new AtomicLong(0);
        private AtomicLong mqSize      = new AtomicLong(0);
        private AtomicLong deleteCount = new AtomicLong(0);
        private AtomicLong updateCount = new AtomicLong(0);
        private AtomicLong insertCount = new AtomicLong(0);

        public LoadCounter(Long pairId){
            this.pairId = pairId;
        }

        public Long getPairId() {
            return pairId;
        }

        public void setPairId(Long pairId) {
            this.pairId = pairId;
        }

        public AtomicLong getFileSize() {
            return fileSize;
        }

        public void setFileSize(AtomicLong fileSize) {
            this.fileSize = fileSize;
        }

        public AtomicLong getFileCount() {
            return fileCount;
        }

        public void setFileCount(AtomicLong fileCount) {
            this.fileCount = fileCount;
        }

        public AtomicLong getDeleteCount() {
            return deleteCount;
        }

        public void setDeleteCount(AtomicLong deleteCount) {
            this.deleteCount = deleteCount;
        }

        public AtomicLong getUpdateCount() {
            return updateCount;
        }

        public void setUpdateCount(AtomicLong updateCount) {
            this.updateCount = updateCount;
        }

        public AtomicLong getInsertCount() {
            return insertCount;
        }

        public void setInsertCount(AtomicLong insertCount) {
            this.insertCount = insertCount;
        }

        public AtomicLong getRowSize() {
            return rowSize;
        }

        public void setRowSize(AtomicLong rowSize) {
            this.rowSize = rowSize;
        }

        public AtomicLong getRowCount() {
            return rowCount;
        }

        public void setRowCount(AtomicLong rowCount) {
            this.rowCount = rowCount;
        }

        public AtomicLong getMqCount() {
            return mqCount;
        }

        public void setMqCount(AtomicLong mqCount) {
            this.mqCount = mqCount;
        }

        public AtomicLong getMqSize() {
            return mqSize;
        }

        public void setMqSize(AtomicLong mqSize) {
            this.mqSize = mqSize;
        }

    }

}
