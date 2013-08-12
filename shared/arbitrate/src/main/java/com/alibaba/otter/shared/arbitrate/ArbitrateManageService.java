package com.alibaba.otter.shared.arbitrate;

import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.SystemArbitrateEvent;

/**
 * 仲裁器管理服务，提供给console进行干预仲裁器的行为：比如开始/停止channel同步
 * 
 * @author jianghang 2011-8-9 下午04:40:36
 */
public interface ArbitrateManageService {

    public SystemArbitrateEvent systemEvent();

    public NodeArbitrateEvent nodeEvent();

    public PipelineArbitrateEvent pipelineEvent();

    public ChannelArbitrateEvent channelEvent();

}
