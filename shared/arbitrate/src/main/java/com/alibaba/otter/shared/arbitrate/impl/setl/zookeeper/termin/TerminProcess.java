package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.termin;

import com.alibaba.otter.shared.arbitrate.model.TerminEventData;

/**
 * 终结信号处理的接口
 * 
 * @author jianghang 2011-9-26 下午01:37:04
 * @version 4.0.0
 */
public interface TerminProcess {

    public boolean process(TerminEventData data);
}
