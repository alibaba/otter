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

package com.alibaba.otter.manager.web.home.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.stage.ProcessStatService;
import com.alibaba.otter.manager.web.common.model.SeniorChannel;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

public class ChannelList {

    @Resource(name = "channelService")
    private ChannelService         channelService;

    @Resource(name = "processStatService")
    private ProcessStatService     processStatService;

    @Resource(name = "arbitrateManageService")
    private ArbitrateManageService arbitrateManageService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                        @Param("channelStatus") String status, @Param("channelId") Long channelId,
                        @Param("errorType") String errorType, Context context) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();

        if ("请输入关键字(目前支持Channel的ID、名字搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        List<Long> theStatusPks = new ArrayList<Long>();
        if (null != status) {
            List<Long> allChannelPks = channelService.listAllChannelId();

            for (Long channelPk : allChannelPks) {
                ChannelStatus channelStatus = arbitrateManageService.channelEvent().status(channelPk);
                if (channelStatus.equals(ChannelStatus.valueOf(status))) {
                    theStatusPks.add(channelPk);
                }
            }
        }

        int count = channelService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());
        List<Channel> channels = new ArrayList<Channel>();

        if ((null != channelId) && (channelId != 0l)) {
            channels.add(channelService.findById(channelId));
            paginator.setItems(1);
            paginator.setPage(0);
            searchKey = String.valueOf(channelId); // 定义为新的searchKey
        } else {
            channels = channelService.listByConditionWithoutColumn(condition);
        }

        List<SeniorChannel> seniorChannels = new ArrayList<SeniorChannel>();
        for (Channel channel : channels) {
            boolean processEmpty = false;
            List<Pipeline> pipelines = channel.getPipelines();
            for (Pipeline pipeline : pipelines) {
                if (processStatService.listRealtimeProcessStat(channel.getId(), pipeline.getId()).isEmpty()) {
                    processEmpty = true;
                }
            }
            SeniorChannel seniorChannel = new SeniorChannel();
            seniorChannel.setId(channel.getId());
            seniorChannel.setName(channel.getName());
            seniorChannel.setParameters(channel.getParameters());
            seniorChannel.setPipelines(channel.getPipelines());
            seniorChannel.setStatus(channel.getStatus());
            seniorChannel.setDescription(channel.getDescription());
            seniorChannel.setGmtCreate(channel.getGmtCreate());
            seniorChannel.setGmtModified(channel.getGmtModified());
            seniorChannel.setProcessEmpty(processEmpty);
            seniorChannels.add(seniorChannel);
        }

        context.put("channels", seniorChannels);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
        context.put("errorType", errorType);
    }
}
