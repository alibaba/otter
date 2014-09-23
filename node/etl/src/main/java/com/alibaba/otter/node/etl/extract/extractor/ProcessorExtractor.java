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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.datasource.DataSourceService;
import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.extension.ExtensionFactory;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplate;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplateGetter;
import com.alibaba.otter.shared.etl.extend.processor.EventProcessor;
import com.alibaba.otter.shared.etl.extend.processor.support.DataSourceFetcher;
import com.alibaba.otter.shared.etl.extend.processor.support.DataSourceFetcherAware;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * 调用{@linkplain EventProcessor}，进行业务数据处理
 * 
 * @author jianghang 2012-7-23 下午03:11:19
 */
public class ProcessorExtractor extends AbstractExtractor<DbBatch> {

    private ExtensionFactory       extensionFactory;
    private DataSourceService      dataSourceService;
    private ExecutorTemplateGetter executorTemplateGetter;

    public void extract(DbBatch param) throws ExtractException {
        ExecutorTemplate executorTemplate = null;
        try {
            RowBatch rowBatch = param.getRowBatch();
            final Pipeline pipeline = getPipeline(rowBatch.getIdentity().getPipelineId());
            List<EventData> eventDatas = rowBatch.getDatas();
            final Set<EventData> removeDatas = Collections.synchronizedSet(new HashSet<EventData>());// 使用set，提升remove时的查找速度
            executorTemplate = executorTemplateGetter.get();
            executorTemplate.start();
            // 重新设置下poolSize
            executorTemplate.adjustPoolSize(pipeline.getParameters().getExtractPoolSize());
            for (final EventData eventData : eventDatas) {
                List<DataMediaPair> dataMediaPairs = ConfigHelper.findDataMediaPairByMediaId(pipeline,
                    eventData.getTableId());
                if (dataMediaPairs == null) {
                    throw new ExtractException("ERROR ## the dataMediaId = " + eventData.getTableId()
                                               + " dataMediaPair is null,please check");
                }

                for (DataMediaPair dataMediaPair : dataMediaPairs) {
                    if (!dataMediaPair.isExistFilter()) {
                        continue;
                    }

                    final EventProcessor eventProcessor = extensionFactory.getExtension(EventProcessor.class,
                        dataMediaPair.getFilterData());
                    if (eventProcessor instanceof DataSourceFetcherAware) {
                        ((DataSourceFetcherAware) eventProcessor).setDataSourceFetcher(new DataSourceFetcher() {

                            @Override
                            public DataSource fetch(Long tableId) {
                                DataMedia dataMedia = ConfigHelper.findDataMedia(pipeline, tableId);
                                return dataSourceService.getDataSource(pipeline.getId(), dataMedia.getSource());
                            }
                        });

                        executorTemplate.submit(new Runnable() {

                            @Override
                            public void run() {
                                MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipeline.getId()));
                                boolean process = eventProcessor.process(eventData);
                                if (!process) {
                                    removeDatas.add(eventData);// 添加到删除记录中
                                }
                            }
                        });
                    } else {
                        boolean process = eventProcessor.process(eventData);
                        if (!process) {
                            removeDatas.add(eventData);// 添加到删除记录中
                            break;
                        }
                    }

                }

            }

            // 等待所有都处理完成
            executorTemplate.waitForResult();

            if (!CollectionUtils.isEmpty(removeDatas)) {
                eventDatas.removeAll(removeDatas);
            }
        } finally {
            if (executorTemplate != null) {
                executorTemplateGetter.release(executorTemplate);
            }
        }

    }

    public void setExtensionFactory(ExtensionFactory extensionFactory) {
        this.extensionFactory = extensionFactory;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setExecutorTemplateGetter(ExecutorTemplateGetter executorTemplateGetter) {
        this.executorTemplateGetter = executorTemplateGetter;
    }

}
