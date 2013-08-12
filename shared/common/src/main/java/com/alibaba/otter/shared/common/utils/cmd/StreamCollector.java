package com.alibaba.otter.shared.common.utils.cmd;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * An object that reads a stream asynchronously and collects it into a data buffer.
 */
public class StreamCollector extends StreamCopier {

    public StreamCollector(InputStream stream){
        // 使用alibaba common io，避免byte多次拷贝
        super(stream, new ByteArrayOutputStream());
    }

    public String toString() {
        return new String(((ByteArrayOutputStream) this.out).toByteArray());
    }
}
