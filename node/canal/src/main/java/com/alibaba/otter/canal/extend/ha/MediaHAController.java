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

package com.alibaba.otter.canal.extend.ha;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.common.AbstractCanalLifeCycle;
import com.alibaba.otter.canal.parse.CanalHASwitchable;
import com.alibaba.otter.canal.parse.exception.CanalHAException;
import com.alibaba.otter.canal.parse.ha.CanalHAController;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.common.push.supplier.DatasourceChangeCallback;
import com.alibaba.otter.common.push.supplier.DatasourceInfo;
import com.alibaba.otter.common.push.supplier.DatasourceSupplier;
import com.alibaba.otter.common.push.supplier.media.MediaDatasourceSupplier;

/**
 * 基于media的HA控制机制
 * 
 * @author jianghang 2012-7-6 下午02:48:21
 * @version 4.1.0
 */
public class MediaHAController extends AbstractCanalLifeCycle implements CanalHAController {

    private static Logger               log = LoggerFactory.getLogger(MediaHAController.class);

    private String                      group;
    private String                      customUser;
    private String                      customPasswd;
    private String                      customSchema;

    private DatasourceSupplier          supplier;
    private CanalHASwitchable           canalHASwitchable;
    private volatile AuthenticationInfo availableAuthenticationInfo;

    public MediaHAController(String group){
        this.group = group;
    }

    public MediaHAController(String group, String customUser, String customPasswd, String customSchema){
        this.group = group;
        this.customUser = customUser;
        this.customPasswd = customPasswd;
        this.customSchema = customSchema;
    }

    public void start() throws CanalHAException {
        super.start();

        if (this.supplier == null) {
            validate();
            this.supplier = MediaDatasourceSupplier.newInstance(group);
        }
        if (!this.supplier.isStart()) {
            this.supplier.start();
        }

        DatasourceInfo fetched = this.supplier.fetchMaster();
        AuthenticationInfo masterFetched = AuthenticationInfoUtils.createFrom(fetched);

        log.info(String.format("medialHAController started for  goup:[%s], and first auth info is : [%s]", this.group,
                               masterFetched));

        this.availableAuthenticationInfo = customInfoIfNecessay(masterFetched);

        log.info(String.format("medialHAController customed for goup:[%s], and first auth info is : [%s]", this.group,
                               this.availableAuthenticationInfo));

        this.supplier.addSwtichCallback(new DatasourceChangeCallback() {

            @Override
            public void masterChanged(DatasourceInfo newMaster) {
                AuthenticationInfo newAuthenticationInfo = AuthenticationInfoUtils.createFrom(newMaster);
                switchEventSource(newAuthenticationInfo);
            }
        });
    }

    private void validate() {
        if (StringUtils.isEmpty(this.group)) {
            throw new IllegalStateException(String.format("app or group is empty, app is [%s] , group is [%s]",
                                                          this.group));
        }
    }

    public void stop() throws CanalHAException {
        super.stop();
        this.supplier.stop();
    }

    private void switchEventSource(AuthenticationInfo newMaster) {
        log.warn(String.format("MediaHAController received a datasource swith from [%s] to [%s]",
                               availableAuthenticationInfo, newMaster));

        customInfoIfNecessay(newMaster);

        log.warn(String.format("MediaHAController customed a datasource swith from [%s] to [%s]",
                               availableAuthenticationInfo, newMaster));

        availableAuthenticationInfo = newMaster;
        this.canalHASwitchable.doSwitch(newMaster);
    }

    public void setCanalHASwitchable(CanalHASwitchable canalHASwitchable) {
        this.canalHASwitchable = canalHASwitchable;
    }

    public CanalHASwitchable getCanalHASwitchable() {
        return canalHASwitchable;
    }

    public String getGroup() {
        return group;
    }

    public DatasourceSupplier getSupplier() {
        return supplier;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setSupplier(DatasourceSupplier supplier) {
        this.supplier = supplier;
    }

    public AuthenticationInfo getAvailableAuthenticationInfo() {
        return availableAuthenticationInfo;
    }

    public void setAvailableAuthenticationInfo(AuthenticationInfo availableAuthenticationInfo) {
        this.availableAuthenticationInfo = availableAuthenticationInfo;
    }

    /**
     * override custom field
     * 
     * @param authenticationInfo
     */
    protected AuthenticationInfo customInfoIfNecessay(AuthenticationInfo authenticationInfo) {
        if (StringUtils.isNotBlank(customUser)) {
            authenticationInfo.setUsername(customUser);
        }
        if (StringUtils.isNotBlank(customPasswd)) {
            authenticationInfo.setPassword(customPasswd);
        }
        if (StringUtils.isNotBlank(customSchema)) {
            authenticationInfo.setDefaultDatabaseName(customSchema);
        }

        return authenticationInfo;
    }

}
