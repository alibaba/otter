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

package com.alibaba.otter.node.deployer;

import java.util.concurrent.CountDownLatch;

import com.alibaba.otter.node.etl.OtterContextLocator;

/**
 * 集成测试
 * 
 * @author jianghang 2011-10-8 下午06:25:52
 * @version 4.0.0
 */
public class OtterLauncherIntegration {

    public static void main(String args[]) throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        Thread mainstem = new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(60 * 1000 * 1000);
                } catch (InterruptedException e) {
                }
                System.out.println("!!!!!!!!!!!!!!!!!!   start single");
                OtterContextLocator.getOtterController();
                // MainStemArbitrateEvent mainStemEvent =
                // OtterContextLocator.getArbitrateEventService().mainStemEvent();
                // // 启动
                // MainStemEventData eventData = new MainStemEventData();
                // eventData.setPipelineId(1L);
                // eventData.setStatus(MainStemEventData.Status.OVERTAKE);
                // mainStemEvent.single(eventData);
                // System.out.println("!!!!!!!!!!!!!!!!!!  end single");
                latch.countDown();
            }

        };
        mainstem.start();
        System.setProperty("nid", "2");
        OtterLauncher.main(null);
        latch.await();
    }
}
