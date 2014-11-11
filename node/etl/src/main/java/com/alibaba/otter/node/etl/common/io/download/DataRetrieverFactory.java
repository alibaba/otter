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

package com.alibaba.otter.node.etl.common.io.download;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.alibaba.otter.node.etl.common.io.download.exception.DataRetrieveException;
import com.alibaba.otter.node.etl.common.io.download.impl.aria2c.Aria2cRetriever;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter.RetrieverType;

/**
 * DataRetriever工厂，选择合适的下载器
 * 
 * @author jianghang 2011-11-3 下午07:42:05
 * @version 4.0.0
 */
public class DataRetrieverFactory {

    public DataRetriever createRetriever(RetrieverType type, String url, String targetDir) {
        if (type.isAria2c()) {
            return getAria2cRetriever(type.getExe(), url, targetDir);
        } else {
            // 其他的类型
        }

        throw new DataRetrieveException("no DataRetriever for[" + type + "]");
    }

    private DataRetriever getAria2cRetriever(String cmd, String url, String targetDir) {
        if (StringUtils.isEmpty(cmd)) {
            cmd = (SystemUtils.IS_OS_WINDOWS ? cmd + ".exe" : cmd);
        }
        return new Aria2cRetriever(cmd, url, targetDir);
    }
}
