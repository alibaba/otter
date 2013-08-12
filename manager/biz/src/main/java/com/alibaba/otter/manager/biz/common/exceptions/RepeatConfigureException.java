package com.alibaba.otter.manager.biz.common.exceptions;

/**
 * @author simon 2011-11-14 下午11:04:32
 */
public class RepeatConfigureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RepeatConfigureException(String cause){
        super(cause);
    }

    public RepeatConfigureException(Throwable t){
        super(t);
    }

    public RepeatConfigureException(String cause, Throwable t){
        super(cause, t);
    }

}
