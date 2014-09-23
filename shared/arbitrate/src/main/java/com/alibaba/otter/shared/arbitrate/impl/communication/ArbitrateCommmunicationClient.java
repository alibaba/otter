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

package com.alibaba.otter.shared.arbitrate.impl.communication;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
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
public class ArbitrateCommmunicationClient {

    private static final Logger logger = LoggerFactory.getLogger(ArbitrateCommmunicationClient.class);
    private CommunicationClient delegate;
    private List<String>        managerAddress;
    private volatile int        index  = 0;

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
        for (int i = index; i < index + managerAddress.size(); i++) {
            String address = managerAddress.get(i % managerAddress.size());
            try {
                Object result = delegate.call(address, event);
                index = i;
                return result;
            } catch (CommunicationException e) {
                ex = e;
                logger.warn("call manager [{}] event [{}] failed, maybe can try another manager.", address, event);
            }
        }

        throw ex;
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
            DefaultCommunicationClientImpl defaultClient = (DefaultCommunicationClientImpl) delegate;
            defaultClient.submit(new Runnable() {

                @Override
                public void run() {
                    Object result = callManager(event);
                    callback.call(result);
                }
            });
        }
    }

    private String convertToAddress(Long nid) {
        Node node = ArbitrateConfigUtils.findNode(nid);
        if (node.getParameters().getUseExternalIp()) {
            return node.getParameters().getExternalIp() + ":" + node.getPort();
        } else {
            return node.getIp() + ":" + node.getPort();
        }
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

}
