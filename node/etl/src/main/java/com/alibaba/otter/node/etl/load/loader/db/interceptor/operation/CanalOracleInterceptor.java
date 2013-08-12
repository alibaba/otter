package com.alibaba.otter.node.etl.load.loader.db.interceptor.operation;

/**
 * 基于oracle的数据过滤
 * 
 * @author jianghang 2011-10-31 下午02:51:09
 * @version 4.0.0
 */
public class CanalOracleInterceptor extends AbstractOperationInterceptor {

    public static final String mergeofOracleSql     = "merge /*+ use_nl(a b)*/ into {0} a using (select ? as id , ? as {1} from dual) b on (a.id=b.id)"
                                                      + " when matched then update set a.{1}=b.{1}"
                                                      + " when not matched then insert (a.id , a.{1}) values (b.id , b.{1})";

    public static final String mergeofOracleInfoSql = "merge /*+ use_nl(a b)*/ into {0} a using (select ? as id , ? as {1} , ? as {2} from dual) b on (a.id=b.id)"
                                                      + " when matched then update set a.{1}=b.{1} , a.{2}=b.{2}"
                                                      + " when not matched then insert (a.id , a.{1} , a.{2}) values (b.id , b.{1} , b.{2})";

    public CanalOracleInterceptor(){
        super(mergeofOracleSql, mergeofOracleInfoSql);
    }

}
