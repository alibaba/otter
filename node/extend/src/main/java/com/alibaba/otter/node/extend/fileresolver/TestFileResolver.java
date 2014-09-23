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

package com.alibaba.otter.node.extend.fileresolver;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.shared.etl.extend.fileresolver.FileInfo;

public class TestFileResolver extends AbstractFileResolver {

    public FileInfo[] getFileInfo(Map<String, String> rowMap) {
        // 基本步骤：
        // 1. 获取binlog中的变更字段，比如组成文件有多个字段组成version+path
        // 2. 基于字段内容，构造一个文件路径，目前开源版本只支持本地文件的同步.(如果是网络文件，建议进行NFS mount到ndde机器).
        // 3. 返回FileInfo数组，(目前不支持目录同步，如果是目录需要展开为多个FileInfo的子文件)，如果不需要同步，则返回null.
        String path = rowMap.get("FIELD"); //注意为大写
        FileInfo fileInfo = null;
        if (StringUtils.isNotEmpty(path)) {
            fileInfo = new FileInfo(path);
            return new FileInfo[] { fileInfo };
        } else {
            return null;
        }
    }

}
