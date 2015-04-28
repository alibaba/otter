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

package com.alibaba.otter.node.etl.common.pipe.impl.http.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.Deflater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.FileData;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * 文档归档压缩/解压工具
 * 
 * <pre>
 * 优化思路：
 * 1. 针对网络服务文件，考虑使用多线程进行数据获取 (压缩效率 << 获取网络文件数据I/O latency)，提前获取网络文件
 * 2. 针对本地文件，直接进行数据流压缩
 * 
 * </pre>
 * 
 * @author jianghang 2011-10-11 下午04:44:07
 * @version 4.0.0
 */
public class ArchiveBean implements InitializingBean, DisposableBean {

    private static final int    DEFAULT_POOL_SIZE       = 5;
    private static final String WORKER_NAME             = "AttachmentHttpPipe";
    private int                 poolSize                = DEFAULT_POOL_SIZE;
    private ExecutorService     executor;
    private int                 retry                   = 3;
    private boolean             useLocalFileMutliThread = true;

    public static class ArchiveEntry {

        private String      name;
        private File        localFile = null;
        private InputStream stream    = null;

        public ArchiveEntry(String name){
            this.name = name;
        }

        public ArchiveEntry(String name, InputStream stream){
            this.name = name;
            this.stream = stream;
        }

        public ArchiveEntry(String name, File localFile){
            this.name = name;
            this.localFile = localFile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InputStream getStream() {
            if (localFile != null) {
                try {
                    return new FileInputStream(localFile);
                } catch (FileNotFoundException e) {
                    throw new ArchiveException(e);
                }
            } else {
                return stream;
            }
        }

        public void setStream(InputStream stream) {
            this.stream = stream;
        }

    }

    /**
     * 将对应的FileData数据的文件，压缩到指定的目标targetArchiveFile上 <br/>
     * 需要进行retry处理，解决java.io.IOException: Input/output error
     */
    public boolean pack(final File targetArchiveFile, List<FileData> fileDatas,
                        final ArchiveRetriverCallback<FileData> callback) throws ArchiveException {
        int count = 0;
        Exception exception = null;
        while (++count <= retry) {
            try {
                return doPack(targetArchiveFile, fileDatas, callback);
            } catch (Exception ex) {
                exception = ex;
            }
        }

        throw new ArchiveException("pack fileDatas error!", exception);
    }

    /**
     * 执行压缩
     */
    @SuppressWarnings("resource")
    private boolean doPack(final File targetArchiveFile, List<FileData> fileDatas,
                           final ArchiveRetriverCallback<FileData> callback) {
        // 首先判断下对应的目标文件是否存在，如存在则执行删除
        if (true == targetArchiveFile.exists() && false == NioUtils.delete(targetArchiveFile, 3)) {
            throw new ArchiveException(String.format("[%s] exist and delete failed",
                targetArchiveFile.getAbsolutePath()));
        }

        boolean exist = false;
        ZipOutputStream zipOut = null;
        Set<String> entryNames = new HashSet<String>();
        BlockingQueue<Future<ArchiveEntry>> queue = new LinkedBlockingQueue<Future<ArchiveEntry>>(); // 下载成功的任务列表
        ExecutorCompletionService completionService = new ExecutorCompletionService(executor, queue);

        final File targetDir = new File(targetArchiveFile.getParentFile(),
            FilenameUtils.getBaseName(targetArchiveFile.getPath()));
        try {
            // 创建一个临时目录
            FileUtils.forceMkdir(targetDir);

            zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(targetArchiveFile)));
            zipOut.setLevel(Deflater.BEST_SPEED);
            // 进行并发压缩处理
            for (final FileData fileData : fileDatas) {
                if (fileData.getEventType().isDelete()) {
                    continue; // 忽略delete类型的数据打包，因为只需直接在目标进行删除
                }

                String namespace = fileData.getNameSpace();
                String path = fileData.getPath();
                boolean isLocal = StringUtils.isBlank(namespace);
                String entryName = null;
                if (true == isLocal) {
                    entryName = FilenameUtils.getPath(path) + FilenameUtils.getName(path);
                } else {
                    entryName = namespace + File.separator + path;
                }

                // 过滤一些重复的文件数据同步
                if (entryNames.contains(entryName) == false) {
                    entryNames.add(entryName);
                } else {
                    continue;
                }

                final String name = entryName;
                if (true == isLocal && !useLocalFileMutliThread) {
                    // 采用串行处理，不走临时文件
                    queue.add(new DummyFuture(new ArchiveEntry(name, callback.retrive(fileData))));
                } else {
                    completionService.submit(new Callable<ArchiveEntry>() {

                        public ArchiveEntry call() throws Exception {
                            // 处理下异常，可能失败
                            InputStream input = null;
                            OutputStream output = null;
                            try {
                                input = callback.retrive(fileData);

                                if (input instanceof LazyFileInputStream) {
                                    input = ((LazyFileInputStream) input).getInputSteam();// 获取原始的stream
                                }

                                if (input != null) {
                                    File tmp = new File(targetDir, name);
                                    NioUtils.create(tmp.getParentFile(), false, 3);// 尝试创建父路径
                                    output = new FileOutputStream(tmp);
                                    NioUtils.copy(input, output);// 拷贝到文件
                                    return new ArchiveEntry(name, new File(targetDir, name));
                                } else {
                                    return new ArchiveEntry(name);
                                }
                            } finally {
                                IOUtils.closeQuietly(input);
                                IOUtils.closeQuietly(output);
                            }
                        }
                    });
                }
            }

