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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

import com.alibaba.otter.node.etl.load.loader.db.context.FileLoadContext;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * dumper 记录
 * 
 * @author jianghang 2011-12-28 上午11:19:10
 * @version 4.0.0
 */
public class FileloadDumper {

    private static final String SEP              = SystemUtils.LINE_SEPARATOR;

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    private static String       context_format   = null;
    private static String       miss_format      = null;
    private static String       filter_format    = null;

    static {
        context_format = SEP + "****************************************************" + SEP;
        context_format = "* status : {0}  , time : {1} *" + SEP;
        context_format += "* Identity : {2} *" + SEP;
        context_format += "* total Data : [{3}] , success Data : [{4}] , miss Data : [{5}] , Interrupt : [{6}]" + SEP;
        context_format += "****************************************************" + SEP;
        context_format += "* process file  *" + SEP;
        context_format += "{7}" + SEP;
        context_format += "* miss file *" + SEP;
        context_format += "{8}" + SEP;
        context_format += "****************************************************" + SEP;

        miss_format = SEP + "****************************************************" + SEP;
        miss_format += "* Identity : {0} *" + SEP;
        miss_format += "* miss : " + SEP;
        miss_format += "* {1}" + SEP;
        miss_format += "****************************************************";

        filter_format = SEP + "****************************************************" + SEP;
        filter_format += "* Identity : {0} *" + SEP;
        filter_format += "* input [{1}] , output [{2}] , filter [{3}] *" + SEP;
        filter_format += "* filters : " + SEP;
        filter_format += "* {4}" + SEP;
        filter_format += "****************************************************" + SEP;

    }

    public static String dumpContext(String status, FileLoadContext context) {
        int successed = context.getProcessedDatas().size();
        int failed = context.getFailedDatas().size();
        int all = context.getPrepareDatas().size();
        boolean isInterrupt = (all != failed + successed);
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);

        return MessageFormat.format(context_format, status, format.format(now), context.getIdentity().toString(), all,
                                    successed, failed, isInterrupt, dumpFileDatas(context.getProcessedDatas()),
                                    dumpFileDatas(context.getFailedDatas()));
    }

    public static String dumpFileDatas(List<FileData> fileDatas) {
        StringBuilder builder = new StringBuilder();
        synchronized (fileDatas) {
            for (FileData data : fileDatas) {
                builder.append("\t").append(data.toString()).append(SEP);
            }
        }
        return builder.toString();
    }

    public static String dumpMissFileDatas(Identity identity, FileData fileData) {
        return MessageFormat.format(miss_format, identity.toString(), fileData.toString());
    }

    public static String dumpFilterFileDatas(Identity identity, int input, int output, List<FileData> fileDatas) {
        StringBuilder builder = new StringBuilder();
        synchronized (fileDatas) {
            for (FileData data : fileDatas) {
                builder.append("\t").append(data.toString()).append(SEP);
            }
        }
        return MessageFormat.format(filter_format, identity.toString(), input, output, fileDatas.size(),
                                    builder.toString());
    }
}
