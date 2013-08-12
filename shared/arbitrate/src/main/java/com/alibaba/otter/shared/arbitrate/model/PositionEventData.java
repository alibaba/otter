package com.alibaba.otter.shared.arbitrate.model;

import java.util.Date;

/**
 * 位点信息
 * 
 * @author jianghang 2012-12-12 上午10:52:12
 * @version 4.1.3
 */
public class PositionEventData extends EventData {

    private static final long serialVersionUID = 1L;
    private Date              createTime;
    private Date              modifiedTime;
    private String            position;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

}
