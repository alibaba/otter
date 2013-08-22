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

package com.alibaba.otter.shared.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;

/**
 * NioUtils单元测试
 * 
 * @author jianghang 2011-10-10 下午02:25:56
 * @version 4.0.0
 */
public class NioUtilsTest extends BaseOtterTest {

    private static final String tmp = System.getProperty("java.io.tmpdir", "/tmp");

    @Test
    public void test_create_delete() {
        File file = new File(tmp + "/nioTestFile.txt");
        NioUtils.create(file);
        NioUtils.delete(file);

        File dir = new File(tmp + "/nioTestDir");
        NioUtils.create(dir, false, 1);
        NioUtils.delete(dir);
    }

    public void test_read_write_byte() {
        File file = new File("/tmp/nioTestFile.txt");
        NioUtils.create(file);
        byte[] data = getBlock(10 * 1024);

        try {
            NioUtils.write(data, file);
            byte[] result = NioUtils.read(file);
            check(data, result);
        } catch (IOException e) {
            e.printStackTrace();
            want.fail();
        } finally {
            NioUtils.delete(file);
        }
    }

    public void test_read_write_stream() {
        File file = new File(tmp, "nioTestFile.txt");
        NioUtils.create(file);
        byte[] data = getBlock(10 * 1024);

        try {
            NioUtils.write(data, new FileOutputStream(file));
            byte[] result = NioUtils.read(new FileInputStream(file));
            check(data, result);
        } catch (IOException e) {
            e.printStackTrace();
            want.fail();
        } finally {
            NioUtils.delete(file);
        }
    }

    @Test
    public void test_move() {
        File src = new File(tmp, "nioTestFile1.txt");
        File dest = new File(tmp, "nioTestFile2.txt");
        NioUtils.create(src);
        byte[] data = getBlock(10 * 1024);

        try {
            NioUtils.write(data, new FileOutputStream(src));
            NioUtils.move(src, dest);
            byte[] result = NioUtils.read(new FileInputStream(dest));
            check(data, result);
        } catch (IOException e) {
            e.printStackTrace();
            want.fail();
        } finally {
            NioUtils.delete(src);
            NioUtils.delete(dest);
        }
    }

    @Test
    public void test_copy_offest() {
        File file = new File(tmp, "nioTestFile.txt");
        NioUtils.create(file);
        byte[] data = getBlock(20 * 1024);

        try {
            NioUtils.write(data, new FileOutputStream(file));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NioUtils.copy(new FileInputStream(file), output, 10 * 1024, 5 * 1024);

            byte[] result = output.toByteArray();
            for (int i = 0; i < result.length; i++) {
                if (result[i] != data[i + 10 * 1024]) {
                    want.fail();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            want.fail();
        } finally {
            NioUtils.delete(file);
        }
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
