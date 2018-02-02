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

package com.alibaba.otter.manager.biz.utils;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * @author simon 2012-9-25 下午5:01:48
 * @version 4.1.0
 */
public class RegexUtils {

    private static Map<String, Pattern> patterns = null;

    static {
        patterns = OtterMigrateMap.makeSoftValueComputingMap(new Function<String, Pattern>() {

            public Pattern apply(String pattern) {
                try {
                    PatternCompiler pc = new Perl5Compiler();
                    return pc.compile(pattern, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.READ_ONLY_MASK);
                } catch (MalformedPatternException e) {
                    throw new ManagerException(e);
                }
            }
        });
    }

    public static String findFirst(String originalStr, String regex) {
        if (StringUtils.isBlank(originalStr) || StringUtils.isBlank(regex)) {
            return StringUtils.EMPTY;
        }

        PatternMatcher matcher = new Perl5Matcher();
        if (matcher.contains(originalStr, patterns.get(regex))) {
            return StringUtils.trimToEmpty(matcher.getMatch().group(0));
        }
        return StringUtils.EMPTY;
    }
}
