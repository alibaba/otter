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

package com.alibaba.otter.manager.web.webx.valve.auth.action;

import org.apache.oro.text.regex.Pattern;

public class ActionPatternHolder {

    private String  actionName;
    private Pattern actionPattern;
    private String  methodName;
    private Pattern methodPattern;

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Pattern getActionPattern() {
        return actionPattern;
    }

    public void setActionPattern(Pattern actionPattern) {
        this.actionPattern = actionPattern;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Pattern getMethodPattern() {
        return methodPattern;
    }

    public void setMethodPattern(Pattern methodPattern) {
        this.methodPattern = methodPattern;
    }

}
