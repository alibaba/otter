package com.alibaba.otter.shared.common.model.autokeeper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

public class AutoKeeperStat {

    private String originalContent; // 原始的zk返回的文本信息

    public String getOriginalContent() {
        return originalContent;
    }

    public String getHtmlOriginalContent() {
        return StringUtils.replace(originalContent, "\n", "<br>");
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
