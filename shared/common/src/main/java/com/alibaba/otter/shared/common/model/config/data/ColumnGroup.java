package com.alibaba.otter.shared.common.model.config.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 数据介质字段同步组
 * 
 * @author simon 2012-3-31 下午03:54:22
 */
public class ColumnGroup implements Serializable {

    private static final long serialVersionUID = 8903835374659632986L;
    private Long              id;
    private List<ColumnPair>  columnPairs      = new ArrayList<ColumnPair>();
    private Long              dataMediaPairId;
    private Date              gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<ColumnPair> getColumnPairs() {
        return columnPairs;
    }

    public void setColumnPairs(List<ColumnPair> columnPairs) {
        this.columnPairs = columnPairs;
    }

    public Long getDataMediaPairId() {
        return dataMediaPairId;
    }

    public void setDataMediaPairId(Long dataMediaPairId) {
        this.dataMediaPairId = dataMediaPairId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
