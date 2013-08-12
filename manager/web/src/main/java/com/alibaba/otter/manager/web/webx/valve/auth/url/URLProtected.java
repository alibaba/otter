package com.alibaba.otter.manager.web.webx.valve.auth.url;


/**
 * 基于url的匹配
 * 
 * @author jianghang 2011-11-11 下午12:30:43
 * @version 4.0.0
 */
public interface URLProtected {

    public boolean check(String requestUrl);
}
