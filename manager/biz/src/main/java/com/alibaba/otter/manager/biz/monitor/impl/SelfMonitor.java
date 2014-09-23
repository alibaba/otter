package com.alibaba.otter.manager.biz.monitor.impl;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.manager.biz.monitor.Monitor;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * jvm内自动运行，不需要通过外部定时触发
 * 
 * @author jianghang 2013-9-6 上午10:22:28
 * @since 4.2.2
 */
public class SelfMonitor implements Monitor, InitializingBean, DisposableBean {

    protected static final Logger    log          = LoggerFactory.getLogger("monitorInfo");
    private static final int         DEFAULT_POOL = 1;
    private ScheduledExecutorService executor;
    private ScheduledFuture          future;
    private GlobalMonitor            monitor;
    private AtomicBoolean            enable       = new AtomicBoolean(true);
    private int                      interval     = 120;

    public void explore() {
        monitor.explore();
    }

    public void explore(Long... pipelineIds) {
        monitor.explore(pipelineIds);
    }

    public void explore(List<AlarmRule> rules) {
        monitor.explore(rules);
    }

    public void destroy() throws Exception {
        if (enable.get()) {
            stop();
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (enable.get()) {
            start();
        }
    }

    private synchronized void start() {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(DEFAULT_POOL, new NamedThreadFactory("Self-Monitor"),
                                                       new ThreadPoolExecutor.CallerRunsPolicy());
        }
        if (future == null) {
            future = executor.scheduleWithFixedDelay(new Runnable() {

                public void run() {
                    try {
                        monitor.explore();// 定时调用 
                    } catch (Exception e) {
                        log.error("self-monitor failed.", e);
                    }
                }
            }, interval, interval, TimeUnit.SECONDS);
        }
    }

    private synchronized void stop() {
        if (future != null) {
            future.cancel(true);
        }

        if (executor != null) {
            try {
                executor.awaitTermination(2 * 1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public void setMonitor(GlobalMonitor monitor) {
        this.monitor = monitor;
    }

    public void setEnable(boolean enable) {
        this.enable.set(enable);

        if (this.enable.get()) {
            start();
        } else {
            stop();
        }
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

}
