package com.alibaba.otter.common.push;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author zebin.xuzb 2013-1-23 下午2:07:28
 * @since 4.1.3
 */
public class PushException extends NestableRuntimeException {

    private static final long serialVersionUID = -1223749329887228066L;

    public PushException(){
        super();
    }

    public PushException(String msg, Throwable cause){
        super(msg, cause);
    }

    public PushException(String msg){
        super(msg);
    }

    public PushException(Throwable cause){
        super(cause);
    }

}
