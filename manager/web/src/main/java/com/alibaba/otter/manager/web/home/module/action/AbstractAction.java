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

package com.alibaba.otter.manager.web.home.module.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 类AbstractAction.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-12-1 下午03:58:27
 */
public class AbstractAction {

    protected String urlEncode(String character) {
        String result = null;
        try {
            result = (null == character ? "" : URLEncoder.encode(character, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
