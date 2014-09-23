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

package com.alibaba.otter.shared.etl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件数据集合对象
 * 
 * @author jianghang 2012-10-31 下午05:56:01
 * @version 4.1.2
 */
public class FileBatch extends BatchObject<FileData> {

    private static final long serialVersionUID = -520456006652566067L;
    private List<FileData>    files            = new ArrayList<FileData>();

    public List<FileData> getFiles() {
        return files;
    }

    public void setFiles(List<FileData> files) {
        this.files = files;
    }

    public void merge(FileData data) {
        this.files.add(data);
    }

}
