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

package com.alibaba.otter.manager.biz.autokeeper.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.utils.RegexUtils;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperConnectionStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperEphemeralStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperQuorumType;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperWatchStat;
import com.alibaba.otter.shared.common.utils.cmd.Exec;
import com.alibaba.otter.shared.common.utils.cmd.Exec.Result;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * 对应的数据采集器
 * 
 * @author jianghang 2012-9-21 下午03:05:28
 * @version 4.1.0
 */
public class AutoKeeperCollector implements InitializingBean {

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    private static final String      MODE_FOLLOWER            = "Mode: follower";
    private static final String      MODE_LEADERER            = "Mode: leader";
    private static final String      MODE_OBSERVER            = "Mode: observer";
    private static final String      MODE_STANDALONE          = "Mode: standalone";
    private static final String      NODE_COUNT               = "Node count:";
    private static final String      STRING_LATENCY           = "Latency min/avg/max:";
    private static final String      STRING_SENT              = "Sent:";
    private static final String      STRING_RECEIVED          = "Received:";
    private static final String      STRING_OUTSTANDING       = "Outstanding:";
    private static final String      COMMA                    = ",";
    private static final String      BRACKETS                 = ")";
    private static final String      COLON                    = ":";
    private static final String      WRAP                     = "\n";
    private static final String      CMD_STAT                 = "echo stat | nc %s %s";
    private static final String      CMD_CONS                 = "echo cons | nc %s %s";
    private static final String      CMD_DUMP                 = "echo dump | nc %s %s";
    private static final String      CMD_WCHC                 = "echo wchc | nc %s %s";
    private static final long        DEFAULT_COLLECT_INTERVAL = 300;
    private long                     delay                    = 1;
    private int                      singleSize               = 1;
    private long                     collectInterval          = DEFAULT_COLLECT_INTERVAL;

    private AutoKeeperData           autoKeeperData;
    private ScheduledExecutorService collectorExecutor;

