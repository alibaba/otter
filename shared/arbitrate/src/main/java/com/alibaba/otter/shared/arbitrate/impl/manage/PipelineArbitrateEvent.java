package com.alibaba.otter.shared.arbitrate.impl.manage;

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.helper.ManagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 针对pipeline管理的相关信号操作
 * 
 * @author jianghang 2011-8-31 下午07:34:11
 */
public class PipelineArbitrateEvent implements ArbitrateEvent {

    private ZkClientx zookeeper = ZooKeeperClient.getInstance();

    /**
     * 初始化对应的pipeline节点,同步调用
     */
    public void init(Long channelId, Long pipelineId) {
        String path = ManagePathUtils.getPipeline(channelId, pipelineId);
        String processRootPath = ManagePathUtils.getProcessRoot(channelId, pipelineId);
        String terminRootPath = ManagePathUtils.getTerminRoot(channelId, pipelineId);
        String remedyRootPath = ManagePathUtils.getRemedyRoot(channelId, pipelineId);
        String lockRootPath = ManagePathUtils.getLockRoot(channelId, pipelineId);
        String loadLockPath = lockRootPath + "/" + ArbitrateConstants.NODE_LOCK_LOAD;
        try {
            zookeeper.create(path, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(processRootPath, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(terminRootPath, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(remedyRootPath, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(lockRootPath, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(loadLockPath, new byte[0], CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            // 如果节点已经存在，则不抛异常
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("Pipeline_init", pipelineId.toString(), e);
        }
    }

    /**
     * 销毁对应的pipeline节点,同步调用
     */
    public void destory(Long channelId, Long pipelineId) {
        String path = ManagePathUtils.getPipeline(channelId, pipelineId);
        String processRootPath = ManagePathUtils.getProcessRoot(channelId, pipelineId);
        String terminRootPath = ManagePathUtils.getTerminRoot(channelId, pipelineId);
        String remedyRootPath = ManagePathUtils.getRemedyRoot(channelId, pipelineId);
        String lockRootPath = ManagePathUtils.getLockRoot(channelId, pipelineId);
        String loadLockPath = lockRootPath + "/" + ArbitrateConstants.NODE_LOCK_LOAD;
        try {
            zookeeper.deleteRecursive(loadLockPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(lockRootPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(terminRootPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(remedyRootPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(processRootPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(path); // 删除节点，不关心版本
        } catch (ZkNoNodeException e) {
            // 如果节点已经不存在，则不抛异常
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("Pipeline_destory", pipelineId.toString(), e);
        }
    }

}
