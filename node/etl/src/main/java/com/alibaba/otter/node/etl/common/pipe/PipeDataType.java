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
