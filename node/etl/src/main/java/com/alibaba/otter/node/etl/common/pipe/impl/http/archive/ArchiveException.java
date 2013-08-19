package com.alibaba.otter.node.etl.common.pipe.impl.http.archive;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * ArchiveException
 */
public class ArchiveException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    private String            errorCode;
    private String            errorDesc;

    public ArchiveException(String errorCode){
        super(errorCode);
    }

    public ArchiveException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public ArchiveException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public ArchiveException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public ArchiveException(Throwable cause){
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
