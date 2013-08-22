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

package com.alibaba.otter.node.etl.common.pipe.impl.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.node.etl.common.pipe.PipeDataType;
import com.alibaba.otter.node.etl.common.pipe.exception.PipeException;
import com.alibaba.otter.node.etl.load.loader.db.FileloadDumper;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

public class RowDataMemoryPipe extends AbstractMemoryPipe<DbBatch, MemoryPipeKey> {

    private static final Logger logger      = LoggerFactory.getLogger(RowDataMemoryPipe.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private int                 retry       = 3;
    private String              downloadDir;

    public MemoryPipeKey put(DbBatch data) {
        MemoryPipeKey key = new MemoryPipeKey();
        key.setIdentity(data.getRowBatch().getIdentity());
        // if (data.getRoot() == null && data.getFileBatch() != null
        // && !CollectionUtils.isEmpty(data.getFileBatch().getFiles())) {
        // logger.warn("Identity[{}] memory pipe exist fileBatch!",
        // key.getIdentity());
        // // data.setRoot(prepareFile(data.getFileBatch()));
        // }
        key.setDataType(PipeDataType.DB_BATCH);
        cache.put(key, data);
        return key;
    }

    public DbBatch get(MemoryPipeKey key) {
        return cache.remove(key);
    }

    // 处理对应的附件
    @SuppressWarnings("unused")
    private File prepareFile(FileBatch fileBatch) {
        // 处理构造对应的文件url
        String dirname = buildFileName(fileBatch.getIdentity(), ClassUtils.getShortClassName(fileBatch.getClass()));
        File dir = new File(downloadDir, dirname);
        NioUtils.create(dir, false, 3);// 创建父目录
        // 压缩对应的文件数据
        List<FileData> fileDatas = fileBatch.getFiles();

        for (FileData fileData : fileDatas) {
            String namespace = fileData.getNameSpace();
            String path = fileData.getPath();
            boolean isLocal = StringUtils.isBlank(namespace);
            String entryName = null;
            if (true == isLocal) {
                entryName = FilenameUtils.getPath(path) + FilenameUtils.getName(path);
            } else {
                entryName = namespace + File.separator + path;
            }

            InputStream input = retrive(fileBatch.getIdentity(), fileData);
            if (input == null) {
                continue;
            }
            File entry = new File(dir, entryName);
            NioUtils.create(entry.getParentFile(), false, retry);// 尝试创建父路径
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(entry);
                NioUtils.copy(input, output);// 输出到压缩流中
            } catch (Exception e) {
                throw new PipeException("prepareFile error for file[" + entry.getPath() + "]");
            } finally {
                IOUtils.closeQuietly(output);
            }
        }

        return dir;
    }

    private InputStream retrive(Identity identity, FileData fileData) {
        boolean miss = false;
        try {
            if (StringUtils.isNotEmpty(fileData.getNameSpace())) {
                throw new RuntimeException(fileData + " is not support!");
            } else {
                try {
                    File source = new File(fileData.getPath());
                    if (source.exists() && source.isFile()) {
                        return new FileInputStream(source);
                    } else {
                        miss = true;
                        return null;
                    }
                } catch (FileNotFoundException ex) {
                    miss = true;
                    return null;
                }
            }
        } finally {
            if (miss) {
                logger.error(FileloadDumper.dumpMissFileDatas(identity, fileData));
            }
        }
    }

    // 构造文件名
    private String buildFileName(Identity identity, String prefix) {
        Date now = new Date();
        String time = new SimpleDateFormat(DATE_FORMAT).format(now);
        return MessageFormat.format("{0}-{1}-{2}-{3}-{4}", prefix, time, String.valueOf(identity.getChannelId()),
                                    String.valueOf(identity.getPipelineId()), String.valueOf(identity.getProcessId()));
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(downloadDir);
        NioUtils.create(new File(downloadDir), false, 3);
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

}
