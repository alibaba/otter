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
