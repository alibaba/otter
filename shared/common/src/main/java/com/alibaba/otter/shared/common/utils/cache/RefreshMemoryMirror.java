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

package com.alibaba.otter.shared.common.utils.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;
import com.google.common.collect.MapMaker;

/**
 * 简单内存镜像实现
 * 
 * <pre>
 * 1. 使用hashMap做为存储
 * 2. 支持过期时间
 * 3. 支持ComputeFunction，在get结果为null允许进行回调处理
 * 
 * 后期改进：优化下并行处理性能，目前比较暴力加了synchronized
 * </pre>
 * 
 * @author jianghang 2011-10-9 下午01:08:46
 * @version 4.0.0
 */
public class RefreshMemoryMirror<KEY, VALUE> {

    private final Long                              period;
    private final Map<String, RefreshObject<VALUE>> store;
    private final ComputeFunction<KEY, VALUE>       function;

    public RefreshMemoryMirror(Long period, ComputeFunction<KEY, VALUE> function){
        this.period = period;
        this.function = function;
        store = new MapMaker().makeMap();
    }

    public synchronized VALUE get(KEY key) {
        RefreshObject<VALUE> object = store.get(getKey(key));

        if (object == null) {// 记录为空,直接返回null
            VALUE result = function.apply(key, null);
            put(key, result);
            return result;
        } else {
            if (isExpired(object)) { // 判断是否过期,过期清空数据
                VALUE result = function.apply(key, object.getValue());
                put(key, result);
                return result;
            } else {
                return object.getValue();
            }
        }
    }

    public synchronized void put(KEY key, VALUE value) {
        RefreshObject<VALUE> object = new RefreshObject<VALUE>(value);
        store.put(getKey(key), object);
    }

    public synchronized void remove(Object key) {
        store.remove(getKey(key));
    }

    public synchronized void clear() {
        store.clear();
    }

    public Iterator getKeys() {
        return store.keySet().iterator();
    }

    /**
     * 判断对象是否过期
     * 
     * @param refreshObject
     * @return
     */
    private boolean isExpired(RefreshObject refreshObject) {
        if (refreshObject == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(refreshObject.getTimestamp());
        // calendar.add(Calendar.SECOND, period.intValue());
        calendar.add(Calendar.MILLISECOND, period.intValue());

        Date now = new Date();
        return now.after(calendar.getTime());
    }

    /**
     * 取得对应的key String
     * 
     * @param key
     * @return
     */
    private String getKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key not be null");
        }

        return ObjectUtils.toString(key);
    }

    public String toString() {
        return "RefreshMemoryCache[period=" + period + ", size=" + store.size() + "]";
    }

    /**
     * cache失效后重新计算的函数
     */
    public static interface ComputeFunction<KEY, VALUE> {

        VALUE apply(KEY key, VALUE oldValue);
    }

    /**
     * cache对象
     */
    public static class RefreshObject<VALUE> {

        private long  timestamp; // 记录数据存入时间戳
        private VALUE value;    // 记录具体的对象值

        public RefreshObject(VALUE value){
            this.value = value;
            timestamp = new Date().getTime();
        }

        public VALUE getValue() {
            return value;
        }

        public void setValue(VALUE value) {
            this.value = value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, OtterToStringStyle.SIMPLE_STYLE);
        }
    }
}
