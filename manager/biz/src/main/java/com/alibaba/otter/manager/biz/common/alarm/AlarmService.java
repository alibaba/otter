package com.alibaba.otter.manager.biz.common.alarm;

import java.util.Map;

/**
 * 报警服务service定义,暂时先简单实现：利用dragoon的报警推送机制进行短信，邮件，旺旺信息等报警
 * 
 * @author jianghang 2011-9-26 下午10:27:44
 * @version 4.0.0
 */
public interface AlarmService {

    /**
     * 发送基于kv的报警信息
     * 
     * @param data
     */
    public void sendAlarm(Map<String, Object> data);

}
