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

package com.alibaba.otter.node.etl.common.io.download.impl;

import java.io.File;
import java.io.IOException;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.node.etl.common.io.download.DataRetriever;
import com.alibaba.otter.node.etl.common.io.download.exception.DataRetrieveException;
import com.alibaba.otter.node.etl.common.io.download.impl.observer.DefaultExceptionObserver;
import com.alibaba.otter.node.etl.common.io.download.impl.observer.DefaultProgressObserver;
import com.alibaba.otter.node.etl.common.io.download.impl.observer.DefaultStatusObserver;
import com.alibaba.otter.shared.common.utils.NioUtils;

/**
 * 基于命令行工具的下载
 * 
 * @author jianghang 2011-10-10 下午05:43:24
 * @version 4.0.0
 */
public abstract class AbstractCommandRetriever implements DataRetriever {

    protected final Logger            logger = LoggerFactory.getLogger(getClass());
    protected AbstractCommandDownload download;

    public AbstractCommandRetriever(String cmdPath, String url, String targetDir){
        this.buildDownload(cmdPath, url, targetDir, null);
    }

    public AbstractCommandRetriever(String cmdPath, String url, String targetDir, String[] params){
        this.buildDownload(cmdPath, url, targetDir, params);
    }

    protected abstract void buildDownload(String cmdPath, String url, String targetDir, String[] params);

    public void connect() throws DataRetrieveException {
        this.download.addObserver(new DefaultStatusObserver(logger));
        this.download.addObserver(new DefaultProgressObserver(logger));
        this.download.addObserver(new DefaultExceptionObserver(logger));
    }

    public void doRetrieve() throws DataRetrieveException {
        try {
            download.download();
        } catch (IOException e) {
            //ignore
        }

        if (download.isCompleted()) {
            //
        } else if (download.isPaused()) {
            throw new DataRetrieveException("retry 3 times still have err, paused.");
        } else if (download.isAborted()) {
            throw new DataRetrieveException("aborted for some configration error.");
        }
    }

    public void disconnect() {
        this.download = null;
    }

    public void abort() {
        this.download = null;
    }

    public void addObserver(Observer o) {
        this.download.addObserver(o);
    }

    public long getContentLength() throws IOException {
        if (download.isCompleted()) {
            return download.getAssociatedLocalFile().length();
        }

        return -1;
    }

    public byte[] getDataAsByteArray() {
        try {
            return NioUtils.read(this.getDataAsFile());
        } catch (IOException e) {
            throw new DataRetrieveException(e);
        }
    }

    public File getDataAsFile() {
        return download.getAssociatedLocalFile();
    }

    public boolean isDataAvailable() {
        return download.isCompleted();
    }
}
