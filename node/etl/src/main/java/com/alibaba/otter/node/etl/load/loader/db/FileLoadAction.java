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

package com.alibaba.otter.node.etl.load.loader.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker.LoadCounter;
import com.alibaba.otter.node.etl.load.loader.db.context.FileLoadContext;
import com.alibaba.otter.node.etl.load.loader.weight.WeightBuckets;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 处理文件load
 * 
 * @author jianghang 2011-10-31 上午11:33:22
 * @author zebinxu 2012-4-28 下午3:39:17 将每个权重的 file load 做成多线程
 * @version 4.0.0
 */
public class FileLoadAction implements InitializingBean, DisposableBean {

    private static final Logger logger             = LoggerFactory.getLogger(FileLoadAction.class);
    private int                 retry              = 5;
    private ConfigClientService configClientService;
    private LoadStatsTracker    loadStatsTracker;
    private boolean             dump               = true;

    // for concurrent file load
    private static final String WORKER_NAME        = "FileLoadAction";
    private static final String WORKER_NAME_FORMAT = "pipelineId = %s , pipelineName = %s , " + WORKER_NAME;
    private static final int    DEFAULT_POOL_SIZE  = 5;
    private int                 poolSize           = DEFAULT_POOL_SIZE;
    private ExecutorService     executor;

    /**
     * 返回结果为已处理成功的记录
     */
    public FileLoadContext load(FileBatch fileBatch, File rootDir, WeightController controller) {
        if (false == rootDir.exists()) {
            throw new LoadException(rootDir.getPath() + " is not exist");
        }
        FileLoadContext context = buildContext(fileBatch.getIdentity());
        context.setPrepareDatas(fileBatch.getFiles());
        boolean isDryRun = context.getPipeline().getParameters().isDryRun();
        try {
            // 复制成功的文件信息
            WeightBuckets<FileData> buckets = buildWeightBuckets(fileBatch.getIdentity(), fileBatch.getFiles());
            List<Long> weights = buckets.weights();
            controller.start(weights);
            // 处理数据
            for (int i = 0; i < weights.size(); i++) {
                Long weight = weights.get(i);
                controller.await(weight.intValue());
                if (logger.isInfoEnabled()) {
                    logger.debug("##start load for weight:{}\n", weight);
                }

                // 处理同一个weight下的数据
                List<FileData> items = buckets.getItems(weight);
                if (context.getPipeline().getParameters().isDryRun()) {
                    dryRun(context, items, rootDir);
                } else {
                    moveFiles(context, items, rootDir);
                }

                controller.single(weight.intValue());
                if (logger.isInfoEnabled()) {
                    logger.debug("##end load for weight:{}\n", weight);
                }
            }

            if (dump || isDryRun) {
                MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                        String.valueOf(fileBatch.getIdentity().getPipelineId()));
                logger.info(FileloadDumper.dumpContext("successed", context));
                MDC.remove(OtterConstants.splitPipelineLoadLogFileKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (dump || isDryRun) {
                MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                        String.valueOf(fileBatch.getIdentity().getPipelineId()));
                logger.info(FileloadDumper.dumpContext("error", context));
                MDC.remove(OtterConstants.splitPipelineLoadLogFileKey);
            }
        } catch (Exception e) {
            if (dump || isDryRun) {
                MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                        String.valueOf(fileBatch.getIdentity().getPipelineId()));
                logger.info(FileloadDumper.dumpContext("error", context));
                MDC.remove(OtterConstants.splitPipelineLoadLogFileKey);
            }
            throw new LoadException(e);
        } finally {

            // 不论是否移动成功，删除临时目录
            NioUtils.delete(rootDir, 3);
        }

