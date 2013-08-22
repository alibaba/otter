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

package com.alibaba.otter.node.etl.common.pipe.impl.http;

import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 基于Http协议下载的pipe key实现
 * 
 * @author jianghang 2011-10-13 下午05:37:24
 * @version 4.0.0
 */
public class HttpPipeKey extends PipeKey {

    private static final long serialVersionUID = 2926519897517494101L;
    private Identity          identity;
    private String            crc;                                    // checksum数字串
    private String            key;                                    // 密钥串
    private String            url;                                    // 数据文件

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

}
