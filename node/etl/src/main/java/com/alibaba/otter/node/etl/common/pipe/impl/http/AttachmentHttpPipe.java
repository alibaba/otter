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

package com.alibaba.otter.node.etl.common.pipe.impl.http;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.io.EncryptedData;
import com.alibaba.otter.node.etl.common.io.download.DataRetriever;
import com.alibaba.otter.node.etl.common.pipe.PipeDataType;
import com.alibaba.otter.node.etl.common.pipe.exception.PipeException;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveBean;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveRetriverCallback;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.LazyFileInputStream;
import com.alibaba.otter.node.etl.load.loader.db.FileloadDumper;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 基于文件附件的http协议的管道
 * 
 * @author jianghang 2011-10-17 下午03:11:44
 * @version 4.0.0
 */
public class AttachmentHttpPipe extends AbstractHttpPipe<Object, HttpPipeKey> implements BeanFactoryAware {

    private static final Logger logger  = LoggerFactory.getLogger(AttachmentHttpPipe.class);
    private BeanFactory         beanFactory;
    private boolean             encrypt = false;

    @Override
    public HttpPipeKey put(Object data) throws PipeException {
        if (data instanceof FileBatch) {
            return archiveFile((FileBatch) data);
        } else {
            throw new IllegalArgumentException("error argument");
        }
    }

    public File get(HttpPipeKey key) throws PipeException {
        return unpackFile(key);
    }

    // 处理对应的附件
    private HttpPipeKey archiveFile(final FileBatch fileBatch) {
        // 处理构造对应的文件url
        String filename = buildFileName(fileBatch.getIdentity(), ClassUtils.getShortClassName(fileBatch.getClass()));
        File file = new File(htdocsDir, filename);
        // 压缩对应的文件数据
        List<FileData> fileDatas = fileBatch.getFiles();
        Pipeline pipeline = configClientService.findPipeline(fileBatch.getIdentity().getPipelineId());
        int poolSize = pipeline.getParameters().getFileLoadPoolSize();
        boolean useLocalFileMutliThread = pipeline.getParameters().getUseLocalFileMutliThread();
        ArchiveBean archiveBean = getArchiveBean();
        archiveBean.adjustPoolSize(poolSize);// 调整线程池大小
        archiveBean.setUseLocalFileMutliThread(useLocalFileMutliThread);// 设置是否启用local多线程同步
        boolean done = archiveBean.pack(file, fileDatas, new ArchiveRetriverCallback<FileData>() {

            public InputStream retrive(FileData fileData) {
                boolean miss = false;
                try {
                    if (StringUtils.isNotEmpty(fileData.getNameSpace())) {
                        throw new RuntimeException(fileData + " is not support!");
                    } else {
                        File source = new File(fileData.getPath());
                        if (source.exists() && source.isFile()) {
                            return new LazyFileInputStream(source);
                        } else {
                            miss = true;
                            return null;
                        }
                    }
                } finally {
                    if (miss && logger.isInfoEnabled()) {
                        MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                                String.valueOf(fileBatch.getIdentity().getPipelineId()));
                        logger.info(FileloadDumper.dumpMissFileDatas(fileBatch.getIdentity(), fileData));
                    }
                }

            }
        });

        if (done == false) {
            return null; // 直接返回
        }

        HttpPipeKey key = new HttpPipeKey();
        key.setUrl(remoteUrlBuilder.getUrl(fileBatch.getIdentity().getPipelineId(), filename));
        key.setDataType(PipeDataType.FILE_BATCH);
        key.setIdentity(fileBatch.getIdentity());
        if (encrypt || pipeline.getParameters().getUseFileEncrypt()) {
            // 加密处理
            EncryptedData encryptedData = encryptFile(file);
            key.setKey(encryptedData.getKey());
            key.setCrc(encryptedData.getCrc());
        }
        return key;
    }

    // 处理对应的附件
    private File unpackFile(HttpPipeKey key) {
        Pipeline pipeline = configClientService.findPipeline(key.getIdentity().getPipelineId());
        DataRetriever dataRetriever = dataRetrieverFactory.createRetriever(pipeline.getParameters().getRetriever(),
                                                                           key.getUrl(), downloadDir);
        File archiveFile = null;
        try {
            dataRetriever.connect();
            dataRetriever.doRetrieve();
            archiveFile = dataRetriever.getDataAsFile();
        } catch (Exception e) {
            dataRetriever.abort();
            throw new PipeException("download_error", e);
        } finally {
            dataRetriever.disconnect();
        }

        // 处理下有加密的数据
        if (StringUtils.isNotEmpty(key.getKey()) && StringUtils.isNotEmpty(key.getCrc())) {
            decodeFile(archiveFile, key.getKey(), key.getCrc());
        }

        // 去除末尾的.gzip后缀，做为解压目录
        String dir = StringUtils.removeEnd(archiveFile.getPath(),
                                           FilenameUtils.EXTENSION_SEPARATOR_STR
                                                   + FilenameUtils.getExtension(archiveFile.getPath()));
        File unpackDir = new File(dir);
        // 开始解压
        getArchiveBean().unpack(archiveFile, unpackDir);
        return unpackDir;
    }

    // 构造文件名
    private String buildFileName(Identity identity, String prefix) {
        Date now = new Date();
        String time = new SimpleDateFormat(DATE_FORMAT).format(now);
        return MessageFormat.format("{0}-{1}-{2}-{3}-{4}.gzip", prefix, time, String.valueOf(identity.getChannelId()),
                                    String.valueOf(identity.getPipelineId()), String.valueOf(identity.getProcessId()));
    }

    private ArchiveBean getArchiveBean() {
        // archiveBean做了池化处理，所以每次需要重容器里拿一次
        return (ArchiveBean) beanFactory.getBean("archiveBean", ArchiveBean.class);
    }

    // ================== setter / getter ===================

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

}
