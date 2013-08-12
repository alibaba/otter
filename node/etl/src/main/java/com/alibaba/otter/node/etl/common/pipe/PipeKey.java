package com.alibaba.otter.node.etl.common.pipe;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * pipe中数据的唯一标示
 * 
 * @author jianghang 2011-10-13 下午05:26:32
 * @version 4.0.0
 */
public class PipeKey implements Serializable {

    private static final long serialVersionUID = 1543055219365681976L;

    private PipeDataType      dataType;                               // 数据对象类型

    public PipeDataType getDataType() {
        return dataType;
    }

    public void setDataType(PipeDataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
