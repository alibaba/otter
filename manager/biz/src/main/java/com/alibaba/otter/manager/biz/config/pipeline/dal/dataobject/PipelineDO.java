package com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;

/**
 * @author simon
 */
public class PipelineDO implements Serializable {

    private static final long serialVersionUID = -4894036418246404446L;
    private Long              id;
    private String            name;
    private PipelineParameter parameters;
    private String            description;                             // 描述信息
    private Long              channelId;                               // 对应关联的channel唯一标示id
    private List<Long>        selectNodeId;
    private List<Long>        extractNodeId;
    private List<Long>        loadNodeId;
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

    public PipelineParameter getParameters() {
        return parameters;
    }

    public void setParameters(PipelineParameter parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public List<Long> getSelectNodeId() {
        return selectNodeId;
    }

    public void setSelectNodeId(List<Long> selectNodeId) {
        this.selectNodeId = selectNodeId;
    }

    public List<Long> getExtractNodeId() {
        return extractNodeId;
    }

    public void setExtractNodeId(List<Long> extractNodeId) {
        this.extractNodeId = extractNodeId;
    }

    public List<Long> getLoadNodeId() {
        return loadNodeId;
    }

    public void setLoadNodeId(List<Long> loadNodeId) {
        this.loadNodeId = loadNodeId;
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
