/*
 * Copyright (C) 2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
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
package com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal;

import static com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.Crc32C.maskedCrc32c;
import static com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.SnappyInternalUtils.checkNotNull;
import static com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.SnappyInternalUtils.checkPositionIndexes;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements an output stream for writing Snappy compressed data.
 * The output format is the stream header "snappy\0" followed by one or more
 * compressed blocks of data, each of which is preceded by a seven byte header.
 * <p/>
 * The first byte of the header is a flag indicating if the block is compressed
 * or not. A value of 0x00 means uncompressed, and 0x01 means compressed.
 * <p/>
 * The second and third bytes are the size of the block in the stream as a big
 * endian number. This value is never zero as empty blocks are never written.
 * The maximum allowed length is 32k (1 << 15).
 * <p/>
 * The remaining four byes are crc32c checksum of the user input data masked
 * with the following function: {@code ((crc >>> 15) | (crc << 17)) + 0xa282ead8
 * * }
 * <p/>
 * An uncompressed block is simply copied from the input, thus guaranteeing that
 * the output is never larger than the input (not including the header).
 */
public class SnappyOutputStream extends OutputStream {
    static final byte[]          STREAM_HEADER  = new byte[] { 's', 'n', 'a', 'p', 'p', 'y', 0 };

    // the header format requires the max block size to fit in 15 bits -- do not change!
    static final int             MAX_BLOCK_SIZE = 1 << 15;

    private final BufferRecycler recycler;
    private final byte[]         buffer;
    private final byte[]         outputBuffer;
    private final OutputStream   out;
    private final boolean        writeChecksums;

    private int                  position;
    private boolean              closed;

    /**
     * Creates a Snappy output stream to write data to the specified underlying
     * output stream.
     * 
     * @param out the underlying output stream
     */
    public SnappyOutputStream(OutputStream out) throws IOException {
        this(out, true);
    }

    /**
     * Creates a Snappy output stream with block checksums disabled. This is
     * only useful for apples-to-apples benchmarks with other compressors that
     * do not perform block checksums.
     * 
     * @param out the underlying output stream
     */
    public static SnappyOutputStream newChecksumFreeBenchmarkOutputStream(OutputStream out)
            throws IOException {
        return new SnappyOutputStream(out, false);
    }

    private SnappyOutputStream(OutputStream out, boolean writeChecksums) throws IOException {
        this.out = checkNotNull(out, "out is null");
        this.writeChecksums = writeChecksums;
        recycler = BufferRecycler.instance();
        buffer = recycler.allocOutputBuffer(MAX_BLOCK_SIZE);
        outputBuffer = recycler.allocEncodingBuffer(Snappy.maxCompressedLength(MAX_BLOCK_SIZE));
        out.write(STREAM_HEADER);
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        if (position >= MAX_BLOCK_SIZE) {
            flushBuffer();
        }
        buffer[position++] = (byte) b;
    }

    @Override
    public void write(byte[] input, int offset, int length) throws IOException {
        checkNotNull(input, "input is null");
        checkPositionIndexes(offset, offset + length, input.length);
        if (closed) {
            throw new IOException("Stream is closed");
        }

        int free = MAX_BLOCK_SIZE - position;

        // easy case: enough free space in buffer for entire input
        if (free >= length) {
            copyToBuffer(input, offset, length);
            return;
        }

        // fill partial buffer as much as possible and flush
        if (position > 0) {
            copyToBuffer(input, offset, free);
            flushBuffer();
            offset += free;
            length -= free;
        }

        // write remaining full blocks directly from input array
        while (length >= MAX_BLOCK_SIZE) {
            writeCompressed(input, offset, MAX_BLOCK_SIZE);
            offset += MAX_BLOCK_SIZE;
            length -= MAX_BLOCK_SIZE;
        }

        // copy remaining partial block into now-empty buffer
        copyToBuffer(input, offset, length);
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        flushBuffer();
        out.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
            out.close();
        } finally {
            if (closed) {
                closed = true;
                recycler.releaseOutputBuffer(outputBuffer);
                recycler.releaseEncodeBuffer(buffer);
            }
        }
    }

    private void copyToBuffer(byte[] input, int offset, int length) {
        System.arraycopy(input, offset, buffer, position, length);
        position += length;
    }

    private void flushBuffer() throws IOException {
        if (position > 0) {
            writeCompressed(buffer, 0, position);
            position = 0;
        }
    }

    private void writeCompressed(byte[] input, int offset, int length) throws IOException {
        // crc is based on the user supplied input data
        int crc32c = writeChecksums ? maskedCrc32c(input, offset, length) : 0;

        int compressed = Snappy.compress(input, offset, length, outputBuffer, 0);

        // use uncompressed input if less than 12.5% compression
        if (compressed >= (length - (length / 8))) {
            writeBlock(input, offset, length, false, crc32c);
        } else {
            writeBlock(outputBuffer, 0, compressed, true, crc32c);
        }
    }

    private void writeBlock(byte[] data, int offset, int length, boolean compressed, int crc32c)
            throws IOException {
        // write compressed flag
        out.write(compressed ? 0x01 : 0x00);

        // write length
        out.write(length >>> 8);
        out.write(length);

        // write crc32c of user input data
        out.write(crc32c >>> 24);
        out.write(crc32c >>> 16);
        out.write(crc32c >>> 8);
        out.write(crc32c);

        // write data
        out.write(data, offset, length);
    }
}
