package com.alibaba.otter.shared.communication;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.communication.core.CommunicationClient;
import com.alibaba.otter.shared.communication.core.model.heart.HeartEvent;

/**
 * @author jianghang
 */
public class CommunicationSpringIntegrateTest extends BaseOtterTest {

    @SpringBeanByName
    private CommunicationClient dubboCommunicationClient;

    @SpringBeanByName
    private CommunicationClient rmiPoolCommunicationClient;

    @SpringBeanByName
    private CommunicationClient rmiCommunicationClient;

    @Test
    public void testRmiSingle() {
        Object result = rmiCommunicationClient.call("127.0.0.1:1099", new HeartEvent());
        want.object(result).notNull();
    }

    @Test
    public void testRmiPool() {
        Object result = rmiPoolCommunicationClient.call("127.0.0.1:1099", new HeartEvent());
        want.object(result).notNull();
    }

    @Test
    public void testDubboSingle() {
        Object result = dubboCommunicationClient.call("127.0.0.1:2088", new HeartEvent());
        want.object(result).notNull();
    }

}
