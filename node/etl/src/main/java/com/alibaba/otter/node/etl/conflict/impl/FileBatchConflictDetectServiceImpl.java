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

package com.alibaba.otter.node.etl.conflict.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.conflict.FileBatchConflictDetectService;
import com.alibaba.otter.node.etl.conflict.model.ConflictEventType;
import com.alibaba.otter.node.etl.conflict.model.FileConflictDetectEvent;
import com.alibaba.otter.node.etl.load.loader.db.FileloadDumper;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplate;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplateGetter;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;

/**
 * 提供文件的冲突检测服务
 * 
 * @author jianghang 2011-11-10 上午09:41:20
 * @version 4.0.0
 */
public class FileBatchConflictDetectServiceImpl implements FileBatchConflictDetectService {

    private static final Logger      logger = LoggerFactory.getLogger(FileBatchConflictDetectServiceImpl.class);
    private int                      retry  = 3;
    private ConfigClientService      configClientService;
    private NodeCommmunicationClient nodeCommmunicationClient;
    private ExecutorTemplateGetter   executorTemplateGetter;

    public FileBatchConflictDetectServiceImpl(){
        // 将自己注册为远程事件处理
        CommunicationRegistry.regist(ConflictEventType.fileConflictDetect, this);
    }

    public FileBatch detect(FileBatch fileBatch, Long targetNodeId) {
        FileConflictDetectEvent event = new FileConflictDetectEvent();
        event.setFileBatch(fileBatch);
        if (isLocal(targetNodeId)) {
            return onFileConflictDetect(event);
        } else {
            // 调用远程
            return (FileBatch) nodeCommmunicationClient.call(targetNodeId, event);
        }
    }

    /**
     * 具体冲突检测的行为
     */
    private FileBatch onFileConflictDetect(FileConflictDetectEvent event) {
        final FileBatch fileBatch = event.getFileBatch();
        if (CollectionUtils.isEmpty(fileBatch.getFiles())) {
            return fileBatch;
        }

        ExecutorTemplate executorTemplate = executorTemplateGetter.get();
        try {
            MDC.put(OtterConstants.splitPipelineLoadLogFileKey, String.valueOf(fileBatch.getIdentity().getPipelineId()));
            executorTemplate.start();
            // 重新设置下poolSize
            Pipeline pipeline = configClientService.findPipeline(fileBatch.getIdentity().getPipelineId());
            executorTemplate.adjustPoolSize(pipeline.getParameters().getFileLoadPoolSize());
            // 启动
            final List<FileData> result = Collections.synchronizedList(new ArrayList<FileData>());
            final List<FileData> filter = Collections.synchronizedList(new ArrayList<FileData>());
            for (final FileData source : fileBatch.getFiles()) {
                EventType type = source.getEventType();
                if (type.isDelete()) {
                    result.add(source);
                } else {
                    executorTemplate.submit(new Runnable() {

                        public void run() {
                            MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                                    String.valueOf(fileBatch.getIdentity().getPipelineId()));
                            // 处理更新类型
                            String namespace = source.getNameSpace();
                            String path = source.getPath();
                            FileData target = null;

                            int count = 0;
                            while (count++ < retry) {// 进行重试处理
                                try {
                                    if (true == StringUtils.isBlank(namespace)) {
                                        // local file
                                        java.io.File targetFile = new java.io.File(path);
                                        if (true == targetFile.exists()) {
                                            // modified time cost
                                            long lastModified = targetFile.lastModified();
                                            long size = targetFile.length();
                                            // 更新数据
                                            target = new FileData();
                                            target.setLastModifiedTime(lastModified);
                                            target.setSize(size);
                                        }
                                    } else {
                                        // remote file
                                        throw new RuntimeException(source + " is not support!");
                                    }

                                    break; // 不出异常就跳出
                                } catch (Exception ex) {
                                    target = null;
                                }
                            }

                            boolean shouldSync = false;
                            if (target != null) {
                                if (true == accept(target, source)) {
                                    shouldSync = true;
                                }
                            } else {
                                shouldSync = true;
                            }

                            if (true == shouldSync) {
                                result.add(source);
                            } else {
                                filter.add(source);
                            }
                        }
                    });
                }
            }
            // 等待所有都处理完成
            executorTemplate.waitForResult();

            if (pipeline.getParameters().getDumpEvent() && logger.isInfoEnabled()) {
                logger.info(FileloadDumper.dumpFilterFileDatas(fileBatch.getIdentity(), fileBatch.getFiles().size(),
                                                               result.size(), filter));
            }

            // 构造返回结果
            FileBatch target = new FileBatch();
            target.setIdentity(fileBatch.getIdentity());
            target.setFiles(result);
            return target;
        } finally {
            if (executorTemplate != null) {
                executorTemplateGetter.release(executorTemplate);
            }

            MDC.remove(OtterConstants.splitPipelineLoadLogFileKey);
        }
    }

    /**
     * <pre>
     * 判断规则：
     * 1. 源文件的最后修改时间比目标文件的最后修改时间新
     * 2. 源文件和目标文件大小不一致
     * </pre>
     */
    private boolean accept(FileData target, FileData source) {
        return (target.getLastModifiedTime() < source.getLastModifiedTime()) || (target.getSize() != source.getSize());
    }

    private boolean isLocal(Long targetNodeId) {
        return configClientService.currentNode().getId().equals(targetNodeId);
    }

    // ===================== setter / getter ======================

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setNodeCommmunicationClient(NodeCommmunicationClient nodeCommmunicationClient) {
        this.nodeCommmunicationClient = nodeCommmunicationClient;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setExecutorTemplateGetter(ExecutorTemplateGetter executorTemplateGetter) {
        this.executorTemplateGetter = executorTemplateGetter;
    }

}
