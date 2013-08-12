package com.alibaba.otter.node.common.config;

import java.util.List;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.BaseOtterTest;
import com.alibaba.otter.node.common.config.model.NodeTask;

public class NodeTaskServiceIntegration extends BaseOtterTest {

    @SpringBeanByName
    private NodeTaskService nodeTaskService;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
    }

    @Test
    public void test_task() {
        List<NodeTask> tasks = nodeTaskService.listAllNodeTasks();
        System.out.println(tasks);
    }
}
