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

/**
 * 文件描述信息
 * 
 * @version 4.1.0
 */
public class FileInfo {

    // private File file;
    private long   lastModifiedTime;
    private String namespace;
    private String path;
    private long   size;

    public FileInfo(String path){
        this.namespace = "";
        // this.file = new File(path);
        // this.size = file.length();
        // this.lastModifiedTime = file.lastModified();
        this.path = path;
    }

    public FileInfo(String namespace, String path){
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long getSize() {
        return size;
    }

    public String toString() {
        return "FileInfo [namespace=" + namespace + ", path=" + path + ", size=" + size + ", modifyTime="
               + lastModifiedTime + "]";
    }
}
