package com.alibaba.otter.node.etl.select.exceptions;

/**
 * SelectException for select module.
 * 
 * @author xiaoqing.zhouxq
 */
public class SelectException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SelectException(String cause){
        super(cause);
    }

    public SelectException(Throwable t){
        super(t);
    }

    public SelectException(String cause, Throwable t){
        super(cause, t);
    }

}
