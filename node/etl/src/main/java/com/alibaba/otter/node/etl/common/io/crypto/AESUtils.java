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

package com.alibaba.otter.node.etl.common.io.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.otter.shared.common.utils.ByteUtils;

/**
 * 加密算法的实现
 * 
 * @author jianghang 2011-10-9 下午05:31:00
 * @version 4.0.0
 */
public class AESUtils {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int    KEY_SIZE             = 128;
    private byte[]              secretKey;

    /**
     * 生成AES密钥
     * 
     * @return Secret key
     * @throws AESException AES exception
     */
    public byte[] generateSecretKey() throws AESException {
        try {
            // Get the KeyGenerator
            KeyGenerator kgen = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            kgen.init(KEY_SIZE); // 192 and 256 bits may not be available
            // Generate the secret key specs.
            SecretKey skey = kgen.generateKey();
            secretKey = skey.getEncoded();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            throw new AESException(e);
        }
    }

    /**
     * 加密byte数据
     * 
     * @param plainData
     * @return
     * @throws AESException
     */
    public byte[] encrypt(byte[] plainData) throws AESException {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey, ENCRYPTION_ALGORITHM);
            // Instantiate the cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return cipher.doFinal(plainData);
        } catch (NoSuchAlgorithmException e) {
            throw new AESException(e);
        } catch (NoSuchPaddingException e) {
            throw new AESException(e);
        } catch (InvalidKeyException e) {
            throw new AESException(e);
        } catch (IllegalBlockSizeException e) {
            throw new AESException(e);
        } catch (BadPaddingException e) {
            throw new AESException(e);
        }
    }

    /**
     * 解密byte数据
     * 
     * @param encrypted
     * @return
     * @throws AESException
     */
    public byte[] decrypt(byte[] encrypted) throws AESException {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey, ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException e) {
            throw new AESException(e);
        } catch (NoSuchPaddingException e) {
            throw new AESException(e);
        } catch (InvalidKeyException e) {
            throw new AESException(e);
        } catch (IllegalBlockSizeException e) {
            throw new AESException(e);
        } catch (BadPaddingException e) {
            throw new AESException(e);
        }
    }

    /**
     * 获取生成的加密密钥
     * 
     * @return
     */
    public String getSecretyKeyString() {
        return ByteUtils.bytesToBase64String(secretKey);
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public void setSecretKeyString(String keyString) {
        this.secretKey = ByteUtils.base64StringToBytes(keyString);
    }
}
