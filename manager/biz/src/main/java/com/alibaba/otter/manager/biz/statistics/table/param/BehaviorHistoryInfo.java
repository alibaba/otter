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

package com.alibaba.otter.manager.biz.statistics.table.param;

import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.table.TableStat;

/**
 * @author sarah.lij 2012-7-13 下午03:29:08
 */
public class BehaviorHistoryInfo {

    private List<TableStat> items;

    /**
     * 对应insertCount的数据统计平均值
     */

    public Long getInsertCountAvg() {
        Long insertCountAvg = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    insertCountAvg += item.getInsertCount();
                } else {
                    insertCountAvg += item.getInsertCount() * 1000
                                      / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }
            insertCountAvg = insertCountAvg / items.size();
        }
        return insertCountAvg;

    }

    /**
     * 对应updateCount的数据统计平均值
     */

    public Long getUpdateCountAvg() {
        Long updateCountAvg = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    updateCountAvg += item.getUpdateCount();
                } else {
                    updateCountAvg += item.getUpdateCount() * 1000
                                      / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }
            updateCountAvg = updateCountAvg / items.size();
        }
        return updateCountAvg;

    }

    /**
     * 对应deleteCount的数据统计平均值
     */

    public Long getDeleteCountAvg() {
        Long deleteCountAvg = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    deleteCountAvg += item.getDeleteCount();
                } else {
                    deleteCountAvg += item.getDeleteCount() * 1000
                                      / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }
            deleteCountAvg = deleteCountAvg / items.size();
        }
        return deleteCountAvg;
    }

    /**
     * 对应deleteCount的数据统计平均值
     */

    public Long getFileCountAvg() {
        Long fileCountAvg = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    fileCountAvg += item.getFileCount();
                } else {
                    fileCountAvg += item.getFileCount() * 1000
                                    / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }
            fileCountAvg = fileCountAvg / items.size();
        }
        return fileCountAvg;
    }

    /**
     * 对应deleteCount的数据统计平均值
     */

    public Long getFileSizeAvg() {
        Long fileSizeAvg = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                if (item.getEndTime().equals(item.getStartTime())) {
                    fileSizeAvg += item.getFileSize();
                } else {
                    fileSizeAvg += item.getFileSize() * 1000
                                   / (item.getEndTime().getTime() - item.getStartTime().getTime());
                }
            }
            fileSizeAvg = fileSizeAvg / items.size();
        }
        return fileSizeAvg;
    }

    /**
     * 对应insertCount的数据统计
     */
    public Long getInsertNumber() {
        Long insertNumber = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                insertNumber += item.getInsertCount();
            }
        }
        return insertNumber;
    }

    /**
     * 对应updateCount的数据统计
     */
    public Long getUpdateNumber() {
        Long updateNumber = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                updateNumber += item.getUpdateCount();
            }
        }
        return updateNumber;
    }

    /**
     * 对应deleteCount的数据统计
     */
    public Long getDeleteNumber() {
        Long deleteNumber = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                deleteNumber += item.getDeleteCount();
            }
        }
        return deleteNumber;
    }

    /**
     * 对应fileNumber的数据统计
     */
    public Long getFileNumber() {
        Long fileNumber = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                fileNumber += item.getFileCount();
            }
        }
        return fileNumber;
    }

    /**
     * 对应fileSize的数据统计
     */
    public Long getFileSize() {
        Long fileSize = 0L;
        if (items.size() != 0) {
            for (TableStat item : items) {
                fileSize += item.getFileSize();
            }
        }
        return fileSize;
    }

    // ===================== setter / getter =========================

    public List<TableStat> getItems() {
        return items;
    }

    public void setItems(List<TableStat> items) {
        this.items = items;
    }
}
