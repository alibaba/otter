package com.alibaba.otter.manager.web.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.canal.instance.manager.model.CanalParameter.DataSourcing;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.SourcingType;

/**
 * 格式化一下页面输出的数字内容
 * 
 * @author jianghang 2011-12-1 下午01:53:15
 * @version 4.0.0
 */
public class NumberFormatUtil {

    private static final String PATTERN = "#,###.###";
    private static final Long   KB_SIZE = 1024L;
    private static final Long   MB_SIZE = 1024 * KB_SIZE;
    private static final Long   GB_SIZE = 1024 * MB_SIZE;
    private static final Long   TB_SIZE = 1024 * GB_SIZE;

    public static String format(Double data) {
        if (data == null) {
            return null;
        }
        DecimalFormat format = new DecimalFormat(PATTERN);
        return format.format(data);
    }

    public static String format(Integer data) {
        if (data == null) {
            return null;
        }
        DecimalFormat format = new DecimalFormat(PATTERN);
        return format.format(data);
    }

    public static String format(Long data) {
        if (data == null) {
            return null;
        }
        DecimalFormat format = new DecimalFormat(PATTERN);
        return format.format(data);
    }

    public static String format(BigDecimal data) {
        if (data == null) {
            return null;
        }

        DecimalFormat format = new DecimalFormat(PATTERN);
        return format.format(data);
    }

    public static String format(BigInteger data) {
        if (data == null) {
            return null;
        }

        DecimalFormat format = new DecimalFormat(PATTERN);
        return format.format(data);
    }

    public static String formatFileSize(Number data) {
        if (data == null) {
            return null;
        }

        long size = data.longValue();
        if (size > TB_SIZE) {
            DecimalFormat format = new DecimalFormat(PATTERN);
            return format.format((size * 1.0) / TB_SIZE) + " TB";
        } else if (size > GB_SIZE) {
            DecimalFormat format = new DecimalFormat(PATTERN);
            return format.format((size * 1.0) / GB_SIZE) + " GB";
        } else if (size > MB_SIZE) {
            DecimalFormat format = new DecimalFormat(PATTERN);
            return format.format((size * 1.0) / MB_SIZE) + " MB";
        } else if (size > KB_SIZE) {
            DecimalFormat format = new DecimalFormat(PATTERN);
            return format.format((size * 1.0) / KB_SIZE) + " KB";
        } else {
            DecimalFormat format = new DecimalFormat(PATTERN);
            return format.format(size) + " B";
        }

    }

    public String formatGroupDbAddress(SourcingType defaultType, List<List<DataSourcing>> groupDbAddresses) {
        StringBuilder builder = new StringBuilder();
        for (List<DataSourcing> groupDbAddress : groupDbAddresses) {
            List<String> address = new ArrayList<String>();
            for (DataSourcing dbAddress : groupDbAddress) {
                StringBuilder dbAddressBuilder = new StringBuilder();
                dbAddressBuilder.append(dbAddress.getDbAddress().getAddress().getHostAddress());
                dbAddressBuilder.append(":");
                dbAddressBuilder.append(String.valueOf(dbAddress.getDbAddress().getPort()));
                if (!defaultType.equals(dbAddress.getType())) {
                    dbAddressBuilder.append(":").append(dbAddress.getType().name());
                }

                address.add(dbAddressBuilder.toString());
            }
            builder.append(StringUtils.join(address, ',')).append(";");
        }

        return builder.toString();
    }

    public String getHtmlOriginalContent(String originalContent) {
        originalContent = StringUtils.replace(originalContent, "\n", "<br>");
        originalContent = StringUtils.replace(originalContent, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        return StringEscapeUtils.escapeJavaScript(originalContent);
    }

    public String getHtmlOriginalContent(String originalContent, String escape) {
        originalContent = StringUtils.replace(originalContent, "\n", "<br>");
        originalContent = StringEscapeUtils.escapeJavaScript(originalContent);
        if ("HTML".equalsIgnoreCase(escape)) {
            originalContent = StringEscapeUtils.escapeHtml(originalContent);
            originalContent = StringUtils.replace(originalContent, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            originalContent = StringUtils.replace(originalContent, "\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            return originalContent;
        } else {
            return originalContent;
        }
    }
}
