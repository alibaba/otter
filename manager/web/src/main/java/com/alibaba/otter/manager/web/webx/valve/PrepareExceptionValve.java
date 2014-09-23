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

package com.alibaba.otter.manager.web.webx.valve;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;
import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;

/**
 * @author jianghang 2011-8-30 下午02:52:04
 */
public class PrepareExceptionValve extends AbstractValve {

    private static Logger       log = LoggerFactory.getLogger(PrepareExceptionValve.class);

    @Autowired
    private HttpServletRequest  request;

    @Autowired
    private HttpServletResponse response;

    @Override
    public void invoke(PipelineContext pipelineContext) throws Exception {
        clearBuffer(response);//
        Exception e = (Exception) pipelineContext.getAttribute("exception");
        log.error(e.getMessage(), e);

        Throwable cause = e.getCause();
        if (cause != null && cause instanceof ArbitrateException) {
            e = (ArbitrateException) cause;
        }

        ErrorHandlerHelper errorHandlerHelper = ErrorHandlerHelper.getInstance(request);
        errorHandlerHelper.setException(e);
        pipelineContext.invokeNext();
    }

    private void clearBuffer(HttpServletResponse response) {
        if (!response.isCommitted()) {
            response.resetBuffer();
        }
    }
}
