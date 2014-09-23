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

package com.alibaba.otter.node.etl.load.loader.db.interceptor.log;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadDumper;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.node.etl.load.loader.interceptor.AbstractLoadInterceptor;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * load的日志记录
 * 
 * @author jianghang 2011-11-10 上午11:31:05
 * @version 4.0.0
 */
public class LogLoadInterceptor extends AbstractLoadInterceptor<DbLoadContext, EventData> {

    private static final Logger logger           = LoggerFactory.getLogger(LogLoadInterceptor.class);
    private static final String SEP              = SystemUtils.LINE_SEPARATOR;
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    private int                 batchSize        = 50;
    private static String       context_format   = null;
    private boolean             dump             = true;

    static {
        context_format = "* status : {0}  , time : {1} *" + SEP;
        context_format += "* Identity : {2} *" + SEP;
        context_format += "* total Data : [{3}] , success Data : [{4}] , failed Data : [{5}] , Interrupt : [{6}]" + SEP;
    }

    public void commit(DbLoadContext context) {
        // 成功时记录一下
        boolean dumpThisEvent = context.getPipeline().getParameters().isDumpEvent()
                                || context.getPipeline().getParameters().isDryRun();
        if (dump && dumpThisEvent && logger.isInfoEnabled()) {
            synchronized (LogLoadInterceptor.class) {
                try {
                    MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                            String.valueOf(context.getIdentity().getPipelineId()));
                    logger.info(SEP + "****************************************************" + SEP);
                    logger.info(dumpContextInfo("successed", context));
                    logger.info("****************************************************" + SEP);
                    logger.info("* process Data  *" + SEP);
                    logEventDatas(context.getProcessedDatas());
                    logger.info("-----------------" + SEP);
                    logger.info("* failed Data *" + SEP);
                    logEventDatas(context.getFailedDatas());
                    logger.info("****************************************************" + SEP);
                } finally {
                    MDC.remove(OtterConstants.splitPipelineLoadLogFileKey);
                }
            }
        }
    }

    public void error(DbLoadContext context) {
        boolean dumpThisEvent = context.getPipeline().getParameters().isDumpEvent()
                                || context.getPipeline().getParameters().isDryRun();
        if (dump && dumpThisEvent && logger.isInfoEnabled()) {
            synchronized (LogLoadInterceptor.class) {
                try {
                    MDC.put(OtterConstants.splitPipelineLoadLogFileKey,
                            String.valueOf(context.getIdentity().getPipelineId()));
                    logger.info(dumpContextInfo("error", context));
                    logger.info("* process Data  *" + SEP);
                    logEventDatas(context.getProcessedDatas());
                    logger.info("-----------------" + SEP);
                    logger.info("* failed Data *" + SEP);
                    logEventDatas(context.getFailedDatas());
                    logger.info("****************************************************" + SEP);
                } finally {
                    MDC.remove(OtterConstants.splitPipelineLoadLogFileKey);
                }
            }
        }
    }

    /**
     * 分批输出多个数据
     */
    private void logEventDatas(List<EventData> eventDatas) {
        int size = eventDatas.size();
        // 开始输出每条记录
        int index = 0;
        do {
            if (index + batchSize >= size) {
                logger.info(DbLoadDumper.dumpEventDatas(eventDatas.subList(index, size)));
            } else {
                logger.info(DbLoadDumper.dumpEventDatas(eventDatas.subList(index, index + batchSize)));
            }
            index += batchSize;
        } while (index < size);
    }

    private String dumpContextInfo(String status, DbLoadContext context) {
        int successed = context.getProcessedDatas().size();
        int failed = context.getFailedDatas().size();
        int all = context.getPrepareDatas().size();
        boolean isInterrupt = (all != (failed + successed));
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return MessageFormat.format(context_format, status, format.format(now), context.getIdentity().toString(), all,
                                    successed, failed, isInterrupt);
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

}
