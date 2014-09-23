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

package com.alibaba.otter.node.etl.common.io;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.io.crypto.AESUtils;
import com.alibaba.otter.node.etl.BaseOtterTest;

public class AESUtilsTest extends BaseOtterTest {

    @Test
    public void test_simple() {
        AESUtils aes = new AESUtils();
        aes.generateSecretKey();
        byte[] data = getBlock(10 * 1024);
        byte[] encrypt = aes.encrypt(data);
        byte[] decrypt = aes.decrypt(encrypt);
        System.out.println("data length : " + data.length + " " + encrypt.length);
        check(data, decrypt);
    }

    private void check(byte[] src, byte[] dest) {
        want.object(src).notNull();
        want.object(dest).notNull();
        want.bool(src.length == dest.length).is(true);

        for (int i = 0; i < src.length; i++) {
            if (src[i] != dest[i]) {
                want.fail();
            }
        }
    }

    private byte[] getBlock(int length) {
        byte[] rawData = new byte[length];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = (byte) (' ' + RandomUtils.nextInt(95));

        }
        return rawData;
    }
}