        return context;
    }

    private void adjustPoolSize(FileLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        int newPoolSize = pipeline.getParameters().getFileLoadPoolSize();
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }

    }

    private FileLoadContext buildContext(Identity identity) {
        FileLoadContext context = new FileLoadContext();
        context.setIdentity(identity);
        Channel channel = configClientService.findChannel(identity.getChannelId());
        Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
        context.setChannel(channel);
        context.setPipeline(pipeline);
        return context;
    }

    /**
     * 构建基于weight权重分组的item集合列表
     */
    private WeightBuckets<FileData> buildWeightBuckets(Identity identity, List<FileData> datas) {
        WeightBuckets<FileData> buckets = new WeightBuckets<FileData>();
        for (FileData data : datas) {
            // 获取对应的weight
            DataMediaPair pair = ConfigHelper.findDataMediaPair(getPipeline(identity), data.getPairId());
            buckets.addItem(pair.getPushWeight(), data);
        }

        return buckets;
    }

    private Pipeline getPipeline(Identity identity) {
        return configClientService.findPipeline(identity.getPipelineId());
    }

    private void dryRun(FileLoadContext context, List<FileData> fileDatas, File rootDir) {
        for (FileData fileData : fileDatas) {
            boolean isLocal = StringUtils.isBlank(fileData.getNameSpace());
            String entryName = null;
            if (true == isLocal) {
                entryName = FilenameUtils.getPath(fileData.getPath()) + FilenameUtils.getName(fileData.getPath());
            } else {
                entryName = fileData.getNameSpace() + File.separator + fileData.getPath();
            }
            File sourceFile = new File(rootDir, entryName);
            if (true == sourceFile.exists() && false == sourceFile.isDirectory()) {
                if (false == isLocal) {
                    throw new LoadException(fileData + " is not support!");
                } else {
                    // 记录一下文件的meta信息
                    fileData.setSize(sourceFile.length());
                    fileData.setLastModifiedTime(sourceFile.lastModified());
                    context.getProcessedDatas().add(fileData);
                }

                LoadCounter counter = loadStatsTracker.getStat(context.getIdentity()).getStat(fileData.getPairId());
                counter.getFileCount().incrementAndGet();
                counter.getFileSize().addAndGet(fileData.getSize());
            } else if (fileData.getEventType().isDelete()) {
                // 删除对应的文件
                if (false == isLocal) {
                    throw new LoadException(fileData + " is not support!");
                } else {
                    context.getProcessedDatas().add(fileData);
                }
            } else {
                context.getFailedDatas().add(fileData);// 失败记录
            }
        }
    }

    /**
     * 多线程处理文件加载，使用 fast-fail 策略
     */
    private void moveFiles(FileLoadContext context, List<FileData> fileDatas, File rootDir) {
        Exception exception = null;
        adjustPoolSize(context);
        ExecutorCompletionService<Exception> executorComplition = new ExecutorCompletionService<Exception>(executor);

        List<Future<Exception>> results = new ArrayList<Future<Exception>>();
        for (FileData fileData : fileDatas) {
            Future<Exception> future = executorComplition.submit(new FileLoadWorker(context, rootDir, fileData));
            results.add(future);

            // fast fail
            if (future.isDone()) { // 如果是自己执行的任务(线程池采用 CallerRunsPolicy)，则立刻进行检查
                try {
                    exception = future.get();
                } catch (Exception e) {
                    exception = e;
                }
                if (exception != null) {
                    for (Future<Exception> result : results) {
                        if (!result.isDone() && !result.isCancelled()) {
                            result.cancel(true);
                        }
                    }
                    throw exception instanceof LoadException ? (LoadException) exception : new LoadException(exception);
                }
            }

        }

        int resultSize = results.size();
        int cursor = 0;
        while (cursor < resultSize) {
            try {
                Future<Exception> result = executorComplition.take();
                exception = result.get();
            } catch (Exception e) {
                exception = e;
                break;
            }
            cursor++;
        }

        if (cursor != resultSize) { // 发现任务出错，立刻把正在进行的任务取消
            for (Future<Exception> future : results) {
                if (!future.isDone() && !future.isCancelled()) {
                    future.cancel(true);
                }
            }

        }

        if (exception != null) {
            throw exception instanceof LoadException ? (LoadException) exception : new LoadException(exception);
        }
    }

    private class FileLoadWorker implements Callable<Exception> {

        private FileLoadContext context;
        private File            rootDir;
        private FileData        fileData;

        public FileLoadWorker(FileLoadContext context, File rootDir, FileData fileData){
            this.context = context;
            this.rootDir = rootDir;
            this.fileData = fileData;

        }

        public Exception call() throws Exception {
            Thread.currentThread().setName(String.format(WORKER_NAME_FORMAT, context.getPipeline().getId(),
                                                         context.getPipeline().getName()));
            try {
                MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(context.getPipeline().getId()));
                if (fileData == null) {
                    return null;
                }
                // 进行重试处理
                int count = 0;
                Throwable exception = null;
                while (count++ < retry) {
                    try {
                        doMove(context, rootDir, fileData);
                        return null;
                    } catch (Exception e) {
                        exception = e;
                        if (count < retry) {
                            Thread.sleep(50);
                        }
                    }
                }

                throw new LoadException(String.format("FileLoadWorker is error! createFile failed[%s]",
                                                      fileData.getPath()), exception);
            } finally {
                MDC.remove(OtterConstants.splitPipelineLogFileKey);
            }
        }
    }

    private void doMove(FileLoadContext context, File rootDir, FileData fileData) throws IOException {
        boolean isLocal = StringUtils.isBlank(fileData.getNameSpace());
        String entryName = null;
        if (true == isLocal) {
            entryName = FilenameUtils.getPath(fileData.getPath()) + FilenameUtils.getName(fileData.getPath());
        } else {
            entryName = fileData.getNameSpace() + File.separator + fileData.getPath();
        }
        File sourceFile = new File(rootDir, entryName);
        if (true == sourceFile.exists() && false == sourceFile.isDirectory()) {
            if (false == isLocal) {
                throw new LoadException(fileData + " is not support!");
            } else {
                File targetFile = new File(fileData.getPath());
                // copy to product path
                NioUtils.copy(sourceFile, targetFile, retry);
                if (true == targetFile.exists()) {
                    // 记录一下文件的meta信息
                    fileData.setSize(sourceFile.length());
                    fileData.setLastModifiedTime(sourceFile.lastModified());
                    context.getProcessedDatas().add(fileData);
                } else {
                    throw new LoadException(String.format("copy/rename [%s] to [%s] failed by unknow reason",
                                                          sourceFile.getPath(), targetFile.getPath()));
                }

            }

            LoadCounter counter = loadStatsTracker.getStat(context.getIdentity()).getStat(fileData.getPairId());
            counter.getFileCount().incrementAndGet();
            counter.getFileSize().addAndGet(fileData.getSize());
        } else if (fileData.getEventType().isDelete()) {
            // 删除对应的文件
            if (false == isLocal) {
                throw new LoadException(fileData + " is not support!");
            } else {
                File targetFile = new File(fileData.getPath());
                if (NioUtils.delete(targetFile, retry)) {
                    context.getProcessedDatas().add(fileData);
                } else {
                    context.getFailedDatas().add(fileData);
                }
            }
        } else {
            context.getFailedDatas().add(fileData);// 失败记录
        }

    }

    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                                          new ArrayBlockingQueue<Runnable>(poolSize * 4),
                                          new NamedThreadFactory(WORKER_NAME),
                                          new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    // ====================== setter / getter ===========================

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setLoadStatsTracker(LoadStatsTracker loadStatsTracker) {
        this.loadStatsTracker = loadStatsTracker;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

}
