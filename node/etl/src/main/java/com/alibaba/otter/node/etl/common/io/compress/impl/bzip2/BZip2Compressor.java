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

package com.alibaba.otter.node.etl.common.io.compress.impl.bzip2;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.alibaba.otter.node.etl.common.io.compress.exception.CompressException;
import com.alibaba.otter.node.etl.common.io.compress.impl.AbstractCompressor;
import com.alibaba.otter.shared.common.utils.NioUtils;

/**
 * Implementation of the Compressor Interface for BZip2.
 * 
 * @author brave.taoy
 * @author jianghang 2011-10-9 下午02:33:57
 * @version 4.0.0
 */
public class BZip2Compressor extends AbstractCompressor {

    /* Default file extension */
    private static String       DEFAULT_FILE_EXTENSION = "bz2";

    /* Header BZ as byte-Array */
    private static final byte[] HEADER                 = new byte[] { (byte) 'B', (byte) 'Z', (byte) 'h' };

    /* Name of this implementation */
    private static final String NAME                   = "bz2";

    public BZip2Compressor(){
        super();
    }

    public void compressTo(InputStream in, OutputStream out) throws CompressException {
        BZip2CompressorOutputStream outputBZStream = null;
        try {
            outputBZStream = new BZip2CompressorOutputStream(out);
            NioUtils.copy(in, outputBZStream);
            outputBZStream.finish();
        } catch (Exception e) {
            throw new CompressException("bzip_compress_error", e);
        }
    }

    public void decompressTo(InputStream in, OutputStream out) throws CompressException {
        BZip2CompressorInputStream inputStream = null;
        try {
            inputStream = new BZip2CompressorInputStream(in);
            NioUtils.copy(inputStream, out);
        } catch (Exception e) {
            throw new CompressException("bzip_decompress_error", e);
        }
    }

    public byte[] getHeader() {
        return HEADER;
    }

    public String getName() {
        return NAME;
    }

    public String getDefaultFileExtension() {
        return DEFAULT_FILE_EXTENSION;
    }
}
