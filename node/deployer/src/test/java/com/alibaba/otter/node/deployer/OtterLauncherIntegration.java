package com.alibaba.otter.node.deployer;

import java.util.concurrent.CountDownLatch;

import com.alibaba.otter.node.deployer.OtterLauncher;
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
                    Thread.sleep(60 * 1000);
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
        System.setProperty("nid", "14");
        OtterLauncher.main(null);
        latch.await();
    }
}
