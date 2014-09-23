package com.alibaba.otter.manager.biz.common.exceptions;

public class InvalidConfigureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static enum INVALID_TYPE {
        DDL, HOME
    }

    private INVALID_TYPE type;

    public InvalidConfigureException(INVALID_TYPE type){
        super(type.name());
        this.type = type;
    }

    public INVALID_TYPE getType() {
        return type;
    }

}
