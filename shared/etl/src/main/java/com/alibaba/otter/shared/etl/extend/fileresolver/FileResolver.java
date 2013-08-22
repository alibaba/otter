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

package com.alibaba.otter.shared.etl.extend.fileresolver;

import java.util.Map;

/**
 * 文件提取接口类，包含附件的业务表需要实现该接口获取记录对应的附件信息.
 * 
 * @author xiaoqing.zhouxq
 */
public interface FileResolver {

    /**
     * Get attachment file, the logic:
     * 
     * @param rowMap key=column_name(UpCase) value=column_value
     * @return FileInfo[] or null
     */
    public FileInfo[] getFileInfo(Map<String, String> rowMap);

    /**
     * 针对数据delete类型是否删除对应文件
     * 
     * @return
     */
    public boolean isDeleteRequired();

    public boolean isDistributed();

}
