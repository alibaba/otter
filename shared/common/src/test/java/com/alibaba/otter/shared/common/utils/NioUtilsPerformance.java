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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.io.IOUtils;

/**
 * 测试下nio的一些操作方法性能
 * 
 * <pre>
 * jvm args : 
 * -server -Xmx2g -Xms2g -Xmn512m -XX:PermSize=196m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCompressedOops
 * 
 * result :
 * 500Mb : 
 * copyTest cost : 12148 , 15924
 * channel cost : 14423 , 13847 
 * mapped cost : 7857 , 7883
 * sendfile cost : 5728 , 9352 
 * 
 * 1GB : 
 * copyTest cost : 32409 , 33557
 * channel cost : 32856 , 33305
 * mapped cost : 55789 , 52108
 * sendfile cost : 28179 , 30279
 * </pre>
 * 
 * @author jianghang 2011-10-10 下午03:31:17
 * @version 4.0.0
 */
public class NioUtilsPerformance {

    public static void main(String args[]) throws Exception {
        long start = System.currentTimeMillis();
        long end = -1;

        copyTest(new File("/tmp/source.tar.gz"), new File("/tmp/target-copy.tar.gz"));
        end = System.currentTimeMillis();
        System.out.printf("%s cost : %d \n", "copyTest", end - start);
        start = end;

        channelTest(new File("/tmp/source.tar.gz"), new File("/tmp/target-channel.tar.gz"));
        end = System.currentTimeMillis();
        System.out.printf("%s cost : %d \n", "channel", end - start);
        start = end;

        mappedTest(new File("/tmp/source.tar.gz"), new File("/tmp/target-mapped.tar.gz"));
        end = System.currentTimeMillis();
        System.out.printf("%s cost : %d \n", "mapped", end - start);
        start = end;

        sendFileTest(new File("/tmp/source.tar.gz"), new File("/tmp/target-sendile.tar.gz"));
        end = System.currentTimeMillis();
        System.out.printf("%s cost : %d \n", "sendfile", end - start);
    }

    public static void copyTest(File source, File target) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            target.createNewFile();

            byte[] bytes = new byte[16 * 1024];
            int n = -1;
            while ((n = fis.read(bytes, 0, bytes.length)) > 0) {
                fos.write(bytes, 0, n);
            }

            fos.flush();
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    public static void channelTest(File source, File target) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            FileChannel sChannel = fis.getChannel();
            FileChannel tChannel = fos.getChannel();

            target.createNewFile();

            ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
            while (sChannel.read(buffer) > 0) {
                buffer.flip();
                tChannel.write(buffer);
                buffer.clear();
            }

            tChannel.close();
            sChannel.close();
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    public static void mappedTest(File source, File target) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        MappedByteBuffer mapbuffer = null;

        try {
            long fileSize = source.length();
            final byte[] outputData = new byte[(int) fileSize];
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            FileChannel sChannel = fis.getChannel();

            target.createNewFile();

            mapbuffer = sChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            for (int i = 0; i < fileSize; i++) {
                outputData[i] = mapbuffer.get();
            }

            mapbuffer.clear();
            fos.write(outputData);
            fos.flush();
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);

            if (mapbuffer == null) {
                return;
            }

            final Object buffer = mapbuffer;

            AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    try {
                        Method clean = buffer.getClass().getMethod("cleaner", new Class[0]);

                        if (clean == null) {
                            return null;
                        }
                        clean.setAccessible(true);
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) clean.invoke(buffer, new Object[0]);
                        cleaner.clean();
                    } catch (Throwable ex) {
                    }

                    return null;
                }
            });
        }
    }

    public static void sendFileTest(File source, File target) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            FileChannel sChannel = fis.getChannel();
            FileChannel tChannel = fos.getChannel();
            target.createNewFile();
            sChannel.transferTo(0, source.length(), tChannel);
            tChannel.close();
            sChannel.close();
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }
}
