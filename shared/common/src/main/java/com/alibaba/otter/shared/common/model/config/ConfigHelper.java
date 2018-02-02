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

package com.alibaba.otter.shared.common.model.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMedia.Mode;
import com.alibaba.otter.shared.common.model.config.data.DataMedia.ModeValue;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * 常用的config处理帮助类
 * 
 * @author jianghang 2011-10-20 下午05:28:39
 * @version 4.0.0
 */
public class ConfigHelper {

    public static final String          MODE_PATTERN = "(.*)(\\[(\\d+)\\-(\\d+)\\])(.*)"; // 匹配offer[1-128]
    private static Map<String, Pattern> patterns     = OtterMigrateMap.makeComputingMap(new Function<String, Pattern>() {

                                                         public Pattern apply(String input) {
                                                             PatternCompiler pc = new Perl5Compiler();
                                                             try {
                                                                 return pc.compile(input,
                                                                     Perl5Compiler.CASE_INSENSITIVE_MASK
                                                                             | Perl5Compiler.READ_ONLY_MASK);
                                                             } catch (MalformedPatternException e) {
                                                                 throw new ConfigException(e);
                                                             }
                                                         }
                                                     });

    /**
     * 根据DataMedia id得到对应的DataMedia
     */
    public static DataMedia<? extends DataMediaSource> findDataMedia(Pipeline pipeline, Long id) {
        Assert.notNull(pipeline);
        for (DataMediaPair pair : pipeline.getPairs()) {
            if (pair.getSource().getId().equals(id)) {
                return pair.getSource();
            } else if (pair.getTarget().getId().equals(id)) {
                return pair.getTarget();
            }
        }

        throw new ConfigException("no such DataMedia , the tableId = " + id);
    }

    /**
     * 根据NameSpace和Name得到对应的DataMedia.
     */
    public static DataMedia<? extends DataMediaSource> findSourceDataMedia(Pipeline pipeline, String namespace,
                                                                           String name) {
        return findSourceDataMedia(pipeline, namespace, name, false);
    }

    /**
     * 根据NameSpace和Name得到对应的DataMedia
     */
    public static DataMedia<? extends DataMediaSource> findSourceDataMedia(Pipeline pipeline, String namespace,
                                                                           String name, boolean notExistReturnNull) {
        for (DataMediaPair pair : pipeline.getPairs()) {
            if (isMatch(pair.getSource(), namespace, name)) {
                return pair.getSource();
            }
        }

        if (notExistReturnNull) {
            return null;
        } else {
            throw new ConfigException("no such DataMedia , the namespace = " + namespace + " name = " + name);
        }
    }

    /**
     * 根据NameSpace和Name得到对应的DataMediaPair.
     */
    public static DataMediaPair findDataMediaPairBySourceName(Pipeline pipeline, String namespace, String name) {
        return findDataMediaPairBySourceName(pipeline, namespace, name, false);
    }

    /**
     * 根据NameSpace和Name得到对应的DataMediaPair
     */
    public static DataMediaPair findDataMediaPairBySourceName(Pipeline pipeline, String namespace, String name,
                                                              boolean notExistReturnNull) {
        for (DataMediaPair pair : pipeline.getPairs()) {
            if (isMatch(pair.getSource(), namespace, name)) {
                return pair;
            }
        }

        if (notExistReturnNull) {
            return null;
        } else {
            throw new ConfigException("no such DataMedia , the namespace = " + namespace + " name = " + name);
        }
    }

    /**
     * 根据DataMedia id得到对应的DataMediaPair
     */
    public static List<DataMediaPair> findDataMediaPairByMediaId(Pipeline pipeline, Long tid) {
        Assert.notNull(pipeline);
        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        for (DataMediaPair pair : pipeline.getPairs()) {
            if (pair.getSource().getId().equals(tid)) {
                pairs.add(pair);
            } else if (pair.getTarget().getId().equals(tid)) {
                pairs.add(pair);
            }
        }

        return pairs;
    }

    /**
     * 根据DataMedia id得到对应的DataMediaPair
     */
    public static DataMediaPair findDataMediaPair(Pipeline pipeline, Long pairId) {
        Assert.notNull(pipeline);
        for (DataMediaPair pair : pipeline.getPairs()) {
            if (pair.getId().equals(pairId)) {
                return pair;
            }
        }

        throw new ConfigException("no such DataMediaPair , the pairId = " + pairId);
    }

