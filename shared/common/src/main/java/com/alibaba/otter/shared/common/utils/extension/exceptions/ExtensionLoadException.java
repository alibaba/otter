package com.alibaba.otter.shared.common.utils.extension.exceptions;

/**
 * 类ExtensionLoadException.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-10-23 下午9:19:20
 * @version 4.1.0
 */
public class ExtensionLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExtensionLoadException(String cause){
        super(cause);
    }

    public ExtensionLoadException(Throwable t){
        super(t);
    }

    public ExtensionLoadException(String cause, Throwable t){
        super(cause, t);
    }
}
