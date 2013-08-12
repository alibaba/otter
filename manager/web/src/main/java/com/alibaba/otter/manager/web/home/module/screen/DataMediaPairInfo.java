package com.alibaba.otter.manager.web.home.module.screen;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

public class DataMediaPairInfo {

    @Resource(name = "channelService")
    private ChannelService       channelService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    public void execute(@Param("dataMediaPairId") Long dataMediaPairId, Context context) throws Exception {
        DataMediaPair dataMediaPair = dataMediaPairService.findById(dataMediaPairId);
        Channel channel = channelService.findByPipelineId(dataMediaPair.getPipelineId());

        List<ColumnPair> columnPairs = dataMediaPair.getColumnPairs();
        List<ColumnGroup> columnGroups = dataMediaPair.getColumnGroups();
        // 暂时策略，只拿出list的第一个Group
        ColumnGroup columnGroup = new ColumnGroup();
        if (!CollectionUtils.isEmpty(columnGroups)) {
            columnGroup = columnGroups.get(0);
        }

        context.put("dataMediaPair", dataMediaPair);
        context.put("columnGroup", columnGroup);
        context.put("columnPairs", columnPairs);
        context.put("channelId", channel.getId());
    }
}
