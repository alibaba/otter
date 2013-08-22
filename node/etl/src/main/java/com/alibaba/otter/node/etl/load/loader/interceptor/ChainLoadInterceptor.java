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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.load.loader.LoadContext;
import com.alibaba.otter.shared.etl.model.ObjectData;

public class ChainLoadInterceptor extends AbstractLoadInterceptor<LoadContext, ObjectData> {

    private List<LoadInterceptor> interceptors = new ArrayList<LoadInterceptor>();

    public void prepare(LoadContext context) {
        if (interceptors == null) {
            return;
        }

        for (LoadInterceptor interceptor : interceptors) {
            interceptor.prepare(context);
        }
    }

    public boolean before(LoadContext context, ObjectData currentData) {
        if (interceptors == null) {
            return false;
        }

        boolean result = false;
        for (LoadInterceptor interceptor : interceptors) {
            result |= interceptor.before(context, currentData);
            if (result) {// 出现一个true就退出
                return result;
            }
        }
        return result;
    }

    public void transactionBegin(LoadContext context, List<ObjectData> currentDatas, DbDialect dialect) {
        if (interceptors == null) {
            return;
        }

        for (LoadInterceptor interceptor : interceptors) {
            interceptor.transactionBegin(context, currentDatas, dialect);
        }
    }

    public void transactionEnd(LoadContext context, List<ObjectData> currentDatas, DbDialect dialect) {
        if (interceptors == null) {
            return;
        }

        for (LoadInterceptor interceptor : interceptors) {
            interceptor.transactionEnd(context, currentDatas, dialect);
        }
    }

    public void after(LoadContext context, ObjectData currentData) {
        if (interceptors == null) {
            return;
        }

        for (LoadInterceptor interceptor : interceptors) {
            interceptor.after(context, currentData);
        }
    }

    public void commit(LoadContext context) {
        if (interceptors == null) {
            return;
        }

        for (LoadInterceptor interceptor : interceptors) {
            interceptor.commit(context);
        }
    }

    public void error(LoadContext context) {
        if (interceptors == null) {
            return;
        }

        for (LoadInterceptor interceptor : interceptors) {
            interceptor.error(context);
        }
    }

    public void setInterceptors(List<LoadInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

}
