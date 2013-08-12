package com.alibaba.otter.node.etl.conflict.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author jianghang 2012-4-12 下午02:59:12
 * @version 4.0.2
 */
public class ConflictException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public ConflictException(String errorCode){
        super(errorCode);
    }

    public ConflictException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public ConflictException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public ConflictException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public ConflictException(Throwable cause){
        super(cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
