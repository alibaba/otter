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

package com.alibaba.otter.node.etl.launcher;

public class ArbitrateInitIntegration {

    public static void main(String args[]) {
        // System.setProperty("nid", "1");
        // OtterContextLocator.getOtterController();
        //
        // cleaner("/otter/channel/1/1/termin", "/otter/channel/1/1/termin");
        // cleaner("/otter/channel/1/1/process", "/otter/channel/1/1/process");
        // cleaner("/otter/channel/1/1/lock/load", "/otter/channel/1/1/lock/load");
        // cleaner("/otter/channel/2/2/termin", "/otter/channel/2/2/termin");
        // cleaner("/otter/channel/2/2/process", "/otter/channel/2/2/process");
        // cleaner("/otter/channel/2/2/lock/load", "/otter/channel/2/2/lock/load");
        // cleaner("/e3/destinations/mysql", "/e3/destinations/mysql");
        // cleaner("/e3/destinations/oracle", "/e3/destinations/oracle");
        //
        // // ChannelArbitrateEvent channelEvent =
        // OtterArbitrateServiceLocator.getArbitrateManageService().channelEvent();
        // // channelEvent.init(1L);
        // // PipelineArbitrateEvent pipelineEvent =
        // // OtterArbitrateServiceLocator.getArbitrateManageService().pipelineEvent();
        // // pipelineEvent.init(1L, 1L);
        //
        // ZkClientx zookeeper = ZooKeeperClient.getInstance();
        // try {
        // zookeeper.setData("/otter/channel/1/1", new byte[0], -1); // 更新记录
        // zookeeper.setData("/otter/channel/2/2", new byte[0], -1); // 更新记录
        // } catch (KeeperException e) {
        // e.printStackTrace();
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        System.exit(0);
        // long[] ids = new long[] { 1L, 2L, 3L, 4L, 5L, 11L, 13L, 14L, 15L, 16L, 17L, 18L, 19L };
        // for (long id : ids) {
        // channelEvent.init(id);
        // }
        // PipelineArbitrateEvent pipelineEvent =
        // OtterArbitrateServiceLocator.getArbitrateManageService().pipelineEvent();
        // System.out.println(pipelineEvent);
        // pipelineEvent.init(1L, 1L);
    }

    // private static void cleaner(String root, String path) {
    // ZkClientx zookeeper = ZooKeeperClient.getInstance();
    // try {
    // List<String> nodes = zookeeper.getChildren(path, false);
    // for (String node : nodes) {
    // cleaner(root, path + "/" + node);
    // }
    // if (path.equals(root)) {
    // return;
    // } else {
    // System.out.println("clean :" + path);
    // zookeeper.delete(path, -1);
    // return;
    // }
    // } catch (NoNodeException e) {
    // // ignore
    // } catch (KeeperException e) {
    // e.printStackTrace();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
}
