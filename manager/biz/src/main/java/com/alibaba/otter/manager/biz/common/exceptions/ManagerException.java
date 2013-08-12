package com.alibaba.otter.manager.biz.common.exceptions;

/**
 * ManagerException for Manager Model
 * 
 * @author simon 2011-11-13 下午07:38:47
 */
public class ManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ManagerException(String cause){
        super(cause);
    }

    public ManagerException(Throwable t){
        super(t);
    }

    public ManagerException(String cause, Throwable t){
        super(cause, t);
    }

}
