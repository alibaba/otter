package com.alibaba.otter.shared.arbitrate.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author jianghang 2011-9-16 下午01:59:25
 * @version 4.0.0
 */
public class ArbitrateException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    public ArbitrateException(String errorCode){
        super(errorCode);
    }

    public ArbitrateException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public ArbitrateException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public ArbitrateException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public ArbitrateException(Throwable cause){
        super(cause);
    }

    public Throwable fillInStackTrace() {
        return this;
    }

}
