package com.alibaba.otter.manager.web.common;

/**
 * OTTER WEB层的常量。
 * 
 * @author simon
 */
public interface WebConstant {

    /** 在session中保存user用户对象的key。 */
    String OTTER_USER_SESSION_KEY           = "otterUser";

    /** Login页面返回URL的key。 */
    String LOGIN_RETURN_KEY                 = "return";

    /** 登录URL的名字。 */
    String OTTER_LOGIN_LINK                 = "otterLoginLink";

    /** 登记用户URL的名字。 */
    String OTTER_REGISTER_LINK              = "otterRegisterLink";

    /** 登记用户信息URL的名字。 */
    String OTTER_REGISTER_ACCOUNT_LINK      = "otterRegisterAccountLink";

    /** 查看Channel列表。 */
    String CHANNEL_LIST_LINK                = "channelListLink";

    String CHANNEL_LIST_PAGE_LINK           = "channelListPageLink";

    String SELECT_DATA_MEDIA_SOURCE_LINK    = "selectDataMediaSourceLink";

    String SEARCH_DELAY_STAT_LINK           = "searchDelayStatLink";

    /** 进入编辑Channel页面 */
    String CHANNEL_EDIT_LINK                = "channelEditLink";

    String DATA_MEDIA_LIST_LINK             = "dataMediaListLink";

    String NODE_LIST_LINK                   = "nodeListLink";

    String AUTO_KEEPER_CLUSTERS_LINK        = "autoKeeperClustersListLink";

    String AUTO_KEEPER_CLUSTERS_DETAIL_LINK = "autoKeeperClustersDetailLink";

    String DATA_MEDIA_SOURCE_LIST_LINK      = "dataMediaSourceListLink";

    String USER_SESSION_KEY                 = "managerUser";

    String ERROR_FORBIDDEN_Link             = "errorForbiddenLink";

    String USER_MANAGER_LINK                = "userListLink";

    String ANALYSIS_DELAY_STAT_LINK         = "analysisDelayStatLink";

    String ANALYSIS_THROUGHPUT_HISTORY_LINK = "analysisThroughputHistoryLink";

    /** 进入编辑Canal页面 */
    String CANAL_LIST_LINK                  = "canalListLink";

    /** 进入编辑Canal页面 */
    String MATRIX_LIST_LINK                 = "dataMatrixListLink";

}
