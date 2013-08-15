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
    private Integer           downloadPort;                            // 下载端口
    private AutoKeeperCluster zkCluster;                               // zk的集群
    private String            externalIp;                              // 外部ip

    public Integer getDownloadPort() {
        return downloadPort;
    }

    public void setDownloadPort(Integer downloadPort) {
        this.downloadPort = downloadPort;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
