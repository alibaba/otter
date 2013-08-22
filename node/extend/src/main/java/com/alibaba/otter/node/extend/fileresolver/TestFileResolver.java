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

import com.alibaba.otter.shared.etl.extend.fileresolver.FileInfo;

public class TestFileResolver extends AbstractFileResolver {

    public FileInfo[] getFileInfo(Map<String, String> rowMap) {
        String labelAddress = rowMap.get("REMARKS");
        String arandaAddress = rowMap.get("REMARKS2");
        FileInfo fileInfo = null;
        if (labelAddress != null && labelAddress.length() != 0) {
            if (arandaAddress != null && arandaAddress.length() != 0) {
                fileInfo = new FileInfo(arandaAddress, labelAddress);
            } else {
                fileInfo = new FileInfo(labelAddress);
            }

            return new FileInfo[] { fileInfo };
        } else {
            return null;
        }
    }

}
