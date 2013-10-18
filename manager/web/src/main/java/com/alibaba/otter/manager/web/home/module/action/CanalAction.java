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

package com.alibaba.otter.manager.web.home.module.action;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.DataSourcing;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.SourcingType;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.WebConstant;

public class CanalAction extends AbstractAction {

    @Resource(name = "canalService")
    private CanalService    canalService;

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;

    /**
     * 添加canal
     */
    public void doAdd(@FormGroup("canalInfo") Group canalInfo,
                      @FormGroup("canalParameterInfo") Group canalParameterInfo,
                      @FormField(name = "formCanalError", group = "canalInfo") CustomErrors err,
                      @FormField(name = "formHeartBeatError", group = "canalParameterInfo") CustomErrors heartBeatErr,
                      Navigator nav) throws Exception {
        Canal canal = new Canal();
        CanalParameter parameter = new CanalParameter();
        canalInfo.setProperties(canal);
        canalParameterInfo.setProperties(parameter);

        String zkClustersString = canalParameterInfo.getField("zkClusters").getStringValue();
        String[] zkClusters = StringUtils.split(zkClustersString, ";");
        parameter.setZkClusters(Arrays.asList(zkClusters));

        Long zkClusterId = canalParameterInfo.getField("autoKeeperClusterId").getLongValue();
        parameter.setZkClusterId(zkClusterId);
        canal.setCanalParameter(parameter);

        String dbAddressesString = canalParameterInfo.getField("groupDbAddresses").getStringValue();
        // 解析格式：
        // 127.0.0.1:3306:MYSQL,127.0.0.1:3306:ORACLE;127.0.0.1:3306,127.0.0.1:3306;
        // 第一层的分号代表主备概念，,第二层逗号代表分组概念
        if (StringUtils.isNotEmpty(dbAddressesString)) {
            List<List<DataSourcing>> dbSocketAddress = new ArrayList<List<DataSourcing>>();
            String[] dbAddresses = StringUtils.split(dbAddressesString, ";");
            for (String dbAddressString : dbAddresses) {
                List<DataSourcing> groupDbSocketAddress = new ArrayList<DataSourcing>();
                String[] groupDbAddresses = StringUtils.split(dbAddressString, ",");
                for (String groupDbAddress : groupDbAddresses) {
                    String strs[] = StringUtils.split(groupDbAddress, ":");
                    InetSocketAddress address = new InetSocketAddress(strs[0].trim(), Integer.valueOf(strs[1]));
                    SourcingType type = parameter.getSourcingType();
                    if (strs.length > 2) {
                        type = SourcingType.valueOf(strs[2]);
                    }
                    groupDbSocketAddress.add(new DataSourcing(type, address));
                }
                dbSocketAddress.add(groupDbSocketAddress);
            }

            parameter.setGroupDbAddresses(dbSocketAddress);
        }

        String positionsString = canalParameterInfo.getField("positions").getStringValue();
        if (StringUtils.isNotEmpty(positionsString)) {
            String positions[] = StringUtils.split(positionsString, ";");
            parameter.setPositions(Arrays.asList(positions));
        }

        if (parameter.getDetectingEnable() && StringUtils.startsWithIgnoreCase(parameter.getDetectingSQL(), "select")) {
            heartBeatErr.setMessage("invaliedHeartBeat");
            return;
        }

        try {
            canalService.create(canal);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidCanal");
            return;
        }

        if (parameter.getSourcingType().isMysql() && parameter.getSlaveId() == null) {
            parameter.setSlaveId(10000 + canal.getId());
            // 再次更新一下slaveId
            try {
                canalService.modify(canal);
            } catch (RepeatConfigureException rce) {
                err.setMessage("invalidCanal");
                return;
            }
        }

        nav.redirectTo(WebConstant.CANAL_LIST_LINK);
    }

    /**
     * 修改canal
     */
    public void doEdit(@FormGroup("canalInfo") Group canalInfo,
                       @FormGroup("canalParameterInfo") Group canalParameterInfo,
                       @FormField(name = "formCanalError", group = "canalInfo") CustomErrors err,
                       @FormField(name = "formHeartBeatError", group = "canalParameterInfo") CustomErrors heartBeatErr,
                       Navigator nav) throws Exception {
        Canal canal = new Canal();
        CanalParameter parameter = new CanalParameter();
        canalInfo.setProperties(canal);
        canalParameterInfo.setProperties(parameter);

        String zkClustersString = canalParameterInfo.getField("zkClusters").getStringValue();
        String[] zkClusters = StringUtils.split(zkClustersString, ";");
        parameter.setZkClusters(Arrays.asList(zkClusters));

        Long zkClusterId = canalParameterInfo.getField("autoKeeperClusterId").getLongValue();
        parameter.setZkClusterId(zkClusterId);

        String dbAddressesString = canalParameterInfo.getField("groupDbAddresses").getStringValue();
        if (StringUtils.isNotEmpty(dbAddressesString)) {
            List<List<DataSourcing>> dbSocketAddress = new ArrayList<List<DataSourcing>>();
            String[] dbAddresses = StringUtils.split(dbAddressesString, ";");
            for (String dbAddressString : dbAddresses) {
                List<DataSourcing> groupDbSocketAddress = new ArrayList<DataSourcing>();
                String[] groupDbAddresses = StringUtils.split(dbAddressString, ",");
                for (String groupDbAddress : groupDbAddresses) {
                    String strs[] = StringUtils.split(groupDbAddress, ":");
                    InetSocketAddress address = new InetSocketAddress(strs[0].trim(), Integer.valueOf(strs[1]));
                    SourcingType type = parameter.getSourcingType();
                    if (strs.length > 2) {
                        type = SourcingType.valueOf(strs[2]);
                    }
                    groupDbSocketAddress.add(new DataSourcing(type, address));
                }
                dbSocketAddress.add(groupDbSocketAddress);
            }

            parameter.setGroupDbAddresses(dbSocketAddress);
        }

        String positionsString = canalParameterInfo.getField("positions").getStringValue();
        if (StringUtils.isNotEmpty(positionsString)) {
            String positions[] = StringUtils.split(positionsString, ";");
            parameter.setPositions(Arrays.asList(positions));
        }

        if (parameter.getDetectingEnable() && StringUtils.startsWithIgnoreCase(parameter.getDetectingSQL(), "select")) {
            heartBeatErr.setMessage("invaliedHeartBeat");
            return;
        }

        canal.setCanalParameter(parameter);

        try {
            canalService.modify(canal);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidCanal");
            return;
        }

        nav.redirectToLocation("canalList.htm");
    }

    /**
     * 删除canal
     * 
     * @param canalId
     * @throws WebxException
     */
    public void doDelete(@Param("canalId") Long canalId, Navigator nav) throws WebxException {

        canalService.remove(canalId);
        nav.redirectToLocation("canalList.htm");
    }
}
