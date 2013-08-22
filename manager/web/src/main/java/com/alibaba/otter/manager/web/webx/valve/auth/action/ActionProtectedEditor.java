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

import java.beans.PropertyEditorSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionProtectedEditor extends PropertyEditorSupport {

    private static final Logger logger = LoggerFactory.getLogger(ActionProtectedEditor.class);

    public void setAsText(String text) throws IllegalArgumentException {
        ActionProtected urlProtected = new ActionProtectedImpl(convertTextToPatterns(text));
        setValue(urlProtected);
    }

    /**
     * 把字符串文本转换成一堆Pattern,用于匹配URL
     */
    private List<ActionPatternHolder> convertTextToPatterns(String text) {
        List<ActionPatternHolder> list = new ArrayList<ActionPatternHolder>();
        if (StringUtils.isNotEmpty(text)) {
            BufferedReader br = new BufferedReader(new StringReader(text));
            int counter = 0;
            String line;
            while (true) {
                counter++;
                try {
                    line = br.readLine();
                } catch (IOException ioe) {
                    throw new IllegalArgumentException(ioe.getMessage());
                }
                if (line == null) {
                    break;
                }
                line = StringUtils.trim(line);
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Line " + counter + ": " + line);
                }
                list.add(convertStringToPattern(line));
            }
        }
        return list;
    }

    /**
     * 把字符串转成Pattern和UrlType
     * 
     * @param perl5RegExp
     * @return
     */
    private ActionPatternHolder convertStringToPattern(String line) {
        ActionPatternHolder holder = new ActionPatternHolder();
        String[] strs = org.apache.commons.lang.StringUtils.split(line, "|");
        if (strs.length != 2) {
            throw new IllegalArgumentException("illegal expression: " + line);
        }
        Pattern compiledPattern;
        Perl5Compiler compiler = new Perl5Compiler();
        try {
            holder.setActionName(strs[0]);
            compiledPattern = compiler.compile(strs[0], Perl5Compiler.READ_ONLY_MASK);
            holder.setActionPattern(compiledPattern);
        } catch (MalformedPatternException mpe) {
            throw new IllegalArgumentException("Malformed regular expression: " + strs[0]);
        }

        try {
            holder.setMethodName(strs[1]);
            compiledPattern = compiler.compile(strs[1], Perl5Compiler.READ_ONLY_MASK);
            holder.setMethodPattern(compiledPattern);
        } catch (MalformedPatternException mpe) {
            throw new IllegalArgumentException("Malformed regular expression: " + strs[1]);
        }

        return holder;
    }

}
