package com.alibaba.otter.node.etl.common.pipe.impl.rpc;

import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.pipe.PipeDataType;
import com.alibaba.otter.node.etl.common.pipe.exception.PipeException;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.core.model.EventType;
import com.alibaba.otter.shared.etl.model.DbBatch;

/**
 * 基于rpc调用实现rowData的数据传递
 * 
 * @author jianghang 2011-10-18 下午02:56:47
 * @version 4.0.0
 */
public class RowDataRpcPipe extends AbstractRpcPipe<DbBatch, RpcPipeKey> {

    private ConfigClientService      configClientService;
    private NodeCommmunicationClient nodeCommmunicationClient;

    // 基于rowData rpc的eventType
    public static enum RowDataRpc implements EventType {
        get
    }

    public RowDataRpcPipe(){
        // 注册一下事件处理
        CommunicationRegistry.regist(RowDataRpc.get, this);
    }

    public RpcPipeKey put(DbBatch data) throws PipeException {
        RpcPipeKey key = new RpcPipeKey();
        key.setIdentity(data.getRowBatch().getIdentity());
        key.setNid(getNid());
        key.setDataType(PipeDataType.DB_BATCH);
        cache.put(key, data);
        return key;
    }

    public DbBatch get(RpcPipeKey key) throws PipeException {
        RpcEvent event = new RpcEvent(RowDataRpc.get);
        event.setKey(key);
        return (DbBatch) nodeCommmunicationClient.call(key.getNid(), event);
    }

    @SuppressWarnings("unused")
    // 处理rpc调用事件
    private DbBatch onGet(RpcEvent event) {
        return cache.remove(event.getKey()); // 不建议使用remove，rpc调用容易有retry请求，导致第二次拿到的数据为null
    }

    private Long getNid() {
        return configClientService.currentNode().getId();
    }

    // ==================== setter / getter =====================

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setNodeCommmunicationClient(NodeCommmunicationClient nodeCommmunicationClient) {
        this.nodeCommmunicationClient = nodeCommmunicationClient;
    }

}
