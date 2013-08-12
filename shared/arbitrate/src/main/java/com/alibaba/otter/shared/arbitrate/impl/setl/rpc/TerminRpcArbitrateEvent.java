package com.alibaba.otter.shared.arbitrate.impl.setl.rpc;

import com.alibaba.otter.shared.arbitrate.impl.setl.TerminArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TerminZooKeeperArbitrateEvent;

/**
 * 基于rpc的仲裁调度的termin信号处理，直接使用zookeeper的现有处理机制
 * 
 * @author jianghang 2012-9-29 上午11:06:11
 * @version 4.1.0
 */
public class TerminRpcArbitrateEvent extends TerminZooKeeperArbitrateEvent implements TerminArbitrateEvent {

}
