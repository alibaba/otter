package com.alibaba.otter.manager.web.webx.valve.auth.action;

import org.apache.oro.text.regex.Pattern;

public class ActionPatternHolder {

    private String  actionName;
    private Pattern actionPattern;
    private String  methodName;
    private Pattern methodPattern;

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Pattern getActionPattern() {
        return actionPattern;
    }

    public void setActionPattern(Pattern actionPattern) {
        this.actionPattern = actionPattern;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Pattern getMethodPattern() {
        return methodPattern;
    }

    public void setMethodPattern(Pattern methodPattern) {
        this.methodPattern = methodPattern;
    }

}
