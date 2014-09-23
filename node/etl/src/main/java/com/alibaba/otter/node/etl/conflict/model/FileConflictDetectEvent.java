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

package com.alibaba.otter.node.etl.conflict.model;

import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.etl.model.FileBatch;

/**
 * 文件冲突检测事件
 * 
 * @author jianghang
 */
public class FileConflictDetectEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private FileBatch         fileBatch;

    public FileConflictDetectEvent(){
        super(ConflictEventType.fileConflictDetect);
    }

    public FileBatch getFileBatch() {
        return fileBatch;
    }

    public void setFileBatch(FileBatch fileBatch) {
        this.fileBatch = fileBatch;
    }

}
