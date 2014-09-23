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

package com.alibaba.otter.node.common.communication;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.DisposableBean;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.communication.core.CommunicationClient;
import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.DefaultCommunicationClientImpl;
import com.alibaba.otter.shared.communication.core.model.Callback;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 封装了基于communication通讯的工具
 * 
 * @author jianghang 2011-10-18 下午02:18:04
 * @version 4.0.0
 */
public class NodeCommmunicationClient implements DisposableBean {

    private CommunicationClient delegate;
    private ConfigClientService configClientService;
    private List<String>        managerAddress;
    private volatile int        index = 0;

    /**
     * 指定对应的Node节点，进行event调用
     */
    public Object call(Long nid, final Event event) {
        return delegate.call(convertToAddress(nid), event);
    }

    /**
     * 指定对应的Node节点，进行event调用
     * 
     * <pre>
     * 注意：该方法为异步调用
     * </pre>
     */
    public void call(Long nid, Event event, final Callback callback) {
        delegate.call(convertToAddress(nid), event, callback);
    }

    /**
     * 指定manager，进行event调用
     */
    public Object callManager(final Event event) {
        CommunicationException ex = null;
        Object object = null;
        for (int i = index; i < index + managerAddress.size(); i++) { // 循环一次manager的所有地址
            String address = managerAddress.get(i % managerAddress.size());
            try {
                object = delegate.call(address, event);
                index = i; // 更新一下上一次成功的地址
                return object;
            } catch (CommunicationException e) {
                // retry next address;
                ex = e;
            }
        }

        throw ex; // 走到这一步，说明肯定有出错了
    }

    /**
     * 指定manager，进行event调用
     * 
     * <pre>
     * 注意：该方法为异步调用
     * </pre>
     */
    public void callManager(final Event event, final Callback callback) {
        if (delegate instanceof DefaultCommunicationClientImpl) {
            ((DefaultCommunicationClientImpl) delegate).submit(new Runnable() {

                public void run() {
                    Object obj = callManager(event);
                    callback.call(obj);
                }
            });
        }
    }

    private String convertToAddress(Long nid) {
        Node node = configClientService.findNode(nid);
        if (node.getParameters().getUseExternalIp()) {
            return node.getParameters().getExternalIp() + ":" + node.getPort();
        } else {
            return node.getIp() + ":" + node.getPort();
        }
    }

    public void destroy() throws Exception {
    }

    // ================== setter / getter =====================

    public void setDelegate(CommunicationClient delegate) {
        this.delegate = delegate;
    }

    public void setManagerAddress(String managerAddress) {
        String server = StringUtils.replace(managerAddress, ";", ",");
        String[] servers = StringUtils.split(server, ',');
        this.managerAddress = Arrays.asList(servers);
        this.index = RandomUtils.nextInt(this.managerAddress.size()); // 随机选择一台机器
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public String getManagerAddress() {
        return StringUtils.join(managerAddress, ",");
    }

}
