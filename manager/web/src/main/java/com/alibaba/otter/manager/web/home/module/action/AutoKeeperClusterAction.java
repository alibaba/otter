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

import java.util.Arrays;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.autokeeper.impl.AutoKeeperCollector;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;

public class AutoKeeperClusterAction extends AbstractAction {

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;
    @Resource(name = "autoKeeperCollector")
    private AutoKeeperCollector      autoKeeperCollector;

    public void doAdd(@FormGroup("autokeeperClusterInfo") Group autokeeperClusterInfo,
                      @FormField(name = "formAutokeeperClusterError", group = "autokeeperClusterInfo") CustomErrors err,
                      Navigator nav) throws Exception {
        AutoKeeperCluster autoKeeperCluster = new AutoKeeperCluster();
        autokeeperClusterInfo.setProperties(autoKeeperCluster);
        String zkClustersString = autokeeperClusterInfo.getField("zookeeperClusters").getStringValue();
        String[] zkClusters = StringUtils.split(zkClustersString, ";");

        autoKeeperCluster.setServerList(Arrays.asList(zkClusters));

        try {
            autoKeeperClusterService.createAutoKeeperCluster(autoKeeperCluster);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidNode");
            return;
        }
        nav.redirectTo(WebConstant.AUTO_KEEPER_CLUSTERS_LINK);
    }

    public void doEdit(@FormGroup("autokeeperClusterInfo") Group autokeeperClusterInfo,
                       @FormField(name = "formAutokeeperClusterError", group = "autokeeperClusterInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        AutoKeeperCluster autoKeeperCluster = new AutoKeeperCluster();
        autokeeperClusterInfo.setProperties(autoKeeperCluster);
        String zkClustersString = autokeeperClusterInfo.getField("zookeeperClusters").getStringValue();
        String[] zkClusters = StringUtils.split(zkClustersString, ";");

        autoKeeperCluster.setServerList(Arrays.asList(zkClusters));
        try {
            autoKeeperClusterService.modifyAutoKeeperCluster(autoKeeperCluster);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidChannelName");
            return;
        }
        nav.redirectTo(WebConstant.AUTO_KEEPER_CLUSTERS_LINK);
    }

    public void doRefresh(@Param("clusterId") Long clusterId, Navigator nav) throws Exception {
        AutoKeeperCluster autoKeeperCluster = autoKeeperClusterService.findAutoKeeperClusterById(clusterId);
        for (String address : autoKeeperCluster.getServerList()) {
            autoKeeperCollector.collectorServerStat(address);
            autoKeeperCollector.collectorConnectionStat(address);
            autoKeeperCollector.collectorWatchStat(address);
            autoKeeperCollector.collectorEphemeralStat(address);
        }
        nav.redirectToLocation("/auto_keeper_clusters_list.htm");
    }

    public void doDelete(@Param("clusterId") Long clusterId, Navigator nav) throws Exception {

        autoKeeperClusterService.removeAutoKeeperCluster(clusterId);
        nav.redirectToLocation("/auto_keeper_clusters_list.htm");
    }
}
