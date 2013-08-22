/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
