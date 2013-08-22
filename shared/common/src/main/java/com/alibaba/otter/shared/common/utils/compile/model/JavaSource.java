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

package com.alibaba.otter.shared.common.utils.compile.model;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.shared.common.utils.RegexUtils;

/**
 * @author simon 2012-10-16 上午10:33:58
 * @version 4.1.0
 */
public class JavaSource {

    private String packageName;
    private String className;
    private String source;

    public JavaSource(String sourceString){
        String className = RegexUtils.findFirst(sourceString, "public class (?s).*?{").split("extends")[0].split("implements")[0].replaceAll("public class ",
                                                                                                                                             StringUtils.EMPTY).replace("{",
                                                                                                                                                                        StringUtils.EMPTY).trim();
        String packageName = RegexUtils.findFirst(sourceString, "package (?s).*?;").replaceAll("package ",
                                                                                               StringUtils.EMPTY).replaceAll(";",
                                                                                                                             StringUtils.EMPTY).trim();
        this.packageName = packageName;
        this.className = className;
        this.source = sourceString;
    }

    public JavaSource(String packageName, String className, String source){
        this.packageName = packageName;
        this.className = className;
        this.source = source;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toString() {
        return packageName + "." + className;
    }
}
