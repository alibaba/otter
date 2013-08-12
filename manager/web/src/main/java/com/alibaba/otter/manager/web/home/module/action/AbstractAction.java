package com.alibaba.otter.manager.web.home.module.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 类AbstractAction.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-12-1 下午03:58:27
 */
public class AbstractAction {

    protected String urlEncode(String character) {
        String result = null;
        try {
            result = (null == character ? "" : URLEncoder.encode(character, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
