package com.alibaba.otter.manager.web.webx.valve.auth;

import com.alibaba.otter.manager.web.webx.valve.auth.action.ActionProtected;
import com.alibaba.otter.manager.web.webx.valve.auth.url.URLProtected;

/**
 * 一抽象的接口
 * 
 * @author jianghang 2011-11-11 下午01:11:17
 * @version 4.0.0
 */
public class AuthorizeProtected {

    private URLProtected    urlProtected;
    private ActionProtected actionProtected;

    public URLProtected getUrlProtected() {
        return urlProtected;
    }

    public void setUrlProtected(URLProtected urlProtected) {
        this.urlProtected = urlProtected;
    }

    public ActionProtected getActionProtected() {
        return actionProtected;
    }

    public void setActionProtected(ActionProtected actionProtected) {
        this.actionProtected = actionProtected;
    }

}
