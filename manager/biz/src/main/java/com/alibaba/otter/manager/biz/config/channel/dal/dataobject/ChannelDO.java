package com.alibaba.otter.manager.biz.config.channel.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;

/**
 * @author simon
 */
public class ChannelDO implements Serializable {

    private static final long serialVersionUID = 3708730560311969117L;
    private Long              id;                                     // 唯一标示id
    private String            name;                                   // channel命名
    private ChannelStatus     status;                                 // 运行状态
    private String            description;                            // 描述信息
    private ChannelParameter  parameters;                             // 配置参数
    private Date              gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelStatus getStatus() {
        return status;
    }

    public void setStatus(ChannelStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ChannelParameter getParameters() {
        return parameters;
    }

    public void setParameters(ChannelParameter parameters) {
        this.parameters = parameters;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

}
