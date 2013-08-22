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

package com.alibaba.otter.node.etl.common.io.compress;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.otter.node.etl.common.io.compress.exception.CompressException;

/**
 * The Compressor Interface defines all operations for the compress/decompress actions.
 */
public interface Compressor {

    /**
     * Compresses this file and returns an InputStream to the compressed File
     * 
     * @param input File to compress
     * @return InputStream of the compressed file
     * @throws CompressException if the Compressor reports an error
     */
    public InputStream compress(File input) throws CompressException;

    /**
     * Compresses this InputStream and returns an InputStream to the compressed file
     * 
     * @param input Stream to compress
     * @return Stream to the compressed file
     * @throws CompressException if the Compressor reports an error
     */
    public InputStream compress(InputStream input) throws CompressException;

    /**
     * Compresses this bytes and returns an bytes
     * 
     * @param data
     * @return
     * @throws CompressException
     */
    public byte[] compress(byte[] data) throws CompressException;

    /**
     * Compresses the file input and creates a file in the same directory with the default file extension in its name.
     * 
     * @param input the file to compress
     * @throws CompressException if the Compressor reports an error
     */
    public void compressToHere(File input) throws CompressException;

    /**
     * Creates the file "output" with the compressed content of file "input"
     * 
     * @param input the file to compress
     * @param output the file to create
     * @throws CompressException if the Compressor reports an error
     */
    public void compressTo(File input, File output) throws CompressException;

    /**
     * Compresses the input stream and writes the compressed bytes to the output stream. This method must be implemented
     * by all new compressortypes.
     * 
     * @param input InputStream to compress to
     * @param output OutputStream to which the byte shall be written
     * @throws CompressException if the Compressor reports an error
     */
    public void compressTo(InputStream input, OutputStream output) throws CompressException;

    /**
     * Decompresses a file and returns an InputStream
     * 
     * @param input file to decompress
     * @return the decompressed file as an inputstream
     */
    public InputStream decompress(File input) throws CompressException;

    /**
     * Decompresses a file and returns an InputStream
     * 
     * @param input inputstream to decompress
     * @return the decompressed InputStream
     */
    public InputStream decompress(InputStream input) throws CompressException;

    /**
     * Decompresses this bytes and returns an bytes
     * 
     * @param data
     * @return
     * @throws CompressException
     */
    public byte[] decompress(byte[] data) throws CompressException;

    /**
     * Decompresses this file and writes the decompressed byte to the output file
     * 
     * @param input File to decompress
     * @param output File to write the decompressed bytes to
     * @throws CompressException if the Compressor reports an error
     */
    public void decompressTo(File input, File output) throws CompressException;

    /**
     * Decompresses this file and writes the decompressed file to the output-stream
     * 
     * @param input Stream to decompress
     * @param output Stream to write the decompressed bytes to
     * @throws CompressException if the Compressor reports an error
     */
    public void decompressTo(InputStream input, OutputStream output) throws CompressException;

}
