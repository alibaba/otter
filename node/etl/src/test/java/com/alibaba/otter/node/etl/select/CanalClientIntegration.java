package com.alibaba.otter.node.etl.select;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.canal.instance.manager.CanalConfigClient;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.node.etl.BaseOtterTest;

public class CanalClientIntegration extends BaseOtterTest {

    @SpringBeanByName
    private CanalConfigClient canalConfigClient;

    @BeforeClass
    public void setup() {
        System.setProperty("nid", "14");
    }

    @Test
    public void test_simple() {
        Canal canal = canalConfigClient.findCanal("ljh_canal_test01");
        System.out.println(canal);
    }
}
