package com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

/**
 * 类DataColumnPairDO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:09:38
 */
public class DataColumnPairDO implements Serializable {

    private static final long serialVersionUID = 194553152360180533L;
    private Long              id;
    private String            sourceColumnName;                      // 源字段
    private String            targetColumnName;                      // 目标字段
    private Long              dataMediaPairId;
    private Date              gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public void setSourceColumnName(String sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public void setTargetColumnName(String targetColumnName) {
        this.targetColumnName = targetColumnName;
    }

    public Long getDataMediaPairId() {
        return dataMediaPairId;
    }

    public void setDataMediaPairId(Long dataMediaPairId) {
        this.dataMediaPairId = dataMediaPairId;
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