    /**
     * 解析DataMedia中的namespace和name，支持offer[1-128]分库的定义
     */
    public static ModeValue parseMode(String value) {
        PatternMatcher matcher = new Perl5Matcher();
        if (matcher.matches(value, patterns.get(MODE_PATTERN))) {
            MatchResult matchResult = matcher.getMatch();
            String prefix = matchResult.group(1);
            String startStr = matchResult.group(3);
            String ednStr = matchResult.group(4);
            int start = Integer.valueOf(startStr);
            int end = Integer.valueOf(ednStr);
            String postfix = matchResult.group(5);

            List<String> values = new ArrayList<String>();
            for (int i = start; i <= end; i++) {
                StringBuilder builder = new StringBuilder(value.length());
                String str = String.valueOf(i);
                // 处理0001类型
                if (startStr.length() == ednStr.length() && startStr.startsWith("0")) {
                    str = StringUtils.leftPad(String.valueOf(i), startStr.length(), '0');
                }

                builder.append(prefix).append(str).append(postfix);
                values.add(builder.toString());
            }
            return new ModeValue(Mode.MULTI, values);
        } else if (isWildCard(value)) {// 通配符支持
            return new ModeValue(Mode.WILDCARD, Arrays.asList(value));
        } else {
            return new ModeValue(Mode.SINGLE, Arrays.asList(value));
        }
    }

    public static String makeSQLPattern(String rawValue) {
        return makeSQLPattern(parseMode(rawValue), rawValue);
    }

    public static String makeSQLPattern(ModeValue mode, String rawValue) {
        Assert.notNull(mode);
        Assert.notNull(rawValue);
        if (mode.getMode().isSingle()) {
            return rawValue;
        } else if (mode.getMode().isMulti()) {
            return StringUtils.substringBefore(rawValue, "[") + "%";
        } else if (mode.getMode().isWildCard()) {
            StringBuilder sb = new StringBuilder(rawValue.length());
            FOR_LOOP: for (int i = 0; i < rawValue.length(); i++) {
                String charString = String.valueOf(rawValue.charAt(i));
                if (isWildCard(charString)) {
                    break FOR_LOOP;
                } else {
                    sb.append(rawValue.charAt(i));
                }
            }
            return sb.toString() + "%";
        } else {
            throw new UnsupportedOperationException("unsupport mode:" + mode.getMode());
        }
    }

    public static ModeValueFilter makeModeValueFilter(final ModeValue mode, final String rawValue) {
        Assert.notNull(mode);
        Assert.notNull(rawValue);
        if (mode.getMode().isSingle()) {
            return new ModeValueFilter() {

                @Override
                public boolean accept(String value) {
                    return rawValue.equalsIgnoreCase(value);
                }
            };
        } else if (mode.getMode().isWildCard()) {
            return new ModeValueFilter() {

                @Override
                public boolean accept(String value) {
                    return isWildCardMatch(rawValue, value);
                }
            };
        } else if (mode.getMode().isMulti()) {
            return new ModeValueFilter() {

                @Override
                public boolean accept(String value) {
                    return (indexIgnoreCase(mode.getMultiValue(), value) != -1);
                }
            };
        } else {
            throw new UnsupportedOperationException("unsupport mode:" + mode.getMode());
        }
    }

    // ===================== helper method ================

    private static boolean isMatch(DataMedia dataMedia, String namespace, String name) {
        boolean isMatch = true;
        if (StringUtils.isEmpty(namespace)) {
            isMatch &= StringUtils.isEmpty(dataMedia.getNamespace());
        } else {
            if (dataMedia.getNamespaceMode().getMode().isSingle()) {
                isMatch &= dataMedia.getNamespace().equalsIgnoreCase(namespace);
            } else if (dataMedia.getNamespaceMode().getMode().isMulti()) {
                isMatch &= (indexIgnoreCase(dataMedia.getNamespaceMode().getMultiValue(), namespace) != -1);
            } else if (dataMedia.getNamespaceMode().getMode().isWildCard()) {
                isMatch &= isWildCardMatch(dataMedia.getNamespace(), namespace);
            } else {
                throw new UnsupportedOperationException("unsupport mode:" + dataMedia.getNameMode().getMode());
            }
        }

        if (StringUtils.isEmpty(name)) {
            isMatch &= StringUtils.isEmpty(dataMedia.getName());
        } else {
            if (dataMedia.getNameMode().getMode().isSingle()) {
                isMatch &= dataMedia.getName().equalsIgnoreCase(name);
            } else if (dataMedia.getNameMode().getMode().isMulti()) {
                isMatch &= (indexIgnoreCase(dataMedia.getNameMode().getMultiValue(), name) != -1);
            } else if (dataMedia.getNameMode().getMode().isWildCard()) {
                isMatch &= isWildCardMatch(dataMedia.getName(), name);
            } else {
                throw new UnsupportedOperationException("unsupport mode:" + dataMedia.getNameMode().getMode());
            }
        }

        return isMatch;
    }

    private static boolean isWildCard(String value) {
        return StringUtils.containsAny(value, new char[] { '*', '?', '+', '|', '(', ')', '{', '}', '[', ']', '\\', '$',
                '^', '.' });
    }

    private static boolean isWildCardMatch(String matchPattern, String value) {
        PatternMatcher matcher = new Perl5Matcher();
        return matcher.matches(value, patterns.get(matchPattern));
    }

    public static int indexIgnoreCase(List<String> datas, String value) {
        for (int i = 0; i < datas.size(); i++) {
            String data = datas.get(i);
            if (data.equalsIgnoreCase(value)) {
                return i;
            }

        }

        return -1;
    }

}
