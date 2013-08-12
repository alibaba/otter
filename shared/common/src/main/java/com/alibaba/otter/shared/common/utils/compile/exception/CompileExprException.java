package com.alibaba.otter.shared.common.utils.compile.exception;

/**
 * 类CompileExprException.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-10-18 上午10:36:05
 * @version 4.1.0
 */
public class CompileExprException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CompileExprException(String message){
        super(message);
    }

    public CompileExprException(String message, Throwable cause){
        super(message, cause);
    }
}