            for (int i = 0; i < entryNames.size(); i++) {
                // 读入流
                ArchiveEntry input = null;
                InputStream stream = null;
                try {
                    input = queue.take().get();
                    if (input == null) {
                        continue;
                    }

                    stream = input.getStream();
                    if (stream == null) {
                        continue;
                    }

                    if (stream instanceof LazyFileInputStream) {
                        stream = ((LazyFileInputStream) stream).getInputSteam();// 获取原始的stream
                    }

                    exist = true;
                    zipOut.putNextEntry(new ZipEntry(input.getName()));
                    NioUtils.copy(stream, zipOut);// 输出到压缩流中
                    zipOut.closeEntry();
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }

            if (exist) {
                zipOut.finish();
            }
        } catch (Exception e) {
            throw new ArchiveException(e);
        } finally {
            IOUtils.closeQuietly(zipOut);
            try {
                FileUtils.deleteDirectory(targetDir);// 删除临时目录
            } catch (IOException e) {
                // ignore
            }
        }

        return exist;
    }

    public List<File> unpack(File archiveFile, File targetDir) throws ArchiveException {
        // 首先判断下对应的目标文件是否存在，如存在则执行删除
        if (false == archiveFile.exists()) {
            throw new ArchiveException(String.format("[%s] not exist", archiveFile.getAbsolutePath()));
        }
        if (false == targetDir.exists() && false == NioUtils.create(targetDir, false, 3)) {
            throw new ArchiveException(String.format("[%s] not exist and create failed", targetDir.getAbsolutePath()));
        }

        List<File> result = new ArrayList<File>();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archiveFile);
            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                // entry
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                // target
                File targetFile = new File(targetDir, entryName);
                NioUtils.create(targetFile.getParentFile(), false, 3);// 尝试创建父路径
                InputStream input = null;
                OutputStream output = null;
                try {
                    output = new FileOutputStream(targetFile);
                    input = zipFile.getInputStream(entry);
                    NioUtils.copy(input, output);
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                }
                result.add(targetFile);
            }

        } catch (Exception e) {
            throw new ArchiveException(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                }
            }
        }

        return result;
    }

    private static class DummyFuture implements Future<ArchiveEntry> {

        private ArchiveEntry entry;

        public DummyFuture(ArchiveEntry entry){
            this.entry = entry;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public ArchiveEntry get() throws InterruptedException, ExecutionException {
            return entry;
        }

        public ArchiveEntry get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                                                            TimeoutException {
            return entry;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return false;
        }

    }

    // 调整一下线程池
    public void adjustPoolSize(int newPoolSize) {
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(poolSize,
            poolSize,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue(poolSize * 4),
            new NamedThreadFactory(WORKER_NAME),
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setUseLocalFileMutliThread(boolean useLocalFileMutliThread) {
        this.useLocalFileMutliThread = useLocalFileMutliThread;
    }

}
