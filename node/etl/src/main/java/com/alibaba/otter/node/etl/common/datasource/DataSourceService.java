package com.alibaba.otter.node.etl.common.datasource;

import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

/**
 * 抽象所有的data source处理service,并且返回DataMedia的meta信息
 * 
 * @author xiaoqing.zhouxq
 */
public interface DataSourceService {

    /**
     * 返回操作数据源的句柄
     * 
     * @param <T>
     * @param dataMediaId
     * @return
     */
    <T> T getDataSource(long pipelineId, DataMediaSource dataMediaSource);

    /**
     * 释放当前pipeline的数据源.
     * 
     * @param pipeline
     */
    void destroy(Long pipelineId);

}
