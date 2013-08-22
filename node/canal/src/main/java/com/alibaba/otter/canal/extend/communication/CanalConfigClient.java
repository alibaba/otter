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

package com.alibaba.otter.canal.extend.communication;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.shared.communication.model.canal.FindCanalEvent;
import com.alibaba.otter.shared.communication.model.canal.FindFilterEvent;

/**
 * 对应canal的配置
 * 
 * @author jianghang 2012-7-4 下午03:09:17
 * @version 4.1.0
 */
public class CanalConfigClient {

    private CanalCommmunicationClient delegate;

    /**
     * 根据对应的destinantion查询Canal信息
     */
    public Canal findCanal(String destination) {
        FindCanalEvent event = new FindCanalEvent();
        event.setDestination(destination);
        try {
            Object obj = delegate.callManager(event);
            if (obj != null && obj instanceof Canal) {
                return (Canal) obj;
            } else {
                throw new CanalException("No Such Canal by [" + destination + "]");
            }
        } catch (Exception e) {
            throw new CanalException("call_manager_error", e);
        }
    }

    /**
     * 根据对应的destinantion查询filter信息
     */
    public String findFilter(String destination) {
        FindFilterEvent event = new FindFilterEvent();
        event.setDestination(destination);
        try {
            Object obj = delegate.callManager(event);
            if (obj != null && obj instanceof String) {
                return (String) obj;
            } else {
                throw new CanalException("No Such Canal by [" + destination + "]");
            }
        } catch (Exception e) {
            throw new CanalException("call_manager_error", e);
        }
    }

    // ================== setter / getter ===============

    public void setDelegate(CanalCommmunicationClient delegate) {
        this.delegate = delegate;
    }

}
