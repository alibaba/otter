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

package com.alibaba.otter.node.etl.common.io.compress.impl.gzip;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.alibaba.otter.node.etl.common.io.compress.exception.CompressException;
import com.alibaba.otter.node.etl.common.io.compress.impl.AbstractCompressor;
import com.alibaba.otter.shared.common.utils.NioUtils;

/**
 * 基于gzip的压缩实现
 * 
 * @author jianghang 2011-10-9 下午03:17:08
 * @version 4.0.0
 */
public class GzipCompressor extends AbstractCompressor {

    /* Default file extension */
    private static String       DEFAULT_FILE_EXTENSION = "gzip";

    /* Name of this implementation */
    private static final String NAME                   = "gzip";

    /*
     * GZIP header magic number.
     */
    private final static int    GZIP_MAGIC             = 0x8b1f;

    /* Header BZ as byte-Array */
    private static final byte[] HEADER                 = new byte[] { (byte) GZIP_MAGIC, // Magic number (short)
            (byte) (GZIP_MAGIC >> 8), // Magic number (short)
            Deflater.DEFLATED, // Compression method (CM)
            0, // Flags (FLG)
            0, // Modification time MTIME (int)
            0, // Modification time MTIME (int)
            0, // Modification time MTIME (int)
            0, // Modification time MTIME (int)
            0, // Extra flags (XFLG)
            0                                         // Operating system (OS)
                                                       };

    public GzipCompressor(){
        super();
    }

    public void compressTo(InputStream in, OutputStream out) throws CompressException {
        GZIPOutputStream gzipOut = null;
        try {
            gzipOut = new GZIPOutputStream(out);
            NioUtils.copy(in, gzipOut);
            gzipOut.finish(); //需要使用finish
        } catch (Exception e) {
            throw new CompressException("gzip_compress_error", e);
        }
    }

    public void decompressTo(InputStream in, OutputStream out) throws CompressException {
        GZIPInputStream gzipin = null;
        try {
            gzipin = new GZIPInputStream(in);
            NioUtils.copy(gzipin, out);
            out.flush();
        } catch (Exception e) {
            throw new CompressException("gzip_decompress_error", e);
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
