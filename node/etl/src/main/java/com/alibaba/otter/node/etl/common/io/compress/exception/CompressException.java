package com.alibaba.otter.node.etl.common.io.compress.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * 压缩异常类
 */
public class CompressException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public CompressException(String errorCode) {
        super(errorCode);
    }

    public CompressException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public CompressException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public CompressException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    public CompressException(Throwable cause) {
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
