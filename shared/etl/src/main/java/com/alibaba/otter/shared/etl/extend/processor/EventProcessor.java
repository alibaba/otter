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

package com.alibaba.otter.shared.etl.extend.processor;

import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 业务自定义处理过程
 * 
 * @author jianghang 2012-6-25 下午02:26:36
 * @version 4.1.0
 */
public interface EventProcessor {

    /**
     * 自定义处理单条EventData对象，如果要改变数据内容，请直接修改原对象而非new一个新的对象
     * 
     * <pre>
     * EventData数据格式: 
     *    a. schema name / table name
     *    b. eventType : insert/update/delete
     *    c. executeTime : 执行时间
     *    d. keys / oldKeys : 主键字段 (如果是有主键变更，需要带上老主键的信息在oldKeys中)
     *    e. columns :  非主键字段
     * 
     * EventColumn数据格式：
     *    a. index : 字段在数据表中的顺序下标
     *    b. columnType : 对应于sqlType
     *    c. columnName : 字段名字
     *    d. columnValue : 字段类型
     *    e. isKey : 是否为主键
     *    f. isNull : 是否为空值
     * </pre>
     * 
     * @return false需要忽略该条数据，true代表继续处理
     */
    public boolean process(EventData eventData);
}
