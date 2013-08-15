package com.alibaba.otter.manager.biz.config.autokeeper;

import java.util.List;

import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;

/**
 * @author simon 2012-9-24 下午5:35:01
 * @version 4.1.0
 */
public interface AutoKeeperClusterService {

    public AutoKeeperCluster findAutoKeeperClusterById(Long id);

    public List<AutoKeeperCluster> listAutoKeeperClusters();

    public void modifyAutoKeeperCluster(AutoKeeperCluster autoKeeperCluster);

    public void createAutoKeeperCluster(AutoKeeperCluster autoKeeperCluster);

    public void removeAutoKeeperCluster(Long id);

    public Integer getCount();
}
