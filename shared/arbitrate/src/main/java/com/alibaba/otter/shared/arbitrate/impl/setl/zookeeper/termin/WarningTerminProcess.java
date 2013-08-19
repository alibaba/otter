package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.termin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.impl.alarm.AlarmClientService;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;

/**
 * 回滚的终结信号处理
 * 
 * @author jianghang 2011-9-26 下午02:03:02
 * @version 4.0.0
 */
public class WarningTerminProcess implements TerminProcess {

    private static final Logger logger = LoggerFactory.getLogger(WarningTerminProcess.class);
    private AlarmClientService  alarmClientService;

    public boolean process(TerminEventData data) {
        logger.warn("nid:{}[{}:{}]",
                    new Object[] { ArbitrateConfigUtils.getCurrentNid(), data.getPipelineId(),
                            data.getCode() + ":" + data.getDesc() });
        alarmClientService.sendAlarm(ArbitrateConfigUtils.getCurrentNid(), data.getPipelineId(), data.getCode(),
                                     data.getDesc());
        return true;
    }

    // ============= setter / getter =============

    public void setAlarmClientService(AlarmClientService alarmClientService) {
        this.alarmClientService = alarmClientService;
    }
}
