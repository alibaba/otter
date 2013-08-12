package com.alibaba.otter.node.etl.common.io.signature;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author jianghang 2011-10-9 下午06:13:29
 * @version 4.0.0
 */
public class ChecksumException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public ChecksumException(String errorCode) {
        super(errorCode);
    }

    public ChecksumException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ChecksumException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public ChecksumException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    public ChecksumException(Throwable cause) {
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
