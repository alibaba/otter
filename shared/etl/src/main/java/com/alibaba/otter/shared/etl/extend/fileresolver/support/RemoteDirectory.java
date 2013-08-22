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

package com.alibaba.otter.shared.etl.extend.fileresolver.support;

import java.io.IOException;

/**
 * @author zebin.xuzb 2013-2-25 上午10:51:14
 * @since 4.1.7
 */
public interface RemoteDirectory {

    public String getPath();

    /**
     * 删除目录
     * 
     * @return
     * @throws IOException
     */
    public boolean delete() throws IOException;

    /**
     * 判断目录是否存在。
     * 
     * @return 如果目录存在返回true，否则false
     */
    public boolean exists();

    /**
     * 返回当前目录下的文件列表
     * 
     * @return
     * @throws IOException
     */
    public String[] listFiles() throws IOException;
}
