package com.alibaba.otter.node.etl.load.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author jianghang 2011-9-16 下午01:59:25
 * @version 4.0.0
 */
public class LoadException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public LoadException(String errorCode) {
        super(errorCode);
    }

    public LoadException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public LoadException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public LoadException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    public LoadException(Throwable cause) {
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
