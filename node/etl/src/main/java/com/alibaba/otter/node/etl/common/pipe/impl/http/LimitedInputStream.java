package com.alibaba.otter.node.etl.common.pipe.impl.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * copy from google protobuf，支持带limit限制读取数量的功能，将stream可用于流式读取
 * 
 * @author jianghang 2013-8-29 下午3:34:32
 * @since 4.2.1
 */
public class LimitedInputStream extends FilterInputStream {

    private int limit;

    public LimitedInputStream(InputStream in, int limit){
        super(in);
        this.limit = limit;
    }

    @Override
    public int available() throws IOException {
        return Math.min(super.available(), limit);
    }

    @Override
    public int read() throws IOException {
        if (limit <= 0) {
            return -1;
        }
        final int result = super.read();
        if (result >= 0) {
            --limit;
        }
        return result;
    }

    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        if (limit <= 0) {
            return -1;
        }
        len = Math.min(len, limit);
        final int result = super.read(b, off, len);
        if (result >= 0) {
            limit -= result;
        }
        return result;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long result = super.skip(Math.min(n, limit));
        if (result >= 0) {
            limit -= result;
        }
        return result;
    }
}
