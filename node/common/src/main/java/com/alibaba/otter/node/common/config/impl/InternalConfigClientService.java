package com.alibaba.otter.node.common.config.impl;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

/**
 * 内部config service
 * 
 * @author jianghang
 */
public interface InternalConfigClientService extends ConfigClientService {

    /**
     * 创建或者更新本地service的数据
     */
    public void createOrUpdateChannel(Channel channel);
}
