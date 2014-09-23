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
