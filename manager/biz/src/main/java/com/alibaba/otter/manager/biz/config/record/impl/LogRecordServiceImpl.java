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

package com.alibaba.otter.manager.biz.config.record.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.record.LogRecordService;
import com.alibaba.otter.manager.biz.config.record.dal.LogRecordDAO;
import com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.record.LogRecord;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;

/**
 * 类LogRecordServiceImpl.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-6-15 下午5:01:04
 * @version 4.1.0
 */
public class LogRecordServiceImpl implements LogRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LogRecordServiceImpl.class);

    private ChannelService      channelService;
    private LogRecordDAO        logRecordDao;

    public void create(Event event) {
        LogRecord logRecord = new LogRecord();
        if (event instanceof NodeAlarmEvent) {
            NodeAlarmEvent nodeAlarmEvent = (NodeAlarmEvent) event;
            Pipeline tempPipeline = new Pipeline();
            tempPipeline.setId(nodeAlarmEvent.getPipelineId());
            logRecord.setPipeline(tempPipeline);
            logRecord.setNid(nodeAlarmEvent.getNid());
            logRecord.setTitle(nodeAlarmEvent.getTitle());
            logRecord.setMessage(nodeAlarmEvent.getMessage());
        }
        create(logRecord);
    }

    public void create(LogRecord entityObj) {
        Assert.assertNotNull(entityObj);
        logRecordDao.insert(modelToDo(entityObj));
    }

    public void remove(Long identity) {
        Assert.assertNotNull(identity);
        logRecordDao.delete(identity);

    }

    public void modify(LogRecord entityObj) {

    }

    public LogRecord findById(Long identity) {

        return null;
    }

    public List<LogRecord> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<LogRecordDO> logRecordDos = logRecordDao.listByPipelineId(pipelineId);
        return doToModel(logRecordDos);
    }

    public List<LogRecord> listByPipelineIdWithoutContent(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<LogRecordDO> logRecordDos = logRecordDao.listByPipelineIdWithoutContent(pipelineId);
        return doToModel(logRecordDos);
    }

    public List<LogRecord> listByIds(Long... identities) {

        return null;
    }

    public List<LogRecord> listAll() {
        List<LogRecordDO> logRecordDos = logRecordDao.listAll();
        return doToModel(logRecordDos);
    }

    public List<LogRecord> listByCondition(Map condition) {
        List<LogRecordDO> logRecordDos = logRecordDao.listByCondition(condition);
        if (logRecordDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any log record by the condition:"
                         + JsonUtils.marshalToString(condition));
            return new ArrayList<LogRecord>();
        }
        return doToModel(logRecordDos);
    }

    public int getCount() {

        return 0;
    }

    public int getCount(Map condition) {

        return logRecordDao.getCount(condition);
    }

    /*----------------------DO <-> MODEL 组装方法--------------------------*/
    /**
     * <pre>
     * 用于Model对象转化为DO对象
     * 优化：
     *      无SQL交互，只是简单进行字段组装，暂时无须优化
     * </pre>
     * 
     * @param channel
     * @return ChannelDO
     */
    private LogRecordDO modelToDo(LogRecord entityObj) {

        LogRecordDO logRecordDo = new LogRecordDO();
        try {

            if (entityObj.getPipeline() != null && entityObj.getPipeline().getId() > 0) {
                Channel channel = channelService.findByPipelineId(entityObj.getPipeline().getId());
                logRecordDo.setChannelId(channel.getId());
                logRecordDo.setPipelineId(entityObj.getPipeline().getId());
            } else {
                logRecordDo.setChannelId(-1l);
                logRecordDo.setPipelineId(-1l);
            }

            logRecordDo.setNid(entityObj.getNid());
            logRecordDo.setTitle(entityObj.getTitle());
            String message = entityObj.getMessage();
            if (message != null && message.length() > 65535) {
                message = message.substring(0, 65535);
            }
            logRecordDo.setMessage(message);
            logRecordDo.setGmtCreate(entityObj.getGmtCreate());
            logRecordDo.setGmtModified(entityObj.getGmtModified());

        } catch (Exception e) {
            logger.error("ERROR ## has an error where write log to db");
            throw new ManagerException(e);
        }
        return logRecordDo;
    }

    /**
     * <pre>
     * 用于DO对象转化为Model对象
     * </pre>
     * 
     * @param channelDO
     * @return Channel
     */

    private LogRecord doToModel(LogRecordDO logRecordDo) {
        LogRecord logRecord = new LogRecord();
        try {

            logRecord.setId(logRecordDo.getId());
            if (logRecordDo.getPipelineId() > 0 && logRecordDo.getChannelId() > 0) {
                try {
                    Channel channel = channelService.findByPipelineId(logRecordDo.getPipelineId());
                    logRecord.setChannel(channel);
                    for (Pipeline pipeline : channel.getPipelines()) {
                        if (pipeline.getId().equals(logRecordDo.getPipelineId())) {
                            logRecord.setPipeline(pipeline);
                        }
                    }
                } catch (Exception e) {
                    // 可能历史的log记录对应的channel/pipeline已经被删除了，直接忽略吧
                    Channel channel = new Channel();
                    channel.setId(0l);
                    logRecord.setChannel(channel);
                    Pipeline pipeline = new Pipeline();
                    pipeline.setId(0l);
                    logRecord.setPipeline(pipeline);
                }
            } else {
                Channel channel = new Channel();
                channel.setId(-1l);
                logRecord.setChannel(channel);
                Pipeline pipeline = new Pipeline();
                pipeline.setId(-1l);
                logRecord.setPipeline(pipeline);
            }

            logRecord.setTitle(logRecordDo.getTitle());
            logRecord.setNid(logRecordDo.getNid());
            logRecord.setMessage(logRecordDo.getMessage());
            logRecord.setGmtCreate(logRecordDo.getGmtCreate());
            logRecord.setGmtModified(logRecordDo.getGmtModified());

        } catch (Exception e) {
            logger.error("ERROR ## ");
            throw new ManagerException(e);
        }

        return logRecord;
    }

    private List<LogRecord> doToModel(List<LogRecordDO> logRecordDos) {
        List<LogRecord> logRecords = new ArrayList<LogRecord>();
        try {
            for (LogRecordDO logRecordDo : logRecordDos) {
                logRecords.add(doToModel(logRecordDo));
            }

        } catch (Exception e) {
            logger.error("ERROR ##");
            throw new ManagerException(e);
        }

        return logRecords;
    }

    public ChannelService getChannelService() {
        return channelService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public LogRecordDAO getLogRecordDao() {
        return logRecordDao;
    }

    public void setLogRecordDao(LogRecordDAO logRecordDao) {
        this.logRecordDao = logRecordDao;
    }

}
