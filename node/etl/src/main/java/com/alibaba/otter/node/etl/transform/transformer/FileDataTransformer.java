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

package com.alibaba.otter.node.etl.transform.transformer;

import com.alibaba.otter.shared.etl.model.FileData;

/**
 * {@linkplain FileData}数据对象转化
 * 
 * @author jianghang 2011-10-27 下午06:31:15
 * @version 4.0.0
 */
public class FileDataTransformer extends AbstractOtterTransformer<FileData, FileData> {

    public FileData transform(FileData data, OtterTransformerContext context) {
        // 后续可以针对文件进行目标地的fileResolver解析
        if (context.getDataMediaPair().getId().equals(data.getPairId())) {
            return data;
        } else {
            return null;
        }
        // data.setPairId(context.getDataMediaPair().getId());
        // return data;
    }

}
