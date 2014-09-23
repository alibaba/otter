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

package com.alibaba.otter.shared.common.model.config.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 全局参数定义
 * 
 * @author jianghang 2012-4-9 下午01:51:04
 * @version 4.0.2
 */
public class SystemParameter implements Serializable {

    private static final long   serialVersionUID       = -1780184554337059839L;

    private String              systemSchema           = "retl";                             // 默认为retl，不允许为空
    private String              systemMarkTable        = "retl_mark";                        // 双向同步标记表
    private String              systemMarkTableColumn  = "channel_id";                       // 双向同步标记的列名
    private String              systemMarkTableInfo    = "channel_info";                     // 双向同步标记的info信息
    private String              systemBufferTable      = "retl_buffer";                      // otter同步系统buffer表
    private String              systemDualTable        = "xdual";                            // otter同步心跳表
    private List<String>        hzZkClusters           = new ArrayList<String>();            // 杭州zk集群列表
    private List<String>        usZkClusters           = new ArrayList<String>();            // 美国zk集群列表
    private List<String>        hzStoreClusters        = new ArrayList<String>();            // 杭州store集群列表
    private List<String>        usStoreClusters        = new ArrayList<String>();            // 美国store集群列表
    private String              hzArandaCluster        = "";                                 // 杭州aranda集群地址
    private String              usArandaCluster        = "";                                 // 美国aranda集群地址
    private RetrieverType       retriever              = RetrieverType.ARIA2C;               // 下载方式
    private String              defaultAlarmReceiveKey = "otterteam";
    private String              defaultAlarmReceiver   = "jianghang115@gmail.com";
    private Map<String, String> alarmReceiver          = new LinkedHashMap<String, String>(); // 报警联系人

    public static enum RetrieverType {
        /** java版多线程下载 */
        MR4J(""),
        /** aria2c多线程下载 */
        ARIA2C("aria2c");

        private String exe; // 代表可执行文件的路径，可以是相对于PATH的路径

        RetrieverType(String exe){
            this.exe = exe;
        }

        public boolean isMr4j() {
            return this.equals(RetrieverType.MR4J);
        }

        public boolean isAria2c() {
            return this.equals(RetrieverType.ARIA2C);
        }

        public String getExe() {
            return exe;
        }

    }

    public String getSystemSchema() {
        return systemSchema;
    }

    public void setSystemSchema(String systemSchema) {
        this.systemSchema = systemSchema;
    }

    public String getSystemMarkTable() {
        return systemMarkTable;
    }

    public void setSystemMarkTable(String systemMarkTable) {
        this.systemMarkTable = systemMarkTable;
    }

    public String getSystemBufferTable() {
        return systemBufferTable;
    }

    public void setSystemBufferTable(String systemBufferTable) {
        this.systemBufferTable = systemBufferTable;
    }

    public RetrieverType getRetriever() {
        return retriever;
    }

    public void setRetriever(RetrieverType retriever) {
        this.retriever = retriever;
    }

    public String getSystemMarkTableColumn() {
        return systemMarkTableColumn;
    }

    public void setSystemMarkTableColumn(String systemMarkTableColumn) {
        this.systemMarkTableColumn = systemMarkTableColumn;
    }

    public String getSystemDualTable() {
        return systemDualTable;
    }

    public void setSystemDualTable(String systemDualTable) {
        this.systemDualTable = systemDualTable;
    }

    public String getSystemMarkTableInfo() {
        return systemMarkTableInfo;
    }

    public void setSystemMarkTableInfo(String systemMarkTableInfo) {
        this.systemMarkTableInfo = systemMarkTableInfo;
    }

    public String getHzArandaCluster() {
        return hzArandaCluster;
    }

    public void setHzArandaCluster(String hzArandaCluster) {
        this.hzArandaCluster = hzArandaCluster;
    }

    public List<String> getHzZkClusters() {
        return hzZkClusters;
    }

    public void setHzZkClusters(List<String> hzZkClusters) {
        this.hzZkClusters = hzZkClusters;
    }

    public List<String> getUsZkClusters() {
        return usZkClusters;
    }

    public void setUsZkClusters(List<String> usZkClusters) {
        this.usZkClusters = usZkClusters;
    }

    public String getUsArandaCluster() {
        return usArandaCluster;
    }

    public void setUsArandaCluster(String usArandaCluster) {
        this.usArandaCluster = usArandaCluster;
    }

    public List<String> getHzStoreClusters() {
        return hzStoreClusters;
    }

    public void setHzStoreClusters(List<String> hzStoreClusters) {
        this.hzStoreClusters = hzStoreClusters;
    }

    public List<String> getUsStoreClusters() {
        return usStoreClusters;
    }

    public void setUsStoreClusters(List<String> usStoreClusters) {
        this.usStoreClusters = usStoreClusters;
    }

    public Map<String, String> getAlarmReceiver() {
        return alarmReceiver;
    }

    public void setAlarmReceiver(Map<String, String> alarmReceiver) {
        this.alarmReceiver = alarmReceiver;
    }

    public String getDefaultAlarmReceiveKey() {
        return defaultAlarmReceiveKey;
    }

    public void setDefaultAlarmReceiveKey(String defaultAlarmReceiveKey) {
        this.defaultAlarmReceiveKey = defaultAlarmReceiveKey;
    }

    public String getDefaultAlarmReceiver() {
        return defaultAlarmReceiver;
    }

    public void setDefaultAlarmReceiver(String defaultAlarmReceiver) {
        this.defaultAlarmReceiver = defaultAlarmReceiver;
    }

    // ================ helper method==================

    public String getDefaultAlarmReceiverFormat() {
        return defaultAlarmReceiveKey + "=" + defaultAlarmReceiver;
    }

    public String getAlarmReceiverFormat() {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, String> entry : alarmReceiver.entrySet()) {
            result.add(entry.getKey() + "=" + entry.getValue());
        }

        return StringUtils.join(result, "\n");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
