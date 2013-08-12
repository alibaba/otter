package com.alibaba.otter.node.etl.extract.extractor;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 单条记录处理的extractor
 * 
 * @author jianghang 2012-4-18 下午04:12:39
 * @version 4.0.2
 */
public abstract class AbstractExtractor<P> implements OtterExtractor<P> {

    protected ConfigClientService configClientService;
    protected DbDialectFactory    dbDialectFactory;

    protected DbDialect getDbDialect(Long pipelineId, Long tableId) {
        DataMedia dataMedia = ConfigHelper.findDataMedia(getPipeline(pipelineId), tableId);
        return dbDialectFactory.getDbDialect(pipelineId, (DbMediaSource) dataMedia.getSource());
    }

    protected Pipeline getPipeline(Long pipelineId) {
        return configClientService.findPipeline(pipelineId);
    }

    // ==================== setter / getter =====================

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

}
