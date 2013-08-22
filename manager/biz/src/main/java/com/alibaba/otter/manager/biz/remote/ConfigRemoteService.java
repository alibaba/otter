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

package com.alibaba.otter.manager.biz.remote;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.communication.model.config.FindChannelEvent;
import com.alibaba.otter.shared.communication.model.config.FindMediaEvent;
import com.alibaba.otter.shared.communication.model.config.FindNodeEvent;
import com.alibaba.otter.shared.communication.model.config.FindTaskEvent;

/**
 * 针对manager config对象的远程服务接口定义
 * 
 * @author jianghang
 */
public interface ConfigRemoteService {

    /**
     * 将channel对象重新通知下对应的工作节点
     */
    public boolean notifyChannel(Channel channel);

    /**
     * 接收客户端的查询channel请求
     */
    public Channel onFindChannel(FindChannelEvent event);

    /**
     * 接收客户端的查询Node请求
     */
    public Node onFindNode(FindNodeEvent event);

    /**
     * 接收客户端根据nid查询需要处理的Channel请求
     */
    public List<Channel> onFindTask(FindTaskEvent event);

    /**
     * 返回media信息
     */
    public String onFindMedia(FindMediaEvent event);
}
