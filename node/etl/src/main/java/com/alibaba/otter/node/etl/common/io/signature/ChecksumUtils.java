package com.alibaba.otter.node.etl.common.io.signature;

/**
 * 提供基于checksum的签名方式
 * 
 * @author jianghang 2011-10-9 下午05:43:04
 * @version 4.0.0
 */
public class ChecksumUtils {

    public static String checksum(byte[] bytes) {
        int sum = Crc32C.maskedCrc32c(bytes, 0, bytes.length);
        return Integer.toString(sum);
    }

    public static String checksum(byte[] bytes, int offset, int length) {
        int sum = Crc32C.maskedCrc32c(bytes, offset, length);
        return Integer.toString(sum);
    }

}
