package com.alibaba.otter.shared.common.model.config.node;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.model.config.enums.AreaType;
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
    private List<String>      zkClusters;                              // zk的集群列表,每个集群的多个分区可用逗号分隔
    private List<String>      storeClusters;                           // store的集群列表,每个集群的多个分区可用逗号分隔
    private String            externalIp;                              // 外部ip
    private String            arandaUrl;                               // aranda访问url
    private AreaType          areaType;                                // 默认为hz地区

    public List<String> getZkClusters() {
        return zkClusters;
    }

    public void setZkClusters(List<String> zkClusters) {
        this.zkClusters = zkClusters;
    }

    public List<String> getStoreClusters() {
        return storeClusters;
    }

    public void setStoreClusters(List<String> storeClusters) {
        this.storeClusters = storeClusters;
    }

    public Integer getDownloadPort() {
        return downloadPort;
    }

    public void setDownloadPort(Integer downloadPort) {
        this.downloadPort = downloadPort;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public String getArandaUrl() {
        return arandaUrl;
    }

    public void setArandaUrl(String arandaUrl) {
        this.arandaUrl = arandaUrl;
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
