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

import static com.alibaba.citrus.util.StringUtil.trimToNull;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.util.StringUtil;

public class ActionProtectedImpl implements ActionProtected {

    private static final Logger      logger = LoggerFactory.getLogger(ActionProtectedImpl.class);
    public List<ActionPatternHolder> actionPatternList;
    private String                   actionParam;

    public ActionProtectedImpl(){

    }

    public ActionProtectedImpl(List<ActionPatternHolder> actionPatternList){
        this.actionPatternList = actionPatternList;
    }

    /**
     * 设置在URL query中代表action的参数名。
     */
    public void setActionParam(String actionParam) {
        this.actionParam = trimToNull(actionParam);
    }

    public String getActionParam() {
        return actionParam;
    }

    public boolean check(String action, String method) {
        if (!StringUtil.isBlank(action)) {
            PatternMatcher matcher = new Perl5Matcher();
            Iterator<ActionPatternHolder> iter = actionPatternList.iterator();
            while (iter.hasNext()) {
                ActionPatternHolder holder = (ActionPatternHolder) iter.next();
                if (StringUtils.isNotEmpty(action) && matcher.matches(action, holder.getActionPattern())
                    && StringUtils.isNotEmpty(method) && matcher.matches(method, holder.getMethodPattern())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Candidate is: '" + action + "|" + method + "'; pattern is "
                                     + holder.getActionName() + "|" + holder.getMethodName() + "; matched=true");
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public void setActionPatternList(List<ActionPatternHolder> actionPatternList) {
        this.actionPatternList = actionPatternList;
    }

}
