package com.alibaba.otter.manager.web.webx.valve.auth.url;

import org.apache.oro.text.regex.Pattern;

/**
 * 封装url pattern匹配
 * 
 * @author jianghang 2011-11-11 下午12:38:06
 * @version 4.0.0
 */
public class URLPatternHolder {

    private String  url;
    private Pattern compiledPattern;

    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    public void setCompiledPattern(Pattern compiledPattern) {
        this.compiledPattern = compiledPattern;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
