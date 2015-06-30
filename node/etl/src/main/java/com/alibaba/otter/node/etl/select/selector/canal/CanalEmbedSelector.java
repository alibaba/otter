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

package com.alibaba.otter.node.etl.select.selector.canal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.canal.extend.communication.CanalConfigClient;
import com.alibaba.otter.canal.extend.ha.MediaHAController;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.HAMode;
import com.alibaba.otter.canal.parse.CanalEventParser;
import com.alibaba.otter.canal.parse.ha.CanalHAController;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.sink.AbstractCanalEventSink;
import com.alibaba.otter.canal.sink.CanalEventSink;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.OtterContextLocator;
import com.alibaba.otter.node.etl.select.exceptions.SelectException;
import com.alibaba.otter.node.etl.select.selector.Message;
import com.alibaba.otter.node.etl.select.selector.MessageDumper;
import com.alibaba.otter.node.etl.select.selector.MessageParser;
import com.alibaba.otter.node.etl.select.selector.OtterSelector;
import com.alibaba.otter.shared.common.model.config.data.DataMedia.ModeValue;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 基于canal embed实现数据获取方式
 * 
 * @author jianghang 2012-7-31 下午02:45:15
 * @version 4.1.0
 */
public class CanalEmbedSelector implements OtterSelector {

    private static final Logger     logger           = LoggerFactory.getLogger(CanalEmbedSelector.class);
    private static final String     SEP              = SystemUtils.LINE_SEPARATOR;
    private static final String     DATE_FORMAT      = "yyyy-MM-dd HH:mm:ss";
    private static final int        maxEmptyTimes    = 10;
    private int                     logSplitSize     = 50;
    private boolean                 dump             = true;
    private boolean                 dumpDetail       = true;
    private Long                    pipelineId;
    private CanalServerWithEmbedded canalServer;
    private ClientIdentity          clientIdentity;
    private MessageParser           messageParser;
    private ConfigClientService     configClientService;
    private OtterDownStreamHandler  handler;

    private String                  destination;
    private String                  filter;
    private int                     batchSize        = 10000;
    private long                    batchTimeout     = -1L;
    private boolean                 ddlSync          = true;
    private boolean                 filterTableError = false;

    private CanalConfigClient       canalConfigClient;
    private volatile boolean        running          = false;                                            // 是否处于运行中
    private volatile long           lastEntryTime    = 0;

    public CanalEmbedSelector(Long pipelineId){
        this.pipelineId = pipelineId;
        canalServer = new CanalServerWithEmbedded();
    }

    public boolean isStart() {
        return running;
    }

    public void start() {
        if (running) {
            return;
        }
        // 获取destination/filter参数
        Pipeline pipeline = configClientService.findPipeline(pipelineId);
        filter = makeFilterExpression(pipeline);
        destination = pipeline.getParameters().getDestinationName();
        batchSize = pipeline.getParameters().getMainstemBatchsize();
        batchTimeout = pipeline.getParameters().getBatchTimeout();
        ddlSync = pipeline.getParameters().getDdlSync();
        final boolean syncFull = pipeline.getParameters().getSyncMode().isRow()
                                 || pipeline.getParameters().isEnableRemedy();
        // 暂时使用skip load代替
        filterTableError = pipeline.getParameters().getSkipSelectException();
        if (pipeline.getParameters().getDumpSelector() != null) {
            dump = pipeline.getParameters().getDumpSelector();
        }

        if (pipeline.getParameters().getDumpSelectorDetail() != null) {
            dumpDetail = pipeline.getParameters().getDumpSelectorDetail();
        }

        canalServer.setCanalInstanceGenerator(new CanalInstanceGenerator() {

            public CanalInstance generate(String destination) {
                Canal canal = canalConfigClient.findCanal(destination);
                final OtterAlarmHandler otterAlarmHandler = new OtterAlarmHandler();
                otterAlarmHandler.setPipelineId(pipelineId);
                OtterContextLocator.autowire(otterAlarmHandler); // 注入一下spring资源
                // 设置下slaveId，保证多个piplineId下重复引用时不重复
                long slaveId = 10000;// 默认基数
                if (canal.getCanalParameter().getSlaveId() != null) {
                    slaveId = canal.getCanalParameter().getSlaveId();
                }
                canal.getCanalParameter().setSlaveId(slaveId + pipelineId);
                canal.getCanalParameter().setDdlIsolation(ddlSync);
                canal.getCanalParameter().setFilterTableError(filterTableError);

                CanalInstanceWithManager instance = new CanalInstanceWithManager(canal, filter) {

                    protected CanalHAController initHaController() {
                        HAMode haMode = parameters.getHaMode();
                        if (haMode.isMedia()) {
                            return new MediaHAController(parameters.getMediaGroup(),
                                parameters.getDbUsername(),
                                parameters.getDbPassword(),
                                parameters.getDefaultDatabaseName());
                        } else {
                            return super.initHaController();
                        }
                    }

                    protected void startEventParserInternal(CanalEventParser parser, boolean isGroup) {
                        super.startEventParserInternal(parser, isGroup);

                        if (eventParser instanceof MysqlEventParser) {
                            // 设置支持的类型
                            ((MysqlEventParser) eventParser).setSupportBinlogFormats("ROW");
                            if (syncFull) {
                                ((MysqlEventParser) eventParser).setSupportBinlogImages("FULL");
                            } else {
                                ((MysqlEventParser) eventParser).setSupportBinlogImages("FULL,MINIMAL");
                            }

                            MysqlEventParser mysqlEventParser = (MysqlEventParser) eventParser;
                            CanalHAController haController = mysqlEventParser.getHaController();

                            if (haController instanceof MediaHAController) {
                                if (isGroup) {
                                    throw new CanalException("not support group database use media HA");
                                }

                                ((MediaHAController) haController).setCanalHASwitchable(mysqlEventParser);
                            }

                            if (!haController.isStart()) {
                                haController.start();
                            }

                            // 基于media的Ha，直接从tddl中获取数据库信息
                            if (haController instanceof MediaHAController) {
                                AuthenticationInfo authenticationInfo = ((MediaHAController) haController).getAvailableAuthenticationInfo();
                                ((MysqlEventParser) eventParser).setMasterInfo(authenticationInfo);
                            }
                        }
                    }

                };
                instance.setAlarmHandler(otterAlarmHandler);

                CanalEventSink eventSink = instance.getEventSink();
                if (eventSink instanceof AbstractCanalEventSink) {
                    handler = new OtterDownStreamHandler();
                    handler.setPipelineId(pipelineId);
                    handler.setDetectingIntervalInSeconds(canal.getCanalParameter().getDetectingIntervalInSeconds());
                    OtterContextLocator.autowire(handler); // 注入一下spring资源
                    ((AbstractCanalEventSink) eventSink).addHandler(handler, 0); // 添加到开头
                    handler.start();
                }

                return instance;
            }
        });
        canalServer.start();

        canalServer.start(destination);
        this.clientIdentity = new ClientIdentity(destination, pipeline.getParameters().getMainstemClientId(), filter);
        canalServer.subscribe(clientIdentity);// 发起一次订阅

        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            handler.stop();
        } catch (Exception e) {
            logger.warn("failed destory handler", e);
        }

