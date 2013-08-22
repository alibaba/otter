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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.common.io.compress.Compressor;
import com.alibaba.otter.node.etl.common.io.compress.impl.PackableObject;
import com.alibaba.otter.node.etl.common.io.compress.impl.bzip2.BZip2Compressor;
import com.alibaba.otter.node.etl.common.io.compress.impl.gzip.GzipCompressor;
import com.alibaba.otter.shared.common.utils.NioUtils;

/**
 * 数据压缩测试类
 * 
 * @author jianghang 2011-10-10 上午09:06:32
 * @version 4.0.0
 */
public class CompressorTest extends BaseOtterTest {

    private static Compressor[] comps = new Compressor[] { new GzipCompressor(), new BZip2Compressor() };

    @Test
    public void test_stream() {
        try {
            byte[] data = getBlock(20 * 1024);
            byte[] result;
            for (int i = 0; i < comps.length; i++) {
                ByteArrayInputStream input = new ByteArrayInputStream(data);
                Compressor comp = comps[i];
                // 基于流的处理
                InputStream encrypt = comp.compress(input);
                InputStream decrypt = comp.decompress(encrypt);
                result = NioUtils.read(decrypt);
                check(data, result);
                encrypt.close();
                decrypt.close();
            }

        } catch (Exception e) {
            want.fail();
        }
    }

    @Test
    public void test_block() {
        try {
            byte[] data = getBlock(20 * 1024);
            for (int i = 0; i < comps.length; i++) {
                Compressor comp = comps[i];
                // 基于流的处理
                byte[] encrypt = comp.compress(data);
                byte[] decrypt = comp.decompress(encrypt);
                check(data, decrypt);
            }

        } catch (Exception e) {
            want.fail();
        }
    }

    @Test
    public void test_identify() {
        try {
            byte[] data = getBlock(20 * 1024);
            File input = File.createTempFile("compress_", "src");
            NioUtils.write(data, input);//写入数据到文件

            for (int i = 0; i < comps.length; i++) {
                Compressor comp = comps[i];
                // 基于流的处理
                File output = File.createTempFile("compress_", "jkt");
                comp.compressTo(input, output);

                Compressor icomp = (Compressor) PackableObject.identifyByHeader(output, Arrays.asList(comps));

                InputStream decrypt = icomp.decompress(output);
                byte[] result = NioUtils.read(decrypt);
                check(data, result);
            }

        } catch (Exception e) {
            want.fail();
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
