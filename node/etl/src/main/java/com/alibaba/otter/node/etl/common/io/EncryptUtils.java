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

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.node.etl.common.io.compress.Compressor;
import com.alibaba.otter.node.etl.common.io.compress.impl.gzip.GzipCompressor;
import com.alibaba.otter.node.etl.common.io.crypto.AESUtils;
import com.alibaba.otter.node.etl.common.io.signature.ChecksumException;
import com.alibaba.otter.node.etl.common.io.signature.ChecksumUtils;

/**
 * 数据对象间转换的工具类
 * 
 * @author jianghang 2011-10-13 下午07:37:46
 * @version 4.0.0
 */
public class EncryptUtils {

    private static final Compressor COMPRESSOR = new GzipCompressor();

    public static EncryptedData encrypt(byte[] input) {
        // 压缩数据
        byte[] compData = COMPRESSOR.compress(input);

        // 调用加密工具类
        AESUtils aes = new AESUtils();
        aes.generateSecretKey();

        // 加密数据
        byte[] encryptData = aes.encrypt(compData);
        return new EncryptedData(encryptData, aes.getSecretyKeyString(), ChecksumUtils.checksum(encryptData));
    }

    public static byte[] decrypt(EncryptedData encode) {
        String destCrc = ChecksumUtils.checksum(encode.getData());
        //验证sign
        if (false == StringUtils.equals(encode.getCrc(), destCrc)) {
            throw new ChecksumException(String.format("orig: %s, parsed: %s not match", encode.getCrc(), destCrc));
        }

        // 调用加密工具类
        AESUtils aes = new AESUtils();
        aes.setSecretKeyString(encode.getKey());
        // 解密并解压数据
        return COMPRESSOR.decompress(aes.decrypt(encode.getData()));
    }
}
