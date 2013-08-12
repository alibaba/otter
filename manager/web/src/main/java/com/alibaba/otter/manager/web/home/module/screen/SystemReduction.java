package com.alibaba.otter.manager.web.home.module.screen;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;

/**
 * @author simon 2011-10-25 上午10:00:32
 */
public class SystemReduction {

    private static final Logger    logger = LoggerFactory.getLogger(SystemReduction.class);

    @Resource(name = "channelService")
    private ChannelService         channelService;

    @Resource(name = "arbitrateManageService")
    private ArbitrateManageService arbitrateManageService;

    public void execute(@Param("command") String command, Context context) throws Exception {
        @SuppressWarnings("unchecked")
        String resultStr = "";

        if ("true".equals(command)) {

            List<Channel> channels = channelService.listAll();

            try {
                // 初始化根节点
                arbitrateManageService.systemEvent().init();

                // 遍历所有的Channel节点
                for (Channel channel : channels) {
                    // 在ZK中初始化每个channel节点
                    arbitrateManageService.channelEvent().init(channel.getId());

                    // 在ZK中初始化该channel下的pipeline节点
                    List<Pipeline> pipelines = channel.getPipelines();
                    //
                    for (Pipeline pipeline : pipelines) {
                        arbitrateManageService.pipelineEvent().init(pipeline.getChannelId(), pipeline.getId());
                    }
                }

                resultStr = "恭喜！Zookeeper节点数据已经补全";
            } catch (ArbitrateException ae) {
                logger.error("ERROR ## init zookeeper has a problem ", ae);
                resultStr = "出错了！回复zookeeper的时候遇到问题！";
            } catch (Exception e) {
                logger.error("ERROR ## init zookeeper has a problem ", e);
                resultStr = "出错了！回复zookeeper的时候遇到问题！";
            }

        }

        context.put("resultStr", resultStr);

    }
}
