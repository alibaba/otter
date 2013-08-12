package com.alibaba.otter.node.etl.extract.exceptions;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * ExtractException for extract module.
 * 
 * @author xiaoqing.zhouxq
 */
public class ExtractException extends NestableRuntimeException {

    private static final long serialVersionUID = 2680820522662343759L;
    private String            errorCode;
    private String            errorDesc;

    public ExtractException(String errorCode){
        super(errorCode);
    }

    public ExtractException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public ExtractException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public ExtractException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public ExtractException(Throwable cause){
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
