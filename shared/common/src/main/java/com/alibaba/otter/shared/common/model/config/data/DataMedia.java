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

package com.alibaba.otter.shared.common.model.config.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 同步数据存储介质
 * 
 * @author jianghang 2011-9-2 上午11:25:37
 */
public class DataMedia<Source extends DataMediaSource> implements Serializable {

    private static final long   serialVersionUID = -7161158767271516776L;
    private Long                id;
    private String              namespace;
    private String              name;                                    // 介质名称
    private Source              source;                                  // 介质源地址信息
    private String              encode;                                  // 编码
    private Date                gmtCreate;
    private Date                gmtModified;
    // 运行时计算出来的属性，避免每次通过ConfigHelper进行正则解析
    private transient ModeValue nameMode;
    private transient ModeValue namespaceMode;

    @Deprecated
    private Mode                mode;                                    // 使用ModeValue进行替代

    public static enum Mode {
        SINGLE(0), MULTI(1), WILDCARD(3);

        private int value; // datamedia pair定义排序用

        Mode(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public boolean isSingle() {
            return this == Mode.SINGLE;
        }

        public boolean isMulti() {
            return this == Mode.MULTI;
        }

        public boolean isWildCard() {
            return this == Mode.WILDCARD;
        }

    }

    // 模式，比如offer[1-128]代表offer1...offer128，128个配置定义
    public static class ModeValue implements Serializable {

        private static final long serialVersionUID = 54902778821522113L;
        private Mode              mode;
        private List<String>      values           = new ArrayList<String>();

        public ModeValue(Mode mode, List<String> values){
            this.mode = mode;
            this.values = values;
        }

        public String getSingleValue() {
            Assert.notEmpty(values);
            return values.get(0);
        }

        public List<String> getMultiValue() {
            return values;
        }

        public Mode getMode() {
            return mode;
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public Mode getMode() {
        if (mode == null) {// 重新计算下
            mode = ConfigHelper.parseMode(namespace).getMode();
        }

        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public ModeValue getNameMode() {
        if (nameMode == null) {
            nameMode = ConfigHelper.parseMode(name);
        }

        return nameMode;
    }

    public void setNameMode(ModeValue nameMode) {
        this.nameMode = nameMode;
    }

    public ModeValue getNamespaceMode() {
        if (namespaceMode == null) {
            namespaceMode = ConfigHelper.parseMode(namespace);
        }

        return namespaceMode;
    }

    public void setNamespaceMode(ModeValue namespaceMode) {
        this.namespaceMode = namespaceMode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
