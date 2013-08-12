package com.alibaba.otter.node.etl.common.io;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.io.signature.ChecksumUtils;
import com.alibaba.otter.node.etl.BaseOtterTest;

public class ChecksumUtilsTest extends BaseOtterTest {

    @Test
    public void test_simple() {
        byte[] data = getBlock(10 * 1024);
        String c1 = ChecksumUtils.checksum(data);
        String c2 = ChecksumUtils.checksum(data, 0, data.length);
        System.out.println("checksum : " + c1 + " " + c2);
        want.string(c1).isEqualTo(c2);
    }

    private byte[] getBlock(int length) {
        byte[] rawData = new byte[length];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = (byte) (' ' + RandomUtils.nextInt(95));

        }
        return rawData;
    }
}
