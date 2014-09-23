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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.node.etl.OtterContextLocator;
import com.alibaba.otter.node.etl.OtterController;

/**
 * load otter task to sync data with some pipeline.
 * 
 * @author xiaoqing.zhouxq 2011-8-29 上午10:02:04
 */
public class OtterLauncher {

    private static final Logger logger = LoggerFactory.getLogger(OtterLauncher.class);

    public static void main(String[] args) throws Throwable {
        // 启动dragoon client
        // startDragoon();
        // logger.info("INFO ## the dragoon is start now ......");
        final OtterController controller = OtterContextLocator.getOtterController();
        controller.start();
        try {
            logger.info("INFO ## the otter server is running now ......");
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        logger.info("INFO ## stop the otter server");
                        controller.stop();
                    } catch (Throwable e) {
                        logger.warn("WARN ##something goes wrong when stopping Otter Server:\n{}",
                            ExceptionUtils.getFullStackTrace(e));
                    } finally {
                        logger.info("INFO ## otter server is down.");
                    }
                }

            });
        } catch (Throwable e) {
            logger.error("ERROR ## Something goes wrong when starting up the Otter Server:\n{}",
                ExceptionUtils.getFullStackTrace(e));
            System.exit(0);
        }
    }

    // 启动dragoon client
    // private static void startDragoon() {
    // do nothing
    // }
}
