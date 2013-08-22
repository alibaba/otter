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
