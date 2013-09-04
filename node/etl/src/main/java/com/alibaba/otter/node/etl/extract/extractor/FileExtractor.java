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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.extension.ExtensionFactory;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplate;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplateGetter;
import com.alibaba.otter.shared.etl.extend.fileresolver.FileInfo;
import com.alibaba.otter.shared.etl.extend.fileresolver.FileResolver;
import com.alibaba.otter.shared.etl.extend.fileresolver.support.RemoteDirectoryFetcher;
import com.alibaba.otter.shared.etl.extend.fileresolver.support.RemoteDirectoryFetcherAware;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * 基于rowBatch数据，返回对应的关联文件
 * 
 * @author jianghang 2012-4-18 下午04:52:00
 * @version 4.0.2
 */
public class FileExtractor extends AbstractExtractor<DbBatch> {

    private static final Logger    logger = LoggerFactory.getLogger(ExecutorTemplate.class);
    private ExtensionFactory       extensionFactory;

    private RemoteDirectoryFetcher arandaRemoteDirectoryFetcher;
    private int                    retry  = 3;
    private ExecutorTemplateGetter executorTemplateGetter;

    public void extract(DbBatch dbBatch) throws ExtractException {
        List<FileData> fileDatas = doFileExtract(dbBatch.getRowBatch());
        FileBatch fileBatch = new FileBatch();
        fileBatch.setFiles(fileDatas);
        Identity identity = new Identity();
        identity.setChannelId(dbBatch.getRowBatch().getIdentity().getChannelId());
        identity.setPipelineId(dbBatch.getRowBatch().getIdentity().getPipelineId());
        identity.setProcessId(dbBatch.getRowBatch().getIdentity().getProcessId());
        fileBatch.setIdentity(identity);
        dbBatch.setFileBatch(fileBatch);
    }

    /**
     * 返回这批变更数据对应的FileInfo.
     * 
     * @param rowBatch
     * @return
     */
    private List<FileData> doFileExtract(RowBatch rowBatch) {
        List<FileData> fileDatas = new ArrayList<FileData>();
        // 处理数据
        Pipeline pipeline = getPipeline(rowBatch.getIdentity().getPipelineId());
        List<EventData> eventDatas = rowBatch.getDatas();
        for (EventData eventData : eventDatas) {
            if (eventData.getEventType().isDdl()) {
                continue;
            }

            List<DataMediaPair> dataMediaPairs = ConfigHelper.findDataMediaPairByMediaId(pipeline,
                                                                                         eventData.getTableId());
            if (dataMediaPairs == null) {
                throw new ExtractException("ERROR ## the dataMediaId = " + eventData.getTableId()
                                           + " dataMediaPair is null,please check");
            }

            for (DataMediaPair dataMediaPair : dataMediaPairs) {
                if (dataMediaPair.getResolverData() == null
                    || dataMediaPair.getResolverData().getExtensionDataType() == null
                    || (dataMediaPair.getResolverData().getExtensionDataType().isClazz() && StringUtils.isBlank(dataMediaPair.getResolverData().getClazzPath()))
                    || (dataMediaPair.getResolverData().getExtensionDataType().isSource() && StringUtils.isBlank(dataMediaPair.getResolverData().getSourceText()))) {
                    continue;
                }

                FileResolver fileResolver = null;

                if (dataMediaPair.getResolverData() != null) {
                    fileResolver = extensionFactory.getExtension(FileResolver.class, dataMediaPair.getResolverData());
                } else {
                    continue;
                }

                if (fileResolver == null) {
                    throw new ExtractException("ERROR ## the dataMediaId = " + eventData.getTableId()
                                               + " the fileResolver className  = "
                                               + dataMediaPair.getResolverData().getClazzPath()
                                               + " is null ,please check the class");
                }

                if (fileResolver instanceof RemoteDirectoryFetcherAware) {
                    RemoteDirectoryFetcherAware remoteDirectoryFetcherAware = (RemoteDirectoryFetcherAware) fileResolver;
                    remoteDirectoryFetcherAware.setRemoteDirectoryFetcher(arandaRemoteDirectoryFetcher);
                }

                List<FileData> singleRowFileDatas = getSingleRowFileInfos(dataMediaPair.getId(), fileResolver,
                                                                          eventData);
                // 做一下去重处理
                for (FileData data : singleRowFileDatas) {
                    if (!fileDatas.contains(data)) {
                        fileDatas.add(data);
                    }
                }
            }
        }

        // 判断是否需要进行图片重复同步检查
        if (pipeline.getParameters().getFileDetect()) {
            doFileDetectCollector(pipeline, fileDatas);
        }
        return fileDatas;
    }

