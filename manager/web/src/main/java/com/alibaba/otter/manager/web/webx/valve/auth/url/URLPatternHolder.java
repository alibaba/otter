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

package com.alibaba.otter.manager.web.webx.valve.auth.url;

import org.apache.oro.text.regex.Pattern;

/**
 * 封装url pattern匹配
 * 
 * @author jianghang 2011-11-11 下午12:38:06
 * @version 4.0.0
 */
public class URLPatternHolder {

    private String  url;
    private Pattern compiledPattern;

    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    public void setCompiledPattern(Pattern compiledPattern) {
        this.compiledPattern = compiledPattern;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
