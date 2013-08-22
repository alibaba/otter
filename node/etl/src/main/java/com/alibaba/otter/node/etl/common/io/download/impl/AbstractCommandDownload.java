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
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.node.etl.common.io.download.Download;
import com.alibaba.otter.node.etl.common.io.download.DownloadStatus;
import com.alibaba.otter.node.etl.common.io.download.exception.DataRetrieveException;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.common.utils.cmd.Exec;

public abstract class AbstractCommandDownload extends Observable implements Download {

    protected final Logger   logger = LoggerFactory.getLogger(getClass());
    protected AtomicBoolean  aborted;
    protected String         cmd;
    protected AtomicBoolean  completed;
    protected AtomicBoolean  paused;
    protected DownloadStatus status;
    protected String         targetDir;
    protected File           targetFile;
    protected String         url;

    public AbstractCommandDownload(String cmdPath, String url, String dir, String[] params){
        this.url = url;
        // 下载文件名
        String fileName = url.substring(url.lastIndexOf("/") + 1).replace("%20", " ");
        // 下载到本地目录
        this.targetDir = dir;
        // 目标文件
        this.targetFile = new File(this.targetDir, fileName);
        this.buildCmd(cmdPath, params);
        // 下载完成状态
        completed = new AtomicBoolean(false);
        // 无法下载
        aborted = new AtomicBoolean(false);
        // 下载发生异常, 可断点恢复
        paused = new AtomicBoolean(false);
        // idle
        status = DownloadStatus.IDLE;
    }

    public void download() throws IOException {
        // 准备下载
        notifyStatusChange(DownloadStatus.CONNECTING);
        Exec.Result result = null;
        try {
            result = Exec.execute(cmd);
            if (false == this.isSuccess(result.getExitCode())) {
                aborted.set(true);
                notifyException(new DataRetrieveException(result.toString()));
                notifyStatusChange(DownloadStatus.EXCEPTION);
            } else {
                this.analyzeResult(result);
                this.notifyMessage(result.toString());

                if (aborted.get()) {
                    // 中断
                    notifyStatusChange(DownloadStatus.ABORT);
                } else if (paused.get()) {
                    // 暂停
                    notifyStatusChange(DownloadStatus.PAUSED);
                } else {
                    // 下载完成
                    notifyStatusChange(DownloadStatus.COMPLETE);
                }
            }
        } catch (Exception ex) {
            aborted.set(true);
            notifyException(new DataRetrieveException((result != null) ? ex.getMessage() + SystemUtils.LINE_SEPARATOR
                                                                         + result.toString() : ex.getMessage(), ex));
        }

    }

    protected abstract void buildCmd(String cmdPath, String[] params);

    protected abstract void analyzeResult(Exec.Result result);

    protected boolean isSuccess(int exitValue) {
        return exitValue == 0;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public boolean isPaused() {
        return paused.get();
    }

    public boolean isAborted() {
        return aborted.get();
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public String getUrl() {
        return url;
    }

    public byte[] getAssociatedMemoryData() {
        if (this.targetFile.exists()) {
            try {
                return NioUtils.read(this.targetFile);
            } catch (IOException e) {
                throw new DataRetrieveException(e);
            }
        } else {
            return new byte[] { (byte) 0 };
        }
    }

    public long getContentLength() {
        if (this.targetFile.exists()) {
            return this.targetFile.length();
        }
        return 0;
    }

    public File getAssociatedLocalFile() {
        return this.targetFile;
    }

    protected void notifyMessage(String msg) {
        notifyEvent(msg);
    }

    protected void notifyStatusChange(DownloadStatus status) {
        this.status = status;
        notifyEvent(status);
    }

    protected void notifyException(Exception ex) {
        notifyEvent(ex);
    }

    protected void notifyEvent(Object arg) {
        setChanged();
        notifyObservers(arg);
    }
}
