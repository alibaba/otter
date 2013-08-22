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

package com.alibaba.otter.shared.common.utils;

import java.util.MissingResourceException;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.utils.code.ResourceBundleUtil;

/**
 * @author jianghang 2011-9-13 下午04:45:23
 */
public class ResourceBundleUtilTest extends BaseOtterTest {

    private static final String RESOURCE_LOCATION = "code/ResourceBundleUtil";

    // 测试资源文件没有找到
    @Test
    public void testResourceBundleNotFound() {
        try {
            new ResourceBundleUtil("code/NotFound");
        } catch (RuntimeException e) {
            want.object(e).clazIs(MissingResourceException.class);
        }
    }

    // 测试不同key下的value情况
    @Test
    public void testResourceBundle() {
        ResourceBundleUtil util = new ResourceBundleUtil(RESOURCE_LOCATION);
        // key为空
        want.object(util.getMessage(null)).isNull();
        // key对应的value为空字符串
        want.string(util.getMessage("key1")).isEqualTo("");
        // 静态value的测试
        want.string(util.getMessage("key2")).isEqualTo("value2");
        // 动态渲染的测试
        want.string(util.getMessage("key3", "stone")).isEqualTo("value3,stone");
    }

}
