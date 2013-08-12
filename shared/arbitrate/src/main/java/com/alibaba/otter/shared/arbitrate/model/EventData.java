package com.alibaba.otter.shared.arbitrate.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 仲裁器信号传递的基类
 * 
 * @author jianghang 2011-8-22 下午08:19:54
 */
public class EventData implements Serializable {

    private static final long serialVersionUID = -1375302207165601758L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
