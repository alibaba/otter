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

public class URLProtectedEditor extends PropertyEditorSupport {

    private static final Logger logger = LoggerFactory.getLogger(URLProtectedEditor.class);

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        URLProtected urlProtected = new URLProtectedImpl(convertTextToPatterns(text));
        setValue(urlProtected);
    }

    /**
     * 把字符串文本转换成一堆Pattern,用于匹配URL
     */
    private List<URLPatternHolder> convertTextToPatterns(String text) {
        List<URLPatternHolder> list = new ArrayList<URLPatternHolder>();
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
    private URLPatternHolder convertStringToPattern(String line) {
        URLPatternHolder holder = new URLPatternHolder();
        Pattern compiledPattern;
        Perl5Compiler compiler = new Perl5Compiler();
        String perl5RegExp = line;
        try {
            compiledPattern = compiler.compile(perl5RegExp, Perl5Compiler.READ_ONLY_MASK);
            holder.setCompiledPattern(compiledPattern);
        } catch (MalformedPatternException mpe) {
            throw new IllegalArgumentException("Malformed regular expression: " + perl5RegExp);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Added regular expression: " + compiledPattern.getPattern().toString());
        }
        return holder;
    }

}
