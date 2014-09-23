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

package com.alibaba.otter.manager.biz.config.autokeeper.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.autokeeper.dal.AutoKeeperClusterDAO;
import com.alibaba.otter.manager.biz.config.autokeeper.dal.dataobject.AutoKeeperClusterDO;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * 类AutoKeeperClusterServiceImpl.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-9-24 下午5:44:43
 * @version 4.1.0
 */
public class AutoKeeperClusterServiceImpl implements AutoKeeperClusterService {

    private AutoKeeperClusterDAO autoKeeperClusterDao;

    @Override
    public AutoKeeperCluster findAutoKeeperClusterById(Long id) {
        AutoKeeperClusterDO autoKeeperClusterDO = autoKeeperClusterDao.findAutoKeeperClusterById(id);
        return autoKeeperClusterDO == null ? null : doToModel(autoKeeperClusterDO);
    }

    @Override
    public List<AutoKeeperCluster> listAutoKeeperClusters() {
        return doToModel(autoKeeperClusterDao.listAutoKeeperClusters());
    }

    @Override
    public void modifyAutoKeeperCluster(AutoKeeperCluster autoKeeperCluster) {
        autoKeeperClusterDao.updateAutoKeeperCluster(modelToDo(autoKeeperCluster));
    }

    @Override
    public void createAutoKeeperCluster(AutoKeeperCluster autoKeeperCluster) {
        autoKeeperClusterDao.insertAutoKeeperClusterDO(modelToDo(autoKeeperCluster));
    }

    @Override
    public void removeAutoKeeperCluster(Long id) {
        autoKeeperClusterDao.delete(id);
    }

    private AutoKeeperCluster doToModel(AutoKeeperClusterDO autoKeeperClusterDo) {
        AutoKeeperCluster autoKeeperCluster = new AutoKeeperCluster();
        autoKeeperCluster.setId(autoKeeperClusterDo.getId());
        autoKeeperCluster.setClusterName(autoKeeperClusterDo.getClusterName());
        autoKeeperCluster.setDescription(autoKeeperClusterDo.getDescription());
        autoKeeperCluster.setServerList(JsonUtils.unmarshalFromString(autoKeeperClusterDo.getServerList(),
                                                                      new TypeReference<List<String>>() {
                                                                      }));
        autoKeeperCluster.setGmtCreate(autoKeeperClusterDo.getGmtCreate());
        autoKeeperCluster.setGmtModified(autoKeeperClusterDo.getGmtModified());
        return autoKeeperCluster;
    }

    public Integer getCount() {
        return autoKeeperClusterDao.getCount();
    }

    private List<AutoKeeperCluster> doToModel(List<AutoKeeperClusterDO> autoKeeperClusterDos) {
        List<AutoKeeperCluster> autoKeeperClusters = new ArrayList<AutoKeeperCluster>();
        for (AutoKeeperClusterDO autoKeeperClusterDo : autoKeeperClusterDos) {
            autoKeeperClusters.add(doToModel(autoKeeperClusterDo));
        }
        return autoKeeperClusters;
    }

    private AutoKeeperClusterDO modelToDo(AutoKeeperCluster autoKeeperCluster) {
        AutoKeeperClusterDO autokeeperClusterDo = new AutoKeeperClusterDO();
        autokeeperClusterDo.setId(autoKeeperCluster.getId());
        autokeeperClusterDo.setClusterName(autoKeeperCluster.getClusterName());
        autokeeperClusterDo.setDescription(autoKeeperCluster.getDescription());
        autokeeperClusterDo.setServerList(JsonUtils.marshalToString(autoKeeperCluster.getServerList()));
        autokeeperClusterDo.setGmtCreate(autoKeeperCluster.getGmtCreate());
        autokeeperClusterDo.setGmtModified(autoKeeperCluster.getGmtModified());
        return autokeeperClusterDo;
    }

    public void setAutoKeeperClusterDao(AutoKeeperClusterDAO autoKeeperClusterDao) {
        this.autoKeeperClusterDao = autoKeeperClusterDao;
    }

}
