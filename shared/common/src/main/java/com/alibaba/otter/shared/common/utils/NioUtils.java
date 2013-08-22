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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于nio技术，提供一些快速的stream处理
 * 
 * @author jianghang 2011-10-9 下午06:28:44
 * @version 4.0.0
 */
public class NioUtils {

    private static final Logger logger              = LoggerFactory.getLogger(NioUtils.class);
    private static final int    DEFAULT_BUFFER_SIZE = 8 * 1024;
    private static final int    timeWait            = 1000;

    /**
     * 基于流的数据copy
     */
    public static long copy(InputStream input, OutputStream output, long offset) throws IOException {
        long count = 0;
        long n = 0;
        if (input instanceof FileInputStream) {
            FileChannel inChannel = ((FileInputStream) input).getChannel();
            WritableByteChannel outChannel = Channels.newChannel(output);
            count = inChannel.transferTo(offset, inChannel.size() - offset, outChannel);
        } else if (output instanceof FileOutputStream) {
            FileChannel outChannel = ((FileOutputStream) output).getChannel();
            ReadableByteChannel inChannel = Channels.newChannel(input);
            do {
                n = outChannel.transferFrom(inChannel, offset + count, DEFAULT_BUFFER_SIZE);
                count += n;
            } while (n > 0);
        } else {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            input.skip(offset);
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, (int) n);
                count += n;
            }
            // ReadableByteChannel inChannel = Channels.newChannel(input);
            // WritableByteChannel outChannel = Channels.newChannel(output);
            //            
            // //ByteBuffer buffer = new ByteBuffer(DEFAULT_BUFFER_SIZE);
            // ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
            // while (-1 != (n = inChannel.read(buffer))) {
            // outChannel.write(buffer);
            // count += n;
            // }
        }
        return count;
    }

    /**
     * 基于流的数据copy
     */
    public static long copy(InputStream input, OutputStream output, long offset, long count) throws IOException {
        long rcount = 0;
        long n = 0;
        if (input instanceof FileInputStream) {
            FileChannel inChannel = ((FileInputStream) input).getChannel();
            WritableByteChannel outChannel = Channels.newChannel(output);
            rcount = inChannel.transferTo(offset, count, outChannel);
        } else if (output instanceof FileOutputStream) {
            FileChannel outChannel = ((FileOutputStream) output).getChannel();
            ReadableByteChannel inChannel = Channels.newChannel(input);
            do {
                if (count < DEFAULT_BUFFER_SIZE) {
                    n = outChannel.transferFrom(inChannel, offset + rcount, count);
                } else {
                    n = outChannel.transferFrom(inChannel, offset + rcount, DEFAULT_BUFFER_SIZE);
                }
                count -= n;
                rcount += n;
            } while (n > 0);
        } else {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            input.skip(offset);
            while (count > 0) {
                if (count < DEFAULT_BUFFER_SIZE) {
                    n = input.read(buffer, 0, (int) count);
                } else {
                    n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
                }

                output.write(buffer, 0, (int) n);
                count -= n;
                rcount += n;
            }
            // ReadableByteChannel inChannel = Channels.newChannel(input);
            // WritableByteChannel outChannel = Channels.newChannel(output);
            //            
            // //ByteBuffer buffer = new ByteBuffer(DEFAULT_BUFFER_SIZE);
            // ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
            // while (-1 != (n = inChannel.read(buffer))) {
            // outChannel.write(buffer);
            // count += n;
            // }
        }
        return rcount;
    }

    /**
     * 基于流的数据copy
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        long n = 0;
        if (input instanceof FileInputStream) {
            FileChannel inChannel = ((FileInputStream) input).getChannel();
            WritableByteChannel outChannel = Channels.newChannel(output);
            count = inChannel.transferTo(0, inChannel.size(), outChannel);
        } else if (output instanceof FileOutputStream) {
            FileChannel outChannel = ((FileOutputStream) output).getChannel();
            ReadableByteChannel inChannel = Channels.newChannel(input);
            do {
                n = outChannel.transferFrom(inChannel, count, DEFAULT_BUFFER_SIZE);
                count += n;
            } while (n > 0);
        } else {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, (int) n);
                count += n;
            }
            // ReadableByteChannel inChannel = Channels.newChannel(input);
            // WritableByteChannel outChannel = Channels.newChannel(output);
            //            
            // //ByteBuffer buffer = new ByteBuffer(DEFAULT_BUFFER_SIZE);
            // ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
            // while (-1 != (n = inChannel.read(buffer))) {
            // outChannel.write(buffer);
            // count += n;
            // }
        }
        return count;
    }

    /**
     * 将byte[]数据写入到流中
     */
    public static void write(byte[] data, OutputStream output) throws IOException {
        ByteArrayInputStream input = null;
        try {
            input = new ByteArrayInputStream(data);
            copy(input, output);
            output.flush();
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 将byte数组写入文件
     */
    public static void write(final byte[] srcArray, File targetFile) throws IOException {
        write(srcArray, targetFile, true);
    }

    /**
     * 将byte数组写入文件
     */
    public static void write(final byte[] srcArray, File targetFile, final boolean overwrite) throws IOException {
        if (srcArray == null) {
            throw new IOException("Source must not be null");
        }

        if (targetFile == null) {
            throw new IOException("Target must not be null");
        }

        if (true == targetFile.exists()) {
            if (true == targetFile.isDirectory()) {
                throw new IOException("Target '" + targetFile.getAbsolutePath() + "' is directory!");
            } else if (true == targetFile.isFile()) {
                if (!overwrite) {
                    throw new IOException("Target file '" + targetFile.getAbsolutePath() + "' already exists!");
                }
            } else {
                throw new IOException("Invalid target object '" + targetFile.getAbsolutePath() + "'!");
            }
        } else {
            // create parent dir
            create(targetFile.getParentFile(), false, 3);
        }

        // 使用无拷贝的inputStream
        ByteArrayInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new ByteArrayInputStream(srcArray);
            output = new FileOutputStream(targetFile);
            copy(input, output);
            output.flush();
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 读取文件到数组
     */
    public static byte[] read(File sourceFile) throws IOException {
        if (sourceFile == null) {
            throw new IOException("Source must not be null");
        }

        if (false == sourceFile.exists()) {
            throw new IOException("Source '" + sourceFile + "' does not exist");
        }

        if (true == sourceFile.isDirectory()) {
            throw new IOException("Source '" + sourceFile + "' exists but is a directory");
        }

        FileInputStream input = null;
        ByteArrayOutputStream output = null;

        try {
            input = new FileInputStream(sourceFile);
            output = new ByteArrayOutputStream();
            copy(input, output);
            output.flush();
            return output.toByteArray();
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 从流中读数据到byte[]
     */
    public static byte[] read(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            copy(input, output);
            output.flush();
            return output.toByteArray();
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * Copy file overwrite
     */
    public static void copy(final File src, File dest) throws IOException {
        copy(src, dest, 1);
    }

    /**
     * 尝试多次复制文件，排除网络故障
     * 
     * @param src
     * @param dest
     * @param retryTimes
     * @throws IOException
     */
    public static boolean copy(final File src, File dest, final int retryTimes) throws IOException {
        int totalRetry = retryTimes;

        if (retryTimes < 1) {
            totalRetry = 1;
        }

        int retry = 0;
        while (retry++ < totalRetry) {
            try {
                copy(src, dest, true);
                return true;
            } catch (Exception ex) {
                // 本次等待时间
                int wait = (int) Math.pow(retry, retry) * timeWait;
                wait = (wait < timeWait) ? timeWait : wait;

                if (retry == totalRetry) {
                    if (ex instanceof IOException) {
                        throw (IOException) ex;
                    } else {
                        throw new IOException((ex == null) ? "unknow error" : ex.getMessage(), ex);
                    }
                } else {
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }

        return false;
    }

    /**
     * Copy source file to destination. If destination is a path then source file name is appended. If destination file
     * exists then: overwrite=true - destination file is replaced; overwite=false - exception is thrown
     */
    public static void copy(final File sourceFile, File targetFile, final boolean overwrite) throws IOException {
        if (sourceFile == null) {
            throw new IOException("Source must not be null");
        }

        if (targetFile == null) {
            throw new IOException("Target must not be null");
        }

        // checks
        if ((false == sourceFile.isFile()) || (false == sourceFile.exists())) {
            throw new IOException("Source file '" + sourceFile.getAbsolutePath() + "' not found!");
        }

        if (true == targetFile.exists()) {
            if (true == targetFile.isDirectory()) {

                // Directory? -> use source file name
                targetFile = new File(targetFile, sourceFile.getName());
            } else if (true == targetFile.isFile()) {
                if (false == overwrite) {
                    throw new IOException("Target file '" + targetFile.getAbsolutePath() + "' already exists!");
                }
            } else {
                throw new IOException("Invalid target object '" + targetFile.getAbsolutePath() + "'!");
            }
        } else {
            // create parent dir
            FileUtils.forceMkdir(targetFile.getParentFile());
        }

        FileInputStream input = null;
        FileOutputStream output = null;

        try {
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(targetFile);
            copy(input, output);
            output.flush();
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 正常创建
     */
    public static boolean create(File dest) {
        return create(dest, true, 1);
    }

    /**
     * 尝试多次创建
     */
    public static boolean create(File dest, final boolean isFile, final int retryTimes) {
        if (dest == null) {
            return false;
        }

        int totalRetry = retryTimes;

        if (retryTimes < 0) {
            totalRetry = 1;
        }

        int retry = 0;
        while (retry++ < totalRetry) {
            try {
                if (true == isFile) {
                    if ((true == dest.exists()) || (true == dest.createNewFile())) {
                        return true;
                    }
                } else {
                    FileUtils.forceMkdir(dest);
                    return true;
                }
            } catch (Exception ex) {
                // 本次等待时间
                int wait = (int) Math.pow(retry, retry) * timeWait;
                wait = (wait < timeWait) ? timeWait : wait;

                // 尝试等待
                if (retry == totalRetry) {
                    return false;
                } else {

                    // 记录日志
                    logger.warn(String.format("[%s] create() - retry %s failed : wait [%s] ms , caused by %s",
                                              dest.getAbsolutePath(), retry, wait, ex.getMessage()));
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 正常删除，实在不行就jvm退出时删除
     * 
     * @param dest
     * @param retryTimes
     */
    public static boolean delete(File dest) {
        return delete(dest, 1);
    }

    /**
     * 尝试多次删除，实在不行就jvm退出时删除
     * 
     * @param dest
     * @param retryTimes
     */
    public static boolean delete(File dest, final int retryTimes) {
        if (dest == null) {
            return false;
        }

        if (false == dest.exists()) {
            return true;
        }

        int totalRetry = retryTimes;
        if (retryTimes < 1) {
            totalRetry = 1;
        }

        int retry = 0;
        while (retry++ < totalRetry) {
            try {
                FileUtils.forceDelete(dest);
                return true;
            } catch (FileNotFoundException ex) {
                return true;
            } catch (Exception ex) {
                // 本次等待时间
                int wait = (int) Math.pow(retry, retry) * timeWait;
                wait = (wait < timeWait) ? timeWait : wait;
                if (retry == totalRetry) {
                    try {
                        FileUtils.forceDeleteOnExit(dest);
                        return false;
                    } catch (Exception e) {
                        // ignore
                    }
                } else {
                    // 记录日志
                    logger.warn(String.format("[%s] delete() - retry %s failed : wait [%s] ms , caused by %s",
                                              dest.getAbsolutePath(), retry, wait, ex.getMessage()));
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }

        return false;
    }

    /**
     * Move file without retry
     */
    public static void move(final File src, File dest) throws IOException {
        move(src, dest, 1);
    }

    /**
     * Moves the source file to the destination. If the destination cannot be created or is a read-only file, the method
     * returns <code>false</code>. Otherwise, the contents of the source are copied to the destination, the source is
     * deleted, and <code>true</code> is returned.
     * 
     * @param src The source file to move.
     * @param dest The destination where to move the file.
     * @param retryTimes Move and delete retry times
     */
    public static void move(final File src, File dest, final int retryTimes) throws IOException {
        copy(src, dest, retryTimes);
        delete(src, retryTimes);
    }

}
