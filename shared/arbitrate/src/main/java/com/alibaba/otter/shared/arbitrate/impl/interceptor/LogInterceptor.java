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

package com.alibaba.otter.shared.arbitrate.impl.interceptor;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过拦截器记录一下调用event事件的相关参数和返回结果
 * 
 * @author jianghang 2011-9-28 下午07:13:40
 * @version 4.0.0
 */
public class LogInterceptor implements MethodInterceptor {

    private static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";
    private static String       MESSAGE     = "\n=======================================\n[Class:{0} , Method:{1} , time:{2} , take:{3}ms]\n{4}Result\r\t{5}\n=======================================";

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = methodInvocation.proceed();
        } catch (Exception e) {
            dump(methodInvocation, e, System.currentTimeMillis() - startTime);// 记录异常信息
            throw e;
        }
        // 记录异常信息
        dump(methodInvocation, result, System.currentTimeMillis() - startTime); // 记录正常结果信息
        return result;
    }

    /**
     * 取得对应的logger
     * 
     * @param obj
     */
    protected Logger getLogger(Class obj) {
        return LoggerFactory.getLogger(obj);
    }

    /**
     * 记录请求信息
     * 
     * @param methodInvocation
     * @param take
     */
    private void dump(MethodInvocation methodInvocation, Object result, long take) {
        // 取得日志打印对象
        Logger log = getLogger(methodInvocation.getMethod().getDeclaringClass());
        Object[] args = methodInvocation.getArguments();
        StringBuffer buffer = getArgsString(args);

        if (log.isInfoEnabled()) {
            String className = ClassUtils.getShortClassName(methodInvocation.getMethod().getDeclaringClass());
            String methodName = methodInvocation.getMethod().getName();
            String resultStr = getResultString(result);

            String now = new SimpleDateFormat(DATA_FORMAT).format(new Date());
            log.info(MessageFormat.format(MESSAGE, new Object[] { className, methodName, now, take, buffer.toString(),
                    resultStr }));
        }
    }

    /**
     * 取得结果字符串
     * 
     * @param result
     * @return
     */
    protected String getResultString(Object result) {
        if (result == null) {
            return StringUtils.EMPTY;
        }

        if (result instanceof Map) { // 处理map
            return getMapResultString((Map) result);
        } else if (result instanceof List) {// 处理list
            return getListResultString((List) result);
        } else if (result.getClass().isArray()) {// 处理array
            return getArrayResultString((Object[]) result);
        } else {
            // 直接处理string
            return ObjectUtils.toString(result, StringUtils.EMPTY).toString();
            // return ToStringBuilder.reflectionToString(result, ToStringStyle.SIMPLE_STYLE);
        }
    }

    /**
     * 取得map的string，自定义的主要目的：针对value中数组数据的toString处理, copy from {@link AbstractMap}
     * 
     * @param result
     * @return
     */
    private String getMapResultString(Map result) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry> i = result.entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }
        sb.append('{');
        for (;;) {
            Entry e = i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            // 注意: 修改为getResultString(e)进行递归处理
            sb.append(key == this ? "(this Map)" : getResultString(key));
            sb.append('=');
            // 注意: 修改为getResultString(e)进行递归处理
            sb.append(value == this ? "(this Map)" : getResultString(value));
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(", ");
        }
    }

    /**
     * 取得list的string，自定义的主要目的：针对value中数组数据的toString处理, copy from {@link AbstractCollection}
     * 
     * @param result
     * @return
     */
    private String getListResultString(List result) {
        StringBuilder sb = new StringBuilder();
        Iterator i = result.iterator();
        if (!i.hasNext()) {
            return "[]";
        }
        sb.append('[');
        for (;;) {
            Object e = i.next();
            // 注意: 修改为getResultString(e)进行递归处理
            sb.append(e == this ? "(this Collection)" : getResultString(e));
            if (!i.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(", ");
        }
    }

    /**
     * 取得array的string，自定义的主要目的：针对value中数组数据的toString处理
     * 
     * @param result
     * @return
     */
    private String getArrayResultString(Object[] result) {
        return getListResultString(Arrays.asList(result));
    }

    /**
     * 取得参数字符串
     * 
     * @param args
     * @return
     */
    private StringBuffer getArgsString(Object[] args) {
        StringBuffer buffer = new StringBuffer();
        String prefix = "args ";
        for (int i = 0; i < args.length; i++) {
            if (args.length > 1) {
                buffer.append(prefix + (i + 1));
            }
            buffer.append("\r\t");
            buffer.append(getResultString(args[i]));
            buffer.append("\n");
        }
        return buffer;
    }

}
