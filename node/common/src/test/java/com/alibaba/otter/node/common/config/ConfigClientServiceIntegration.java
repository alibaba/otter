package com.alibaba.otter.node.common.config;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.BaseOtterTest;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;

public class ConfigClientServiceIntegration extends BaseOtterTest {

    @SpringBeanByName
    private ConfigClientService configClientService;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
    }

    @Test
    public void test_node() {
        Node cnode = configClientService.currentNode();
        System.out.println(cnode);
        want.bool(cnode.getId() == 1L).is(true);
        Node fnode = configClientService.findNode(2L);
        System.out.println(fnode);
        want.bool(fnode.getId() == 2L);

        fnode = configClientService.findNode(2L);
        System.out.println(fnode);
        want.bool(fnode.getId() == 2L);
    }

    @Test
    public void test_pipeline() {
        Long channelId = 11L;
        Channel channel = configClientService.findChannel(channelId);
        System.out.println(channel);
        want.number(channel.getId()).isEqualTo(channelId);
    }

}