    private List<FileData> getSingleRowFileInfos(long pairId, FileResolver fileResolver, EventData eventData) {
        if (eventData.getEventType() == EventType.DELETE && fileResolver.isDeleteRequired() == false) {
            return new ArrayList<FileData>();
        }

        Map<String, String> rowMap = new HashMap<String, String>();

        List<EventColumn> keyColumns = eventData.getKeys();
        List<EventColumn> eventColumns = eventData.getUpdatedColumns();
        for (EventColumn eventColumn : keyColumns) {
            rowMap.put(eventColumn.getColumnName().toUpperCase(), eventColumn.getColumnValue());
        }
        for (EventColumn eventColumn : eventColumns) {
            rowMap.put(eventColumn.getColumnName().toUpperCase(), eventColumn.getColumnValue());
        }
        FileInfo[] fileInfos = fileResolver.getFileInfo(rowMap);
        if (fileInfos == null || fileInfos.length == 0) {
            return new ArrayList<FileData>();
        } else {
            List<FileData> fileDatas = new ArrayList<FileData>();
            for (FileInfo fileInfo : fileInfos) {
                FileData fileData = new FileData();
                fileData.setPairId(pairId); // 记录一下具体映射规则的id
                fileData.setTableId(eventData.getTableId());
                fileData.setEventType(eventData.getEventType());
                fileData.setLastModifiedTime(fileInfo.getLastModifiedTime());
                fileData.setNameSpace(fileInfo.getNamespace());
                fileData.setPath(fileInfo.getPath());
                fileData.setSize(fileInfo.getSize());
                fileDatas.add(fileData);
            }
            return fileDatas;
        }
    }

    private void doFileDetectCollector(Pipeline pipeline, List<FileData> fileDatas) {
        ExecutorTemplate executorTemplate = executorTemplateGetter.get();
        try {
            executorTemplate.start();
            // 重新设置下poolSize
            executorTemplate.adjustPoolSize(pipeline.getParameters().getFileLoadPoolSize());
            for (final FileData fileData : fileDatas) {
                // 提交进行多线程处理
                executorTemplate.submit(new Runnable() {

                    public void run() {
                        boolean isAranda = StringUtils.isNotEmpty(fileData.getNameSpace());
                        int count = 0;
                        Throwable exception = null;
                        while (count++ < retry) {
                            try {
                                if (isAranda) {
                                    // remote file
                                    throw new RuntimeException(fileData + " is not support!");
                                } else {
                                    // 处理本地文件
                                    File file = new File(fileData.getPath());
                                    fileData.setLastModifiedTime(file.lastModified());
                                    fileData.setSize(file.length());
                                }

                                return;// 没有异常就退出
                            } catch (Exception e) {
                                fileData.setLastModifiedTime(Long.MIN_VALUE);
                                fileData.setSize(Long.MIN_VALUE);
                                exception = e;
                            }
                        }

                        if (count >= retry) {
                            logger.warn(String.format("FileDetectCollector is error! collect failed[%s]",
                                                      fileData.getNameSpace() + "/" + fileData.getPath()), exception);
                        }
                    }
                });
            }

            long start = System.currentTimeMillis();
            logger.info("start pipelinep[{}] waitFor FileData Size : {} ", pipeline.getId(), fileDatas.size());
            // 等待所有都处理完成
            executorTemplate.waitForResult();
            logger.info("end pipelinep[{}] waitFor FileData cost : {} ms ", pipeline.getId(),
                        (System.currentTimeMillis() - start));
        } finally {
            if (executorTemplate != null) {
                executorTemplateGetter.release(executorTemplate);
            }
        }
    }

    // ==================== setter / getter =====================

    public void setExtensionFactory(ExtensionFactory extensionFactory) {
        this.extensionFactory = extensionFactory;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setArandaRemoteDirectoryFetcher(RemoteDirectoryFetcher arandaRemoteDirectoryFetcher) {
        this.arandaRemoteDirectoryFetcher = arandaRemoteDirectoryFetcher;
    }

    public void setExecutorTemplateGetter(ExecutorTemplateGetter executorTemplateGetter) {
        this.executorTemplateGetter = executorTemplateGetter;
    }

}
