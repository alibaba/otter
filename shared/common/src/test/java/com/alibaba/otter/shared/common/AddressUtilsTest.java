package com.alibaba.otter.shared.common;

import java.net.InetAddress;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.utils.AddressUtils;

public class AddressUtilsTest extends BaseOtterTest {

    @Test
    public void testHostIp() {
        InetAddress address = AddressUtils.getHostAddress();
        want.bool(address.isLoopbackAddress()).is(false);

        // want.bool(AddressUtils.isHostIp("10.12.48.171")).is(true);
    }

    @Test
    public void testPort() {
        want.bool(AddressUtils.isAvailablePort(23)).is(false);
    }

}
