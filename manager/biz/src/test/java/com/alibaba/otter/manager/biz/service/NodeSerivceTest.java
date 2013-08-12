package com.alibaba.otter.manager.biz.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Mocked;

import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.Test;

import com.alibaba.otter.manager.biz.BaseOtterTest;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.node.dal.NodeDAO;
import com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;

public class NodeSerivceTest extends BaseOtterTest {

    @SpringBeanByName
    private NodeService            nodeService;

    @SpringBeanFrom
    @Mocked
    private NodeDAO                nodeDao;

    @SpringBeanFrom
    @Mocked
    private ArbitrateManageService arbitrateManageService;

    @Test
    public void testListAllNodes() {
        new NonStrictExpectations() {

            {
                nodeDao.listAll();
                List<NodeDO> nodeDos = new ArrayList<NodeDO>();
                for (int i = 0; i < 10; i++) {
                    NodeDO nodeDo = new NodeDO();
                    nodeDo.setId(Long.valueOf(1));
                    nodeDo.setIp("192.168.0.1");
                    nodeDos.add(nodeDo);
                }
                returns(nodeDos);

                arbitrateManageService.nodeEvent();
                returns(new NodeArbitrateEvent() {

                    @Override
                    public List<Long> liveNodes() {
                        return Arrays.asList(1L);
                    }

                });
            }
        };

        want.number(nodeService.listAll().size()).isEqualTo(10);
        want.string(nodeService.listAll().get(0).getIp()).isEqualTo("192.168.0.1");
    }
}
