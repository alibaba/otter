package com.alibaba.otter.shared.communication.core.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author jianghang 2011-9-9 下午05:05:09
 */
public class CommunicationException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public CommunicationException(String errorCode){
        super(errorCode);
    }

    public CommunicationException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public CommunicationException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public CommunicationException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public CommunicationException(Throwable cause){
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
