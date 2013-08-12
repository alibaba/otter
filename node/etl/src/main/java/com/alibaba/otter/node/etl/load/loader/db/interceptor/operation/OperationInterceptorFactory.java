package com.alibaba.otter.node.etl.load.loader.db.interceptor.operation;

import java.util.List;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlDialect;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleDialect;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.node.etl.load.loader.interceptor.AbstractLoadInterceptor;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * operator的调用工厂
 * 
 * @author jianghang 2011-10-31 下午03:20:16
 * @version 4.0.0
 */
public class OperationInterceptorFactory extends AbstractLoadInterceptor<DbLoadContext, EventData> {

    private DbDialectFactory  dbDialectFactory;
    private LoadInterceptor[] mysqlInterceptors;
    private LoadInterceptor[] oracleInterceptors;
    private LoadInterceptor[] empty = new LoadInterceptor[0];

    public void transactionBegin(DbLoadContext context, List<EventData> currentDatas, DbDialect dialect) {
        LoadInterceptor[] interceptors = getIntercetptor(context, currentDatas);
        for (LoadInterceptor interceptor : interceptors) {
            interceptor.transactionBegin(context, currentDatas, dialect);
        }
    }

    public void transactionEnd(DbLoadContext context, List<EventData> currentDatas, DbDialect dialect) {
        LoadInterceptor[] interceptors = getIntercetptor(context, currentDatas);
        for (LoadInterceptor interceptor : interceptors) {
            interceptor.transactionEnd(context, currentDatas, dialect);
        }
    }

    private LoadInterceptor[] getIntercetptor(DbLoadContext context, List<EventData> currentData) {
        if (currentData == null || currentData.size() == 0) {
            return empty;
        }
        DataMedia dataMedia = ConfigHelper.findDataMedia(context.getPipeline(), currentData.get(0).getTableId());
        DbDialect dbDialect = dbDialectFactory.getDbDialect(context.getIdentity().getPipelineId(),
            (DbMediaSource) dataMedia.getSource());

        if (dbDialect instanceof MysqlDialect) {
            return mysqlInterceptors;
        } else if (dbDialect instanceof OracleDialect) {
            return oracleInterceptors;
        } else {
            return empty;
        }
    }

    // ===================== setter / getter =========================

    public void setMysqlInterceptors(LoadInterceptor[] mysqlInterceptors) {
        this.mysqlInterceptors = mysqlInterceptors;
    }

    public void setOracleInterceptors(LoadInterceptor[] oracleInterceptors) {
        this.oracleInterceptors = oracleInterceptors;
    }

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

}
