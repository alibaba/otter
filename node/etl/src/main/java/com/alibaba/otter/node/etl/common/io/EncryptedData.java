package com.alibaba.otter.node.etl.common.io;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 加密后的数据对象
 * 
 * @author jianghang 2011-10-9 下午06:10:43
 * @version 4.0.0
 */
public class EncryptedData {

    private final String crc;
    private final byte[] data;
    private final String key;

    public EncryptedData(byte[] data, String key, String crc){
        this.data = data;
        this.key = key;
        this.crc = crc;
    }

    public String getCrc() {
        return crc;
    }

    public byte[] getData() {
        return data;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
