package com.alibaba.otter.node.etl.common.io.crypto;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * DES exception
 */
public class AESException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public AESException(String errorCode) {
        super(errorCode);
    }

    public AESException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public AESException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public AESException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    public AESException(Throwable cause) {
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
