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

package com.alibaba.otter.manager.biz.common.alarm;


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
     * <pre>
     * Map内容；
     * 1. message : 报警内容
     * 2. receiveKey : 报警接收者信息
     * </pre>
     * 
     * @param data
     */
    public void sendAlarm(AlarmMessage data);

}
