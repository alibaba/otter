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

package com.alibaba.otter.manager.biz.config.autokeeper.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.config.autokeeper.dal.dataobject.AutoKeeperClusterDO;

public interface AutoKeeperClusterDAO {

    public AutoKeeperClusterDO findAutoKeeperClusterById(Long id);

    public List<AutoKeeperClusterDO> listAutoKeeperClusters();

    public void updateAutoKeeperCluster(AutoKeeperClusterDO autoKeeperClusterDo);

    public void insertAutoKeeperClusterDO(AutoKeeperClusterDO autoKeeperClusterDo);

    public void delete(Long clusterId);

    public Integer getCount();

}
