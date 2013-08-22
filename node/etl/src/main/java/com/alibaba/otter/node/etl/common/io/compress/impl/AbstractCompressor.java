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

package com.alibaba.otter.node.etl.common.io.compress.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.alibaba.otter.node.etl.common.io.compress.Compressor;
import com.alibaba.otter.node.etl.common.io.compress.exception.CompressException;

/**
 * AbstractCompressor handles all compression/decompression actions on an abstract basis.
 */
public abstract class AbstractCompressor extends PackableObject implements Compressor {

    public AbstractCompressor(){
        super();
    }

    /**
     * Returns a String with the default file extension for this compressor. For example, a zip-files default file
     * extension would be "zip" (without leading dot).
     * 
     * @return the default file extension
     */
    public abstract String getDefaultFileExtension();

    public InputStream compress(InputStream input) throws CompressException {
        FileOutputStream output = null;
        try {
            File temp = File.createTempFile("compress_", "jkt");
            output = new FileOutputStream(temp);
            //转化为流进行处理
            compressTo(input, output);
            return new FileInputStream(temp);
        } catch (IOException e) {
            throw new CompressException("An I/O Exception has occured", e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    public byte[] compress(byte[] data) throws CompressException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            this.compressTo(input, output);
            return output.toByteArray();
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    public void compressToHere(File input) throws CompressException {
        String pathToFile = input.getAbsolutePath();
        File output = new File(pathToFile);
        this.compressTo(input, output);
    }

    public void compressTo(File input, File output) throws CompressException {
        FileOutputStream outputStream = null;
        FileInputStream inputStream = null;
        try {
            outputStream = new FileOutputStream(output);
            inputStream = new FileInputStream(input);
            this.compressTo(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            throw new CompressException("File not found", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    public InputStream compress(File input) throws CompressException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(input);
            return this.compress(inputStream);
        } catch (FileNotFoundException e) {
            throw new CompressException("File not found", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public InputStream decompress(File input) throws CompressException {
        File temp = null;
        InputStream result = null;
        try {
            temp = File.createTempFile("compress_", "jkt");
            this.decompressTo(input, temp);
            result = new FileInputStream(temp);
        } catch (IOException e) {
            throw new CompressException("Error while creating a temporary file", e);
        }

        return result;
    }

    public InputStream decompress(InputStream input) throws CompressException {
        File temp = null;
        InputStream result = null;
        FileOutputStream output = null;
        try {
            temp = File.createTempFile("compress_", "jkt");
            output = new FileOutputStream(temp);
            this.decompressTo(input, output);
            result = new FileInputStream(temp);
        } catch (IOException e) {
            throw new CompressException("Error while creating a temporary file", e);
        } finally {
            IOUtils.closeQuietly(output);
        }

        return result;
    }

    public byte[] decompress(byte[] data) throws CompressException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            this.decompressTo(input, output);
            return output.toByteArray();
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    public void decompressTo(File input, File output) throws CompressException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(output);
            inputStream = new FileInputStream(input);
            decompressTo(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            throw new CompressException("File could not be found", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

}
