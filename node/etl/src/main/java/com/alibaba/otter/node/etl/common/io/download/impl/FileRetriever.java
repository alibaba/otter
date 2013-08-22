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
import com.alibaba.otter.shared.common.utils.NioUtils;

/**
 * 从文件系统中获取数据
 * 
 * @author brave.taoy
 * @author jianghang 2011-10-10 下午06:14:46
 * @version 4.0.0
 */
public class FileRetriever implements DataRetriever {

    private static Logger logger = LoggerFactory.getLogger(FileRetriever.class);
    private boolean       mAbort = false;
    private long          mByteOffset;
    private File          mFile;

    public FileRetriever(File pFile){
        mFile = pFile;
    }

    public void abort() {
        mAbort = true;
    }

    public void connect() {
        if (mAbort) {
            throw new IllegalStateException("Retriever aborted");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Open file for retrieval: " + mFile);
        }
    }

    public void doRetrieve() {
    }

    public void disconnect() {
        if (logger.isDebugEnabled()) {
            logger.debug("Close file " + mFile.getAbsolutePath());
        }
    }

    public boolean isDataAvailable() {
        return mFile.exists();
    }

    public byte[] getDataAsByteArray() {
        try {
            return NioUtils.read(mFile);
        } catch (IOException e) {
            throw new DataRetrieveException(e);
        }
    }

    public File getDataAsFile() {
        return mFile;
    }

    public void setBytesToSkip(long pBytesToSkip) {
        mByteOffset = pBytesToSkip;
    }

    public long getBytesSkipped() {
        return mByteOffset;
    }

    public long getContentLength() {
        return mFile.length();
    }

    public void addObserver(Observer o) {
    }
}
