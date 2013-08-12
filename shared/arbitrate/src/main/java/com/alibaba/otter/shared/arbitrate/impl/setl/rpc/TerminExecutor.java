package com.alibaba.otter.shared.arbitrate.impl.setl.rpc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * 基于pipeline隔离的executor实现，每个pipeline一个线程
 * 
 * @author jianghang 2013-2-26 下午09:16:32
 * @version 4.1.7
 */
public class TerminExecutor extends ArbitrateLifeCycle {

    // 注意accept数量可以和SelectTask termin的大小一致
    // 队列必须为1，保证termin的创建是顺序的
    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(),
                                                              new NamedThreadFactory("Load-Rpc-Async"));

    public TerminExecutor(Long pipelineId){
        super(pipelineId);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public void destory() {
        super.destory();
        executor.shutdownNow();
    }

}
