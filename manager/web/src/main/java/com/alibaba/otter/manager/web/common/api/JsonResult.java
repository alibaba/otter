package com.alibaba.otter.manager.web.common.api;

import java.io.Serializable;

/**
 * @author zebin.xuzb @ 2012-5-18
 */
public class JsonResult implements Serializable {

    private static final long serialVersionUID = -1637537013205539672L;

    private boolean           success;
    private String            errMessage;
    private Object            data;

    public JsonResult(){
    }

    public JsonResult(boolean success){
        this.success = success;
    }

    // ========= setter & getter ==========
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
