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
