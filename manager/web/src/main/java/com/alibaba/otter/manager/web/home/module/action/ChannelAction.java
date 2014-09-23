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

package com.alibaba.otter.manager.web.home.module.action;

import javax.annotation.Resource;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.InvalidConfigureException;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;

/**
 * 类ChannelAction.java的实现描述：用于Channel管理界面的Action
 * 
 * @author simon 2011-10-21 下午02:49:18
 */
public class ChannelAction extends AbstractAction {

    @Resource(name = "channelService")
    private ChannelService  channelService;

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;

    /**
     * 添加Channel
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doAdd(@FormGroup("channelInfo") Group channelInfo,
                      @FormGroup("channelParameterInfo") Group channelParameterInfo,
                      @FormField(name = "formChannelError", group = "channelInfo") CustomErrors err, Navigator nav)
                                                                                                                   throws Exception {
        Channel channel = new Channel();
        ChannelParameter parameter = new ChannelParameter();
        channelInfo.setProperties(channel);
        channelParameterInfo.setProperties(parameter);
        // 新建Channel默认关闭该状态
        channel.setStatus(ChannelStatus.STOP);
        channel.setParameters(parameter);
        try {
            channelService.create(channel);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidChannelName");
            return;
        }
        nav.redirectTo(WebConstant.CHANNEL_LIST_LINK);
    }

    /**
     * 修改Channel
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doEdit(@FormGroup("channelInfo") Group channelInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,
                       @FormGroup("channelParameterInfo") Group channelParameterInfo,
                       @FormField(name = "formChannelError", group = "channelInfo") CustomErrors err, Navigator nav)
                                                                                                                    throws Exception {
        Channel channel = new Channel();
        ChannelParameter parameter = new ChannelParameter();
        channelInfo.setProperties(channel);
        channelParameterInfo.setProperties(parameter);
        channel.setStatus(channelService.findById(channel.getId()).getStatus());
        parameter.setChannelId(channel.getId());
        channel.setParameters(parameter);
        try {
            channelService.modify(channel);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidChannelName");
            return;
        }

        nav.redirectToLocation("channelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    /**
     * 删除Channel
     * 
     * @param channelId
     * @throws WebxException
     */
    public void doDelete(@Param("channelId") Long channelId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        if (channelService.findById(channelId).getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }

        // 如果channel节点下面还有关联的pipeline时，不允许删除
        if (pipelineService.listByChannelIds(channelId).size() < 1) {
            channelService.remove(channelId);
        }

        nav.redirectToLocation("channelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    /**
     * 用于Channel运行状态的更新操作
     * 
     * @param channelId
     * @param status
     * @throws WebxException
     */
    public void doStatus(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                         @Param("channelId") Long channelId, @Param("status") String status, Navigator nav)
                                                                                                           throws WebxException {
        String errorType = null;
        if (status.equals("start")) {
            try {
                channelService.startChannel(channelId);
            } catch (ManagerException e) {
                if (e.getCause() instanceof InvalidConfigureException) {
                    errorType = ((InvalidConfigureException) e.getCause()).getType().name();
                }
            } catch (InvalidConfigureException rce) {
                errorType = rce.getType().name();
            }

        } else if (status.equals("stop")) {
            channelService.stopChannel(channelId);
        }

        if (errorType != null) {
            nav.redirectToLocation("channelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey)
                                   + "&errorType=" + errorType);
        } else {
            nav.redirectToLocation("channelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
        }
    }

    /**
     * 用于Channel的配置强制推送
     * 
     * @param channelId
     * @param status
     * @throws WebxException
     */
    public void doNotify(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                         @Param("channelId") Long channelId, @Param("status") String status, Navigator nav)
                                                                                                           throws WebxException {

        channelService.notifyChannel(channelId);
        nav.redirectToLocation("channelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

}
