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

package com.alibaba.otter.shared.common.utils.zookeeper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * copy from zkclient中的{@linkplain ZkEventThread}，解决串行执行问题
 * 
 * @author jianghang 2012-9-27 下午05:48:41
 * @version 4.1.0
 */
public class ZkEventThread extends Thread {

    private static final Logger    LOG                  = LoggerFactory.getLogger(ZkEventThread.class);
    private static AtomicInteger   _eventId             = new AtomicInteger(0);
    private static final int       DEFAULT_POOL_SIZE    = 30;
    private static final int       DEFAULT_ACCEPT_COUNT = 60;

    private static ExecutorService executor             = new ThreadPoolExecutor(
                                                                                 DEFAULT_POOL_SIZE,
                                                                                 DEFAULT_POOL_SIZE,
                                                                                 0L,
                                                                                 TimeUnit.MILLISECONDS,
                                                                                 new ArrayBlockingQueue(
                                                                                                        DEFAULT_ACCEPT_COUNT),
                                                                                 new NamedThreadFactory(
                                                                                                        "Arbitrate-Async-Watcher"),
                                                                                 new ThreadPoolExecutor.CallerRunsPolicy());

    private BlockingQueue<ZkEvent> _events              = new LinkedBlockingQueue<ZkEvent>();

    public static abstract class ZkEvent {

        private String _description;

        public ZkEvent(String description){
            _description = description;
        }

        public abstract void run() throws Exception;

        @Override
        public String toString() {
            return "ZkEvent[" + _description + "]";
        }
    }

    ZkEventThread(String name){
        setDaemon(true);
        setName("ZkClient-EventThread-" + getId() + "-" + name);
    }

    public void run() {
        LOG.info("Starting ZkClient event thread.");
        try {
            while (!isInterrupted()) {
                final ZkEvent zkEvent = _events.take();
                int eventId = _eventId.incrementAndGet();
                LOG.debug("Delivering event #" + eventId + " " + zkEvent);
                executor.submit(new Runnable() {

                    public void run() {
                        try {
                            zkEvent.run();
                        } catch (InterruptedException e) {
                            interrupt();
                        } catch (ZkInterruptedException e) {
                            interrupt();
                        } catch (Throwable e) {
                            LOG.error("Error handling event " + zkEvent, e);
                        }
                    }
                });
                LOG.debug("Delivering event #" + eventId + " done");
            }
        } catch (InterruptedException e) {
            LOG.info("Terminate ZkClient event thread.");
        }
    }

    public void send(ZkEvent event) {
        if (!isInterrupted()) {
            LOG.debug("New event: " + event);
            _events.add(event);
        }
    }
}
