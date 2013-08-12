package com.alibaba.otter.node.etl.common.pipe.impl.rpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.etl.common.pipe.Pipe;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.core.model.EventType;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.google.common.collect.MapMaker;

/**
 * 基于rpc通讯的数据传递
 * 
 * <pre>
 * PUT：基于内存cache的临时存储
 * GET: 基于远程rpc请求的调用获取
 * </pre>
 * 
 * @author jianghang 2011-10-17 下午01:29:49
 * @version 4.0.0
 */
public abstract class AbstractRpcPipe<T, KEY extends RpcPipeKey> implements Pipe<T, KEY>, InitializingBean {

    protected Long                     timeout = 60 * 1000L; // 对应的超时时间,1分钟

    protected Map<RpcPipeKey, DbBatch> cache;

    public void afterPropertiesSet() throws Exception {
        cache = new MapMaker().expireAfterWrite(timeout, TimeUnit.MILLISECONDS).softValues().makeMap();
    }

    // rpc get操作事件
    public static class RpcEvent extends Event {

        private static final long serialVersionUID = 810191575813164952L;

        public RpcEvent(EventType eventType){
            super(eventType);
        }

        public RpcPipeKey key;

        public RpcPipeKey getKey() {
            return key;
        }

        public void setKey(RpcPipeKey key) {
            this.key = key;
        }

    }

    // ============== setter / getter ===============

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
