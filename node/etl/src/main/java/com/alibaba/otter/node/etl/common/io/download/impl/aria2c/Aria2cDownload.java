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

package com.alibaba.otter.node.etl.common.io.download.impl.aria2c;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.alibaba.otter.node.etl.common.io.download.Download;
import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandDownload;
import com.alibaba.otter.shared.common.utils.cmd.Exec;

/**
 * 文档：http://aria2.sourceforge.net/
 * 
 * @author jianghang 2011-10-10 下午06:24:11
 * @version 4.0.0
 */
public class Aria2cDownload extends AbstractCommandDownload implements Aria2cConfig, Download {

    public Aria2cDownload(String cmdPath, String url, String dir){
        super(cmdPath, url, dir, null);
    }

    public Aria2cDownload(String cmdPath, String url, String dir, String[] params){
        super(cmdPath, url, dir, params);
    }

    @Override
    protected void buildCmd(String cmdPath, String[] params) {
        // 文件存在时，续传
        boolean retry = targetFile.exists();
        this.cmd = String.format("%s %s-o %s -d %s -l %s/aria2c.log %s %s", cmdPath, retry ? "-c " : "",
                                 targetFile.getName(), this.targetDir, this.targetDir,
                                 StringUtils.join(((params == null) || (params.length == 0)) ? ARIA2C_PARAM : params,
                                                  ' '), url);
    }

    protected void analyzeResult(Exec.Result result) {
        String[] results = StringUtils.split(result.getStdout(), SystemUtils.LINE_SEPARATOR);
        List<Aria2cStat> segmentStat = new ArrayList<Aria2cStat>();
        int pos = 0;

        for (; pos < results.length; pos++) {
            if (true == results[pos].toLowerCase().startsWith("gid|stat")) {
                break;
            }
        }

        for (pos++; pos < results.length; pos++) {
            // 下载结束标志
            // Download Results:
            // gid|stat|avg speed  |path/URI
            // ===+====+===========+===========================================================
            // 1|  OK| 103.7KiB/s|./index.html
            //
            // Status Legend:
            // (OK):download completed.
            if (true == StringUtils.isNumeric(results[pos].substring(0, 1))) {

                // 分析各个下载块的完成状态
                String[] status = StringUtils.split(results[pos], " \t|");

                if (status.length > 2) {
                    if (StringUtils.equalsIgnoreCase(Aria2cStat.OK.name(), status[1])) {
                        segmentStat.add(Aria2cStat.OK);
                    } else if (StringUtils.equalsIgnoreCase(Aria2cStat.ERR.name(), status[1])) {
                        segmentStat.add(Aria2cStat.ERR);
                    } else if (StringUtils.equalsIgnoreCase(Aria2cStat.INPR.name(), status[1])) {
                        segmentStat.add(Aria2cStat.INPR);
                    }

                    logger.warn(results[pos]);
                } else {
                    logger.error("it seems aria2 changed it's status format: " + results[pos]);
                }
            }
        }

        int size = segmentStat.size();
        int errCount = 0;
        int inprCount = 0;
        for (int i = 0; i < size; i++) {
            final Aria2cStat stat = segmentStat.get(i);

            if (Aria2cStat.ERR == stat) {
                errCount++;
            } else if (Aria2cStat.INPR == stat) {
                inprCount++;
            }
        }

        // 如果没有异常，则有可能下载模块全部成功或者部分被异常终止
        if (errCount == 0) {
            if (inprCount == 0) {
                // 没有异常，表示下载成功
                completed.set(true);
            } else {
                // 部分被异常终止，可以续传
                paused.set(true);
            }
        } else {
            if (errCount == size) {
                // 如果全部下载失败，则存在参数异常
                aborted.set(true);
            } else {
                // 部分成功，可以续传
                paused.set(true);
            }
        }
    }
}
