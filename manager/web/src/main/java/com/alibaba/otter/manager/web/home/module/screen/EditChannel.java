package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

public class EditChannel {

    @Resource(name = "channelService")
    private ChannelService channelService;

    /**
     * 找到单个Channel，用于编辑Channel信息界面加载信息
     * 
     * @param channelId
     * @param context
     * @throws WebxException
     */
    public void execute(@Param("channelId") Long channelId, @Param("pageIndex") int pageIndex,
                        @Param("searchKey") String searchKey, Context context, Navigator nav) throws Exception {
        Channel channel = channelService.findById(channelId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }
        context.put("channel", channel);
        context.put("pageIndex", pageIndex);
        context.put("searchKey", searchKey);
    }

}
