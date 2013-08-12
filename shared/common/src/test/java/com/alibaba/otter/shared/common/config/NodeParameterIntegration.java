package com.alibaba.otter.shared.common.config;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.node.NodeParameter;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;
import com.alibaba.otter.shared.common.utils.JsonUtils;

public class NodeParameterIntegration extends BaseOtterTest {

    @Test
    public void test_simple() {
        NodeParameter param = new NodeParameter();
        param.setStoreClusters(Arrays.asList("10.20.153.51:15000,10.20.153.52:15001",
                                             "10.20.153.51:15000,10.20.153.52:15001"));

        String json = JsonUtils.marshalToString(param);
        System.out.println(json);
        NodeParameter p = JsonUtils.unmarshalFromString(json, NodeParameter.class);
        want.collection(p.getStoreClusters()).hasItems(param.getStoreClusters());
    }

    @Test
    public void test_pipeline() {
        String json = "{\"destinationName\":\"hzbopstest\",\"dumpEvent\":true,\"eromangaPassword\":\"hello@1234\",\"eromangaServerAddresses\":[{\"address\":\"172.30.16.33\",\"port\":11111},{\"address\":\"172.30.16.31\",\"port\":11111},{\"address\":\"172.30.16.32\",\"port\":11111}],\"eromangaUsername\":\"admin\",\"extractPoolSize\":5,\"home\":true,\"lbAlgorithm\":\"Random\",\"loadPoolSize\":5,\"mainstemBatchsize\":50000,\"mainstemClientId\":1001,\"needDeleteColumns\":true,\"parallelism\":5,\"pipeChooseType\":\"AUTOMATIC\",\"processBatchsizeThresold\":5000,\"selectorMode\":\"Eromanga\",\"storeExpiredTime\":60,\"subBatchsize\":15000,\"subClientId\":1002,\"useBatch\":true}";
        PipelineParameter param = JsonUtils.unmarshalFromString(json, PipelineParameter.class);
        param.setSyncConsistency(SyncConsistency.BASE);
        param.setEnableMainstem(true);
        param.merge(new PipelineParameter());
        System.out.println(param.isEnableMainstem());

        json = JsonUtils.marshalToStringWithoutTransient(param);
        System.out.println(json);

    }
}