    public void collectorConnectionStat(String address) {
        List<String> netAddress = splitAddress(address);
        if (netAddress.isEmpty()) {
            return;
        }
        String ip = netAddress.get(0);
        String port = netAddress.get(1);
        String[] cmd = { "/bin/bash", "-c", String.format(CMD_CONS, ip, port) };
        String cmdresult = collector(cmd);
        String[] result = cmdresult.split(WRAP);
        List<AutoKeeperConnectionStat> summary = new ArrayList<AutoKeeperConnectionStat>();

        for (String line : result) {

            if (StringUtils.isBlank(line)) {
                continue;
            }

            String[] lineArray = line.split(":");
            if (2 != lineArray.length) {
                continue;
            }

            AutoKeeperConnectionStat autoKeeperConnectionStat = new AutoKeeperConnectionStat();
            autoKeeperConnectionStat.setOriginalContent(line);
            String clientIp = StringUtils.trimToEmpty(line.split(":")[0].replace("/", ""));
            String sessionId = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "sid=(?s).*?[,)]")).replace("sid=",
                                                                                                                            StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                       StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                                  StringUtils.EMPTY);
            String queued = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "queued=(?s).*?[,)]")).replace("queued=",
                                                                                                                            StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                       StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                                  StringUtils.EMPTY);
            String receive = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "recved=(?s).*?[,)]")).replace("recved=",
                                                                                                                             StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                        StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                                   StringUtils.EMPTY);
            String sent = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "sent=(?s).*?[,)]")).replace("sent=",
                                                                                                                        StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                   StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                              StringUtils.EMPTY);
            String minlat = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "minlat=(?s).*?[,)]")).replace("minlat=",
                                                                                                                            StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                       StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                                  StringUtils.EMPTY);
            String avglat = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "avglat=(?s).*?[,)]")).replace("avglat=",
                                                                                                                            StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                       StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                                  StringUtils.EMPTY);
            String maxlat = StringUtils.trimToEmpty(RegexUtils.findFirst(line.split(":")[1], "maxlat=(?s).*?[,)]")).replace("maxlat=",
                                                                                                                            StringUtils.EMPTY).replace(COMMA,
                                                                                                                                                       StringUtils.EMPTY).replace(BRACKETS,
                                                                                                                                                                                  StringUtils.EMPTY);
            autoKeeperConnectionStat.setServerAddress(ip);
            autoKeeperConnectionStat.setClientAddress(clientIp);
            autoKeeperConnectionStat.setSessionId(sessionId);
            if (StringUtils.isNotEmpty(queued)) {
                autoKeeperConnectionStat.setQueued(Long.parseLong(queued));
            }
            if (StringUtils.isNotEmpty(receive)) {
                autoKeeperConnectionStat.setRecved(Long.parseLong(receive));
            }
            if (StringUtils.isNotEmpty(sent)) {
                autoKeeperConnectionStat.setSent(Long.parseLong(sent));
            }
            if (StringUtils.isNotEmpty(minlat)) {
                autoKeeperConnectionStat.setMinLatency(Long.parseLong(minlat));
            }
            if (StringUtils.isNotEmpty(avglat)) {
                autoKeeperConnectionStat.setAvgLatency(Long.parseLong(avglat));
            }
            if (StringUtils.isNotEmpty(maxlat)) {
                autoKeeperConnectionStat.setMaxLatency(Long.parseLong(maxlat));
            }

            summary.add(autoKeeperConnectionStat);
        }
        autoKeeperData.joinConnection(address, summary);
    }

    public void collectorServerStat(String address) {
        List<String> netAddress = splitAddress(address);
        if (netAddress.isEmpty()) {
            return;
        }
        String ip = netAddress.get(0);
        String port = netAddress.get(1);
        String[] cmd = { "/bin/bash", "-c", String.format(CMD_STAT, ip, port) };
        String cmdresult = collector(cmd);
        String[] result = cmdresult.split(WRAP);
        AutoKeeperServerStat summary = new AutoKeeperServerStat();
        summary.setOriginalContent(cmdresult);
        for (String line : result) {

            if (line.contains(MODE_FOLLOWER)) {
                summary.setQuorumType(AutoKeeperQuorumType.FOLLOWER);
            } else if (line.contains(MODE_LEADERER)) {
                summary.setQuorumType(AutoKeeperQuorumType.LEADER);
            } else if (line.contains(MODE_STANDALONE)) {
                summary.setQuorumType(AutoKeeperQuorumType.STANDALONE);
            } else if (line.contains(MODE_OBSERVER)) {
                summary.setQuorumType(AutoKeeperQuorumType.OBSERVER);
            } else if (line.contains(STRING_LATENCY)) {
                List<String> latency = Arrays.asList(StringUtils.trimToEmpty(line.replace(STRING_LATENCY,
                                                                                          StringUtils.EMPTY)).split("/"));
                summary.setMinLatency(Long.parseLong(latency.get(0)));
                summary.setAvgLatency(Long.parseLong(latency.get(1)));
                summary.setMaxLatency(Long.parseLong(latency.get(2)));
            } else if (line.contains(STRING_OUTSTANDING)) {
                summary.setQueued(Long.parseLong(StringUtils.trimToEmpty(line.replace(STRING_OUTSTANDING,
                                                                                      StringUtils.EMPTY))));
            } else if (line.contains(NODE_COUNT)) {
                summary.setNodeCount(Long.parseLong(StringUtils.trimToEmpty(line.replace(NODE_COUNT, StringUtils.EMPTY))));
            } else if (line.contains(STRING_SENT)) {
                summary.setSent(Long.parseLong(StringUtils.trimToEmpty(line.replace(STRING_SENT, StringUtils.EMPTY))));
            } else if (line.contains(STRING_RECEIVED)) {
                summary.setRecved(Long.parseLong(StringUtils.trimToEmpty(line.replace(STRING_RECEIVED,
                                                                                      StringUtils.EMPTY))));
            }
        }

        autoKeeperData.joinServer(address, summary);
    }

    public void collectorEphemeralStat(String address) {
        List<String> netAddress = splitAddress(address);
        if (netAddress.isEmpty()) {
            return;
        }
        String ip = netAddress.get(0);
        String port = netAddress.get(1);
        String[] cmd = { "/bin/bash", "-c", String.format(CMD_DUMP, ip, port) };
        String cmdresult = collector(cmd);

        Map<String, List<String>> pathMap = groupSessionPath(cmdresult);

        List<AutoKeeperEphemeralStat> autoKeeperEphemeralStats = new ArrayList<AutoKeeperEphemeralStat>();
        for (Map.Entry<String, List<String>> entry : pathMap.entrySet()) {
            AutoKeeperEphemeralStat autoKeeperEphemeralStat = new AutoKeeperEphemeralStat();
            autoKeeperEphemeralStat.setSessionId(entry.getKey());
            autoKeeperEphemeralStat.setPaths(entry.getValue());
            autoKeeperEphemeralStats.add(autoKeeperEphemeralStat);
        }

        autoKeeperData.joinEphemeral(address, autoKeeperEphemeralStats);

    }

    public void collectorWatchStat(String address) {
        List<String> netAddress = splitAddress(address);
        if (netAddress.isEmpty()) {
            return;
        }
        String ip = netAddress.get(0);
        String port = netAddress.get(1);
        String[] cmd = { "/bin/bash", "-c", String.format(CMD_WCHC, ip, port) };
        String cmdresult = collector(cmd);

        Map<String, List<String>> pathMap = groupSessionPath(cmdresult);

        List<AutoKeeperWatchStat> autoKeeperWatchStats = new ArrayList<AutoKeeperWatchStat>();
        for (Map.Entry<String, List<String>> entry : pathMap.entrySet()) {
            AutoKeeperWatchStat autoKeeperWatchStat = new AutoKeeperWatchStat();
            autoKeeperWatchStat.setSessionId(entry.getKey());
            autoKeeperWatchStat.setPaths(entry.getValue());
            autoKeeperWatchStats.add(autoKeeperWatchStat);
        }

        autoKeeperData.joinWatch(address, autoKeeperWatchStats);

    }

    public static String collector(String[] command) {
        Result result = null;
        try {
            result = Exec.execute(command);
            if (result.getExitCode() == 0) {
                return result.getStdout();
            } else {
                return result.getStderr();
            }
        } catch (Exception e) {
            throw new ManagerException(e);
        }
    }

    private List<String> splitAddress(String address) {
        List<String> ipPort = Arrays.asList(address.split(":"));
        if (ipPort.size() != 2) {
            return new ArrayList<String>();
        }
        return ipPort;
    }

    /**
     * <pre>
     * key=sessionId
     * value=pathList
     * </pre>
     */
    private Map<String, List<String>> groupSessionPath(String cmdresult) {
        String[] result = cmdresult.split(WRAP);

        Map<String, List<String>> pathMap = new HashMap<String, List<String>>();
        String sessionId = StringUtils.EMPTY;
        for (String line : result) {
            line = StringUtils.trimToEmpty(line);
            if (StringUtils.isBlank(line)) {
                continue;
            }
            if (line.startsWith("0x")) {
                sessionId = line.replace(COLON, StringUtils.EMPTY);
                pathMap.put(sessionId, new ArrayList<String>());
            } else if (line.startsWith("/")) {
                List<String> paths = pathMap.get(sessionId);
                paths.add(line);
            }
        }
        return pathMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        collectorExecutor = Executors.newScheduledThreadPool(singleSize, new NamedThreadFactory("collector-thread",
                                                                                                true));
        startCollect();
    }

    private void startCollect() {

        // 启动定时工作任务
        collectorExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                List<AutoKeeperCluster> autoKeeperClusters = autoKeeperClusterService.listAutoKeeperClusters();
                if (!autoKeeperClusters.isEmpty()) {
                    autoKeeperData.persist();
                    for (AutoKeeperCluster autoKeeperCluster : autoKeeperClusters) {
                        List<String> servers = autoKeeperCluster.getServerList();
                        for (String address : servers) {
                            collectorServerStat(address);
                            collectorConnectionStat(address);
                            collectorWatchStat(address);
                            collectorEphemeralStat(address);
                        }
                    }
                }
            }
        }, delay, collectInterval, TimeUnit.SECONDS);

    }

    public void setAutoKeeperClusterService(AutoKeeperClusterService autoKeeperClusterService) {
        this.autoKeeperClusterService = autoKeeperClusterService;
    }

    public void setAutoKeeperData(AutoKeeperData autoKeeperData) {
        this.autoKeeperData = autoKeeperData;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
    }

}
