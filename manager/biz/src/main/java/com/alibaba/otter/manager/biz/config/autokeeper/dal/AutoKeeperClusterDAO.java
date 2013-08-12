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
