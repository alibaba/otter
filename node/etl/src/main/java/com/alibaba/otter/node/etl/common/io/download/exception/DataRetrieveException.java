package com.alibaba.otter.node.etl.common.io.download.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author jianghang 2011-10-10 下午05:30:17
 * @version 4.0.0
 */
public class DataRetrieveException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public DataRetrieveException(String errorCode){
        super(errorCode);
    }

    public DataRetrieveException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public DataRetrieveException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public DataRetrieveException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public DataRetrieveException(Throwable cause){
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
