package com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.shared.common.model.config.data.DataMediaType;

/**
 * @author simon
 */
public class DataMediaSourceDO implements Serializable {

    private static final long serialVersionUID = 5123273832849527936L;
    private Long              id;
    private String            name;
    private DataMediaType     type;
    private String            properties;
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

    public DataMediaType getType() {
        return type;
    }

    public void setType(DataMediaType type) {
        this.type = type;
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

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

}
