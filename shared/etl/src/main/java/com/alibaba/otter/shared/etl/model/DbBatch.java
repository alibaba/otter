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

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 基于数据库的同步记录对象
 * 
 * @author xiaoqing.zhouxq
 */
public class DbBatch implements Serializable {

    private static final long serialVersionUID = 1716704802567430638L;

    private RowBatch          rowBatch;                               // 如果目标端是db，则一定不为空

    private FileBatch         fileBatch;                              // 可能没有附件

    private File              root;                                   // attachment的根路径，如果存在fileBatch一定存在attachment

    public DbBatch(){

    }

    public DbBatch(RowBatch rowBatch){
        this.rowBatch = rowBatch;
        this.fileBatch = new FileBatch();
        this.fileBatch.setIdentity(rowBatch.getIdentity());
    }

    public DbBatch(RowBatch rowBatch, FileBatch fileBatch, File root){
        this.rowBatch = rowBatch;
        this.fileBatch = fileBatch;
        this.root = root;
    }

    public RowBatch getRowBatch() {
        return rowBatch;
    }

    public void setRowBatch(RowBatch rowBatch) {
        this.rowBatch = rowBatch;
    }

    public FileBatch getFileBatch() {
        return fileBatch;
    }

    public void setFileBatch(FileBatch fileBatch) {
        this.fileBatch = fileBatch;
    }

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
