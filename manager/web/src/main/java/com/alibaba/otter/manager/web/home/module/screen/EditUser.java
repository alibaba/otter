package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.user.UserService;
import com.alibaba.otter.shared.common.model.user.User;

public class EditUser {

    @Resource(name = "userService")
    private UserService userService;

    /**
     * 找到单个Channel，用于编辑Channel信息界面加载信息
     * 
     * @param channelId
     * @param context
     * @throws WebxException
     */
    public void execute(@Param("userId") Long userId, @Param("pageIndex") int pageIndex,
                        @Param("searchKey") String searchKey, Context context, Navigator nav) throws Exception {
        User user = userService.findUserById(userId);

        context.put("user", user);
        context.put("pageIndex", pageIndex);
        context.put("searchKey", searchKey);
    }

}
