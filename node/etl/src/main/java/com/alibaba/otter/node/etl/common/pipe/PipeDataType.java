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

package com.alibaba.otter.node.etl.common.pipe;

/**
 * pipe支持处理的数据类型
 * 
 * @author jianghang 2011-11-3 下午06:33:41
 * @version 4.0.0
 */
public enum PipeDataType {
    /** 数据库 */
    DB_BATCH,
    /** 附件记录 */
    FILE_BATCH,
    /** mq记录 */
    MQ_BATCH,
    /** cache记录 */
    CACHE_BATCH;

    public boolean isDbBatch() {
        return this == DB_BATCH;
    }

    public boolean isFileBatch() {
        return this == FILE_BATCH;
    }

    public boolean isMqBatch() {
        return this == MQ_BATCH;
    }

    public boolean isCacheBatch() {
        return this == CACHE_BATCH;
    }
}
