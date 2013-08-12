package com.alibaba.otter.node.etl.common.io.compress.impl.snappy;

import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.otter.node.etl.common.io.compress.exception.CompressException;
import com.alibaba.otter.node.etl.common.io.compress.impl.AbstractCompressor;
import com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.SnappyInputStream;
import com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.SnappyOutputStream;
import com.alibaba.otter.shared.common.utils.NioUtils;

/**
 * 谷歌开源的snappy压缩算法</br>
 * 
 * <pre>
 * 1. C版： http://code.google.com/p/snappy
 * 2. java版本 : https://github.com/dain/snappy
 * </pre>
 * 
 * @author jianghang 2011-10-9 下午03:56:08
 * @version 4.0.0
 */
public class SnappyCompressor extends AbstractCompressor {

    /* Default file extension */
    private static String DEFAULT_FILE_EXTENSION = "snp";

    /* Name of this implementation */
    private static String NAME                   = "snappy";

    /* Header BZ as byte-Array */
    private static byte[] HEADER                 = new byte[] { 's', 'n', 'a', 'p', 'p', 'y', 0 };

    public void compressTo(InputStream input, OutputStream output) throws CompressException {
        SnappyOutputStream snappyOut = null;
        try {
            snappyOut = SnappyOutputStream.newChecksumFreeBenchmarkOutputStream(output);// 不需要添加chucksum
            NioUtils.copy(input, snappyOut);
            snappyOut.flush();
        } catch (Exception e) {
            throw new CompressException("snappy_compress_error", e);
        }
    }

    public void decompressTo(InputStream input, OutputStream output) throws CompressException {
        SnappyInputStream snappyIn = null;
        try {
            snappyIn = new SnappyInputStream(input, false);
            NioUtils.copy(snappyIn, output);
        } catch (Exception e) {
            throw new CompressException("snappy_decompress_error", e);
        }
    }

    public String getDefaultFileExtension() {
        return DEFAULT_FILE_EXTENSION;
    }

    public byte[] getHeader() {
        return HEADER;
    }

    public String getName() {
        return NAME;
    }

}
