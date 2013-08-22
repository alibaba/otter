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

package com.alibaba.otter.node.etl.common.io.download;

import java.io.File;
import java.io.IOException;
import java.util.Observer;

import com.alibaba.otter.node.etl.common.io.download.exception.DataRetrieveException;

/**
 * 数据下载接口
 * 
 * @author brave.taoy
 */
public interface DataRetriever {

    /**
     * This type of notification will be send if the progress changes.
     */
    public final static Integer NOTIFICATION_PROGRESS = 100;

    // control retriever

    /**
     * Advise the Retriever to connect to the data source.
     */
    public void connect() throws DataRetrieveException;

    /**
     * Do retrieve data
     */
    public void doRetrieve() throws DataRetrieveException;

    /**
     * Returns true if the data source contains any data to be read.
     */
    public boolean isDataAvailable() throws DataRetrieveException;

    /**
     * Returns an byte array for the retrieved data.
     */
    public byte[] getDataAsByteArray() throws DataRetrieveException;

    /**
     * Returns a file
     */
    public File getDataAsFile() throws DataRetrieveException;

    /**
     * Disconnect from the data source.
     */
    public void disconnect() throws DataRetrieveException;

    /**
     * Aborts the current retrieving.
     */
    public void abort();

    /**
     * Returns the content length in bytes.
     */
    public long getContentLength() throws IOException;

    public void addObserver(Observer o);
}
