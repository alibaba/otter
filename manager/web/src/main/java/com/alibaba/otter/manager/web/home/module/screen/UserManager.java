package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.shared.common.model.user.User;
import com.alibaba.otter.manager.biz.user.UserService;

/**
 * 类AddDataMedia.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-25 上午10:00:32
 */
public class UserManager {

    @Resource(name = "userService")
    private UserService userService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey, Context context)
                                                                                                                 throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("支持ID、用户名、真实姓名、部门搜索".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = userService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<User> users = userService.listByCondition(condition);
        context.put("users", users);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
    }
}
