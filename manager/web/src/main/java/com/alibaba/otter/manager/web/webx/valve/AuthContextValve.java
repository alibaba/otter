/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.manager.web.webx.valve;

import static com.alibaba.citrus.turbine.util.TurbineUtil.getTurbineRunData;
import static com.alibaba.otter.shared.common.utils.Assert.assertNotNull;
import static com.alibaba.citrus.util.StringUtil.trimToNull;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.manager.web.common.api.ApiAuthService;
import com.alibaba.otter.manager.web.webx.valve.auth.RegExpURLAnalyze;
import com.alibaba.otter.shared.common.model.user.AuthorizeType;
import com.alibaba.otter.shared.common.model.user.User;

/**
 * 权限控制
 * 
 * @author jianghang 2011-11-11 上午11:46:11
 * @version 4.0.0
 */
public class AuthContextValve extends AbstractValve {

    private static final String DEFAULT_ACTION_PARAM_NAME = "action";
    private static final String DEFAULT_EVENT_PATTERN     = "event_submit_do_";
    private static final String IMAGE_BUTTON_SUFFIX_1     = ".x";
    private static final String IMAGE_BUTTON_SUFFIX_2     = ".y";
    private static final String IMAGE_BUTTON_SUFFIX_3     = ".X";
    private static final String IMAGE_BUTTON_SUFFIX_4     = ".Y";

    @Autowired
    private HttpServletRequest  request;
    @Autowired
    private URIBrokerService    uriBrokerService;
    @Autowired
    private RegExpURLAnalyze    urlAnalyze;
    @Autowired
    private ApiAuthService      apiAuthService;

    private String              loginLink                 = WebConstant.OTTER_LOGIN_LINK;
    private String              forbiddenLink             = WebConstant.ERROR_FORBIDDEN_Link;
    private String              redirectParmeter          = "Done";
    private String              actionParam;

    protected void init() throws Exception {
        if (actionParam == null) {
            actionParam = DEFAULT_ACTION_PARAM_NAME;
        }
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);

        // TODO 走 api 的验证
        if (isAPI(rundata)) {
            if (apiAuthService.auth(rundata)) {
                pipelineContext.invokeNext();
            } else {
                redirect(pipelineContext, rundata, forbiddenLink); // TODO 需要转跳到 json 格式的 link
            }
            return;
        }

        // 得到请求URL相对路径(不包含域名/端口信息)
        String requestUrl = rundata.getRequest().getRequestURI();
        List<AuthorizeType> result = urlAnalyze.check(requestUrl);
        String action = StringUtil.toCamelCase(trimToNull(rundata.getParameters().getString(actionParam)));
        String eventName = getEventName();

        // 首先判断是否登录
        User user = (User) rundata.getRequest().getSession().getAttribute(WebConstant.USER_SESSION_KEY);

        if (StringUtils.isNotEmpty(action)) {
            result.addAll(urlAnalyze.check(action, eventName));
        }

        if (result.isEmpty()) {
            // 访问的连接不符合权限匹配规则，跳转到登录页面
            redirect(pipelineContext, rundata, forbiddenLink);
        } else {
            if (null == user) {
                // 如果用户未登录，则判断访问连接的权限匹配集合：
                // 1.如果有高于匿名权限，则跳转到登录页面；
                // 2.如果集合中只包含匿名权限，则通过。
                if (result.contains(AuthorizeType.OPERATOR) || result.contains(AuthorizeType.ADMIN)) {
                    redirect(pipelineContext, rundata, loginLink);
                } else {
                    pipelineContext.invokeNext();
                }
            } else {
                // 如果用户已经登录，则判断访问连接的权限匹配集合：
                // 1.如果权限集合有等于（低于）用户权限，则通过；
                // 2.如果权限集合有高于用户权限，则跳转到登录页面。
                if (compareAuth(user.getAuthorizeType(), result)) {
                    pipelineContext.invokeNext();
                } else {
                    redirect(pipelineContext, rundata, forbiddenLink);
                }
            }
        }
    }

    // TODO 目前先简单实现
    protected boolean isAPI(TurbineRunData rundata) {
        String requestUrl = rundata.getRequest().getRequestURI();
        return StringUtils.containsIgnoreCase(requestUrl, "/api/");
    }

    private boolean compareAuth(AuthorizeType sType, List<AuthorizeType> dTypes) {
        // 如果用户权限为超级管理员，则返回true
        if (sType.isAdmin()) {
            return true;
        }
        // 用户权限与请求权限集合进行比较，如果集合中有高于用户权限，则返回false
        for (AuthorizeType dType : dTypes) {
            if ((sType.isOperator() && dType.isAdmin()) || (sType.isAnonymous() && !dType.isAnonymous())) {
                return false;
            }
        }

        return true;
    }

    public boolean isAuthenticated(TurbineRunData rundata) {
        Map<String, String> user = (Map<String, String>) rundata.getRequest().getSession().getAttribute(WebConstant.OTTER_USER_SESSION_KEY);
        if (user == null) {
            return false;
        }
        return true;
    }

    private void redirect(PipelineContext pipelineContext, TurbineRunData rundata, String uriBroker) {
        URIBroker urlBroker = assertNotNull(uriBrokerService.getURIBroker(uriBroker),
                                            "uriBroker get from loginLink should not be null");
        urlBroker.addQueryData(redirectParmeter, getRequestUrlWithQueryString());
        rundata.setRedirectLocation(urlBroker.render());
        pipelineContext.breakPipeline(Pipeline.TOP_LABEL);
    }

    private String getRequestUrlWithQueryString() {
        StringBuffer requestUrl = request.getRequestURL();
        String queryString = StringUtil.trimToNull(request.getQueryString());
        if (!StringUtil.isBlank(queryString)) {
            requestUrl.append("?").append(queryString);
        }
        return requestUrl.toString();
    }

    /**
     * 取得key=eventSubmit_doXyz, value不为空的参数。
     */
    private String getEventName() {
        String event = null;

        @SuppressWarnings("unchecked")
        Enumeration<String> e = request.getParameterNames();

        while (e.hasMoreElements()) {
            String originalKey = e.nextElement();
            String paramKey = StringUtil.toLowerCaseWithUnderscores(originalKey);

            if (paramKey.startsWith(DEFAULT_EVENT_PATTERN) && !StringUtil.isBlank(request.getParameter(originalKey))) {
                int startIndex = DEFAULT_EVENT_PATTERN.length();
                int endIndex = paramKey.length();

                // 支持<input type="image">
                if (paramKey.endsWith(IMAGE_BUTTON_SUFFIX_1)) {
                    endIndex -= IMAGE_BUTTON_SUFFIX_1.length();
                } else if (paramKey.endsWith(IMAGE_BUTTON_SUFFIX_2)) {
                    endIndex -= IMAGE_BUTTON_SUFFIX_2.length();
                } else if (paramKey.endsWith(IMAGE_BUTTON_SUFFIX_3)) {
                    endIndex -= IMAGE_BUTTON_SUFFIX_3.length();
                } else if (paramKey.endsWith(IMAGE_BUTTON_SUFFIX_4)) {
                    endIndex -= IMAGE_BUTTON_SUFFIX_4.length();
                }

                event = StringUtil.trimToNull(paramKey.substring(startIndex, endIndex));

                if (event != null) {
                    break;
                }
            }
        }

        return event;
    }

    public void setLoginLink(String loginLink) {
        this.loginLink = loginLink;
    }

    public void setRedirectParmeter(String redirectParmeter) {
        this.redirectParmeter = redirectParmeter;
    }

    public void setActionParam(String actionParam) {
        this.actionParam = actionParam;
    }

}