        handler = null;
        canalServer.stop(destination);
        canalServer.stop();
    }

    public Message<EventData> selector() throws InterruptedException {
        int emptyTimes = 0;
        com.alibaba.otter.canal.protocol.Message message = null;
        if (batchTimeout < 0) {// 进行轮询处理
            while (running) {
                message = canalServer.getWithoutAck(clientIdentity, batchSize);
                if (message == null || message.getId() == -1L) { // 代表没数据
                    applyWait(emptyTimes++);
                } else {
                    break;
                }
            }
            if (!running) {
                throw new InterruptedException();
            }
        } else { // 进行超时控制
            while (running) {
                message = canalServer.getWithoutAck(clientIdentity, batchSize, batchTimeout, TimeUnit.MILLISECONDS);
                if (message == null || message.getId() == -1L) { // 代表没数据
                    continue;
                } else {
                    break;
                }
            }
            if (!running) {
                throw new InterruptedException();
            }
        }

        List<EventData> eventDatas = messageParser.parse(pipelineId, message.getEntries()); // 过滤事务头/尾和回环数据
        Message<EventData> result = new Message<EventData>(message.getId(), eventDatas);
        // 更新一下最后的entry时间，包括被过滤的数据
        if (!CollectionUtils.isEmpty(message.getEntries())) {
            long lastEntryTime = message.getEntries().get(message.getEntries().size() - 1).getHeader().getExecuteTime();
            if (lastEntryTime > 0) {// oracle的时间可能为0
                this.lastEntryTime = lastEntryTime;
            }
        }

        if (dump && logger.isInfoEnabled()) {
            String startPosition = null;
            String endPosition = null;
            if (!CollectionUtils.isEmpty(message.getEntries())) {
                startPosition = buildPositionForDump(message.getEntries().get(0));
                endPosition = buildPositionForDump(message.getEntries().get(message.getEntries().size() - 1));
            }

            dumpMessages(result, startPosition, endPosition, message.getEntries().size());// 记录一下，方便追查问题
        }
        return result;
    }

    public void rollback(Long batchId) {
        canalServer.rollback(clientIdentity, batchId);
    }

    public void rollback() {
        canalServer.rollback(clientIdentity);
    }

    public void ack(Long batchId) {
        canalServer.ack(clientIdentity, batchId);
    }

    public List<Long> unAckBatchs() {
        return canalServer.listBatchIds(clientIdentity);
    }

    public Long lastEntryTime() {
        return lastEntryTime;
    }

    /**
     * 记录一下message对象
     */
    private synchronized void dumpMessages(Message message, String startPosition, String endPosition, int total) {
        try {
            MDC.put(OtterConstants.splitPipelineSelectLogFileKey, String.valueOf(pipelineId));
            logger.info(SEP + "****************************************************" + SEP);
            logger.info(MessageDumper.dumpMessageInfo(message, startPosition, endPosition, total));
            logger.info("****************************************************" + SEP);
            if (dumpDetail) {// 判断一下是否需要打印详细信息
                dumpEventDatas(message.getDatas());
                logger.info("****************************************************" + SEP);
            }
        } finally {
            MDC.remove(OtterConstants.splitPipelineSelectLogFileKey);
        }
    }

    /**
     * 分批输出多个数据
     */
    private void dumpEventDatas(List<EventData> eventDatas) {
        int size = eventDatas.size();
        // 开始输出每条记录
        int index = 0;
        do {
            if (index + logSplitSize >= size) {
                logger.info(MessageDumper.dumpEventDatas(eventDatas.subList(index, size)));
            } else {
                logger.info(MessageDumper.dumpEventDatas(eventDatas.subList(index, index + logSplitSize)));
            }
            index += logSplitSize;
        } while (index < size);
    }

    /**
     * 构建filter 表达式
     */
    private String makeFilterExpression(Pipeline pipeline) {
        List<DataMediaPair> dataMediaPairs = pipeline.getPairs();
        if (dataMediaPairs.isEmpty()) {
            throw new SelectException("ERROR ## the pair is empty,the pipeline id = " + pipeline.getId());
        }

        Set<String> mediaNames = new HashSet<String>();
        for (DataMediaPair dataMediaPair : dataMediaPairs) {
            ModeValue namespaceMode = dataMediaPair.getSource().getNamespaceMode();
            ModeValue nameMode = dataMediaPair.getSource().getNameMode();

            if (namespaceMode.getMode().isSingle()) {
                buildFilter(mediaNames, namespaceMode.getSingleValue(), nameMode, false);
            } else if (namespaceMode.getMode().isMulti()) {
                for (String namespace : namespaceMode.getMultiValue()) {
                    buildFilter(mediaNames, namespace, nameMode, false);
                }
            } else if (namespaceMode.getMode().isWildCard()) {
                buildFilter(mediaNames, namespaceMode.getSingleValue(), nameMode, true);
            }
        }

        StringBuilder result = new StringBuilder();
        Iterator<String> iter = mediaNames.iterator();
        int i = -1;
        while (iter.hasNext()) {
            i++;
            if (i == 0) {
                result.append(iter.next());
            } else {
                result.append(",").append(iter.next());
            }
        }

        String markTable = pipeline.getParameters().getSystemSchema() + "."
                           + pipeline.getParameters().getSystemMarkTable();
        String bufferTable = pipeline.getParameters().getSystemSchema() + "."
                             + pipeline.getParameters().getSystemBufferTable();
        String dualTable = pipeline.getParameters().getSystemSchema() + "."
                           + pipeline.getParameters().getSystemDualTable();

        if (!mediaNames.contains(markTable)) {
            result.append(",").append(markTable);
        }

        if (!mediaNames.contains(bufferTable)) {
            result.append(",").append(bufferTable);
        }

        if (!mediaNames.contains(dualTable)) {
            result.append(",").append(dualTable);
        }

        // String otterTable = pipeline.getParameters().getSystemSchema() +
        // "\\..*";
        // if (!mediaNames.contains(otterTable)) {
        // result.append(",").append(otterTable);
        // }

        return result.toString();
    }

    private void buildFilter(Set<String> mediaNames, String namespace, ModeValue nameMode, boolean wildcard) {
        String splitChar = ".";
        if (wildcard) {
            splitChar = "\\.";
        }

        if (nameMode.getMode().isSingle()) {
            mediaNames.add(namespace + splitChar + nameMode.getSingleValue());
        } else if (nameMode.getMode().isMulti()) {
            for (String name : nameMode.getMultiValue()) {
                mediaNames.add(namespace + splitChar + name);
            }
        } else if (nameMode.getMode().isWildCard()) {
            mediaNames.add(namespace + "\\." + nameMode.getSingleValue());
        }
    }

    // 处理无数据的情况，避免空循环挂死
    private void applyWait(int emptyTimes) {
        int newEmptyTimes = emptyTimes > maxEmptyTimes ? maxEmptyTimes : emptyTimes;
        if (emptyTimes <= 3) { // 3次以内
            Thread.yield();
        } else { // 超过3次，最多只sleep 10ms
            LockSupport.parkNanos(1000 * 1000L * newEmptyTimes);
        }
    }

    private String buildPositionForDump(Entry entry) {
        long time = entry.getHeader().getExecuteTime();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return entry.getHeader().getLogfileName() + ":" + entry.getHeader().getLogfileOffset() + ":"
               + entry.getHeader().getExecuteTime() + "(" + format.format(date) + ")";
    }

    // ================== setter / getter ==================
    public void setMessageParser(MessageParser messageParser) {
        this.messageParser = messageParser;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setCanalConfigClient(CanalConfigClient canalConfigClient) {
        this.canalConfigClient = canalConfigClient;
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

}
