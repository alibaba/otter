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

package com.alibaba.otter.shared.common.utils.extension.classpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class path 扫描器
 * 
 * @author xiaoqing.zhouxq
 */
public class ClasspathClassScanner implements ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathClassScanner.class);

    public Class<?> scan(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.error("ERROR ## can not found this class ,the name = " + className);
        }

        return null;
    }

}
