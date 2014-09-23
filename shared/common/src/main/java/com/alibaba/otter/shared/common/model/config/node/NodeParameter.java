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

package com.alibaba.otter.shared.common.model.config.node;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * Node节点对应的参数信息
 * 
 * @author jianghang 2011-9-16 下午03:39:36
 * @version 4.0.0
 */
public class NodeParameter implements Serializable {

    private static final long serialVersionUID = -4788966688697451950L;
    private Integer           mbeanPort;                               // mbean端口
    private Integer           downloadPort;                            // 下载端口
    private AutoKeeperCluster zkCluster;                               // zk的集群
    private String            externalIp;                              // 外部ip
    private Boolean           useExternalIp    = false;                // 是否使用外部ip，此优先级高于pipeline参数，设置后包括rpc/pipe都将使用外部ip

    public Integer getDownloadPort() {
        return downloadPort;
    }

    public void setDownloadPort(Integer downloadPort) {
        this.downloadPort = downloadPort;
    }

    public Integer getMbeanPort() {
        return mbeanPort;
    }

    public void setMbeanPort(Integer mbeanPort) {
        this.mbeanPort = mbeanPort;
    }

    public AutoKeeperCluster getZkCluster() {
        return zkCluster;
    }

    public void setZkCluster(AutoKeeperCluster zkCluster) {
        this.zkCluster = zkCluster;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public Boolean getUseExternalIp() {
        return useExternalIp == null ? false : useExternalIp;
    }

    public void setUseExternalIp(Boolean useExternalIp) {
        this.useExternalIp = useExternalIp;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
