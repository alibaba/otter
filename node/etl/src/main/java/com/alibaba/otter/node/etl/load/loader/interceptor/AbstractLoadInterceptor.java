package com.alibaba.otter.node.etl.load.loader.interceptor;

import java.util.List;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;

/**
 * 提供接口的默认实现
 * 
 * @author jianghang 2011-11-9 下午06:28:14
 * @version 4.0.0
 */
public class AbstractLoadInterceptor<L, D> implements LoadInterceptor<L, D> {

    public void prepare(L context) {
    }

    public boolean before(L context, D currentData) {
        return false;
    }

    public void transactionBegin(L context, List<D> currentDatas, DbDialect dialect) {
    }

    public void transactionEnd(L context, List<D> currentDatas, DbDialect dialect) {
    }

    public void after(L context, D currentData) {

    }

    public void commit(L context) {
    }

    public void error(L context) {
    }

}
