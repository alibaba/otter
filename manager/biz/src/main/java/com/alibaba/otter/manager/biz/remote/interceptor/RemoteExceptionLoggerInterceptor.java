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

package com.alibaba.otter.manager.biz.remote.interceptor;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.dao.DataAccessException;

import com.alibaba.otter.shared.communication.model.OtterRemoteException;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;

/**
 * 拦截所有的异常调用
 * 
 * @author jianghang 2011-11-28 下午02:20:14
 * @version 4.0.0
 */
public class RemoteExceptionLoggerInterceptor implements ThrowsAdvice {

    private static Logger log = LoggerFactory.getLogger(RemoteExceptionLoggerInterceptor.class);

    public void afterThrowing(Throwable ex) throws Throwable {
        if (log.isErrorEnabled()) {
            log.error("log exception message:", ex);
        }
        String msg = null;
        String stack = null;
        if (ex instanceof ManagerException) {
            msg = ex.getMessage();
            stack = getStackTrace(ex);
        } else if (ex instanceof IllegalArgumentException) {
            msg = ex.getMessage();
            stack = getStackTrace(ex);
        } else if (ex instanceof DataAccessException) {
            msg = ex.getMessage();
            stack = getStackTrace(ex);
        } else if (ex instanceof RuntimeException) {
            msg = ex.getMessage();
            stack = getStackTrace(ex);
        }

        throw new OtterRemoteException(msg, stack);
    }

    /**
     * 打印业务异常堆栈
     */
    private String getStackTrace(Throwable ex) {
        StringWriter out = new StringWriter();
        ex.printStackTrace(new PrintWriter(out));
        return out.getBuffer().toString();
    }
}
