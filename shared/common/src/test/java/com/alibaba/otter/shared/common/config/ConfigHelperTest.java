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

package com.alibaba.otter.shared.common.config;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.model.config.ConfigException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia.ModeValue;

public class ConfigHelperTest extends BaseOtterTest {

    @Test
    public void testParse() {
        String v1 = "offer[001-128]";
        ModeValue m1 = ConfigHelper.parseMode(v1);
        want.bool(m1.getMode().isMulti()).is(true);
        want.collection(m1.getMultiValue()).sizeEq(128);

        String v2 = "offer[1-128]test";
        ModeValue m2 = ConfigHelper.parseMode(v2);
        want.bool(m2.getMode().isMulti()).is(true);
        want.collection(m2.getMultiValue()).sizeEq(128);

        String v3 = "[1-128]test";
        ModeValue m3 = ConfigHelper.parseMode(v3);
        want.bool(m3.getMode().isMulti()).is(true);
        want.collection(m3.getMultiValue()).sizeEq(128);

        String v4 = "offer[1-128";
        ModeValue m4 = ConfigHelper.parseMode(v4);
        want.bool(m4.getMode().isWildCard()).is(true);

        String v5 = "offer1-128]";
        ModeValue m5 = ConfigHelper.parseMode(v5);
        want.bool(m5.getMode().isWildCard()).is(true);

        String v6 = "offer1128";
        ModeValue m6 = ConfigHelper.parseMode(v6);
        want.bool(m6.getMode().isSingle()).is(true);
    }

    @Test
    public void testWildCard() {
        PatternMatcher matcher = new Perl5Matcher();

        Pattern pattern = null;
        PatternCompiler pc = new Perl5Compiler();
        try {
            pattern = pc.compile("havana_us_.*", Perl5Compiler.DEFAULT_MASK);
        } catch (MalformedPatternException e) {
            throw new ConfigException(e);
        }
        boolean ismatch = matcher.matches("havana_us_0001", pattern);
        System.out.println(ismatch);
    }
}
