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

package com.alibaba.otter.common.push.supplier.media;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.common.push.PushException;
import com.alibaba.otter.common.push.SubscribeCallback;
import com.alibaba.otter.common.push.SubscribeManager;
import com.alibaba.otter.common.push.SubscribeManagerFactory;
import com.alibaba.otter.common.push.SubscribeType;
import com.alibaba.otter.common.push.supplier.AbstractDatasourceSupplier;
import com.alibaba.otter.common.push.supplier.DatasourceChangeCallback;
import com.alibaba.otter.common.push.supplier.DatasourceInfo;
import com.alibaba.otter.common.push.supplier.HaDatasourceInfo;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * 基于data media机制提供的数据源信息
 * 
 * @author jianghang 2013-4-18 下午03:03:26
 * @version 4.1.8
 */
public class MediaDatasourceSupplier extends AbstractDatasourceSupplier {

    private static final Logger            log          = LoggerFactory.getLogger(MediaDatasourceSupplier.class);
    private static AtomicInteger           CHANGED_TIME = new AtomicInteger(0);

    private SubscribeManager               mediaSubscribeManager;

    private String                         groupKey;
    private HaDatasourceInfo               haInfo;

    private List<DatasourceChangeCallback> callbacks    = new LinkedList<DatasourceChangeCallback>();

    private Object                         lock         = new Object();
    private SubscribeCallback              subscribeCallback;

    private MediaDatasourceSupplier(){
    }

    private MediaDatasourceSupplier(String groupKey){
        this.groupKey = groupKey;
    }

    public static MediaDatasourceSupplier newInstance(String groupKey) {
        MediaDatasourceSupplier supplier = new MediaDatasourceSupplier(groupKey);
        return supplier;
    }

    public void doStart() {
        subscribeCallback = new SubscribeCallback() {

            @Override
            public void callback(String matrixInfo) {

                log.warn("has received changed ds [{}] for [{}] times", matrixInfo, CHANGED_TIME.addAndGet(1));
                synchronized (MediaDatasourceSupplier.this.lock) {
                    MediaDatasourceSupplier.this.haInfo = parse(matrixInfo);
                    MediaDatasourceSupplier.this.callback();
                }
            }
        };
        this.init();
    }

    @Override
    public void doStop() {
        this.mediaSubscribeManager.unRegisterCallback(this.groupKey, subscribeCallback);
        this.callbacks.clear();
    }

    public synchronized void init() {
        this.mediaSubscribeManager = SubscribeManagerFactory.getSubscribeManager(SubscribeType.MEDIA);
        if (this.mediaSubscribeManager == null) {
            throw new PushException("MediaDatasourceSupplier : mediaSubscribeManager is null, check the spring config");
        }

        String matrixStr = mediaSubscribeManager.fetchConfig(groupKey);
        this.haInfo = parse(matrixStr);
        this.mediaSubscribeManager.registerCallback(this.groupKey, subscribeCallback);
    }

    public DatasourceInfo fetchMaster() {
        synchronized (lock) {
            if (this.haInfo == null) {
                throw new PushException("haInfo is null, check the init phase");
            }
            return this.haInfo.getMaster();
        }
    }

    public void addSwtichCallback(DatasourceChangeCallback callback) {
        callbacks.add(callback);
    }

    private void callback() {
        if (callbacks == null || callbacks.size() == 0) {
            return;
        }

        for (DatasourceChangeCallback callback : callbacks) {
            callback.masterChanged(MediaDatasourceSupplier.this.fetchMaster());
        }

    }

    private HaDatasourceInfo parse(String matrixStr) {
        HaDatasourceInfo haInfo = new HaDatasourceInfo();

        Map jsonMap = JsonUtils.unmarshalFromString(matrixStr, HashMap.class);
        String masterAddress = (String) jsonMap.get("master");
        if (masterAddress != null) {
            DatasourceInfo master = new DatasourceInfo();
            master.setAddress(parseAddress(masterAddress));
            haInfo.setMaster(master);
        }

        String slaveAddress = (String) jsonMap.get("master");
        if (slaveAddress != null) {
            DatasourceInfo slave = new DatasourceInfo();
            slave.setAddress(parseAddress(slaveAddress));
            haInfo.getSlavers().add(slave);
        }

        return haInfo;
    }

    private InetSocketAddress parseAddress(String address) {
        String[] strs = StringUtils.split(address, ":");
        if (strs.length != 2) {
            throw new IllegalArgumentException("illegal address format:" + address);
        }

        return new InetSocketAddress(strs[0], Integer.valueOf(strs[1]));
    }

}
