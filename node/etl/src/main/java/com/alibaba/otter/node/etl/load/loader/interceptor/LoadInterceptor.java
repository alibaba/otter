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
