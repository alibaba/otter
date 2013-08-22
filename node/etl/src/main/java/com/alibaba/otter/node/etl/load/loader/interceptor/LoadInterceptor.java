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

package com.alibaba.otter.node.etl.load.loader.interceptor;

import java.util.List;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;

public interface LoadInterceptor<L, D> {

    public void prepare(L context);

    /**
     * 返回值代表是否需要过滤该记录,true即为过滤不处理
     */
    public boolean before(L context, D currentData);

    public void transactionBegin(L context, List<D> currentDatas, DbDialect dialect);

    public void transactionEnd(L context, List<D> currentDatas, DbDialect dialect);

    public void after(L context, D currentData);

    public void commit(L context);

    public void error(L context);
}
