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

import java.lang.ref.SoftReference;

/**
 * Simple helper class to encapsulate details of basic buffer
 * recycling scheme, which helps a lot (as per profiling) for
 * smaller encoding cases.
 *
 * @author tatu
 */
class BufferRecycler
{
    private final static int MIN_ENCODING_BUFFER = 4000;

    private final static int MIN_OUTPUT_BUFFER = 8000;

    /**
     * This <code>ThreadLocal</code> contains a {@link java.lang.ref.SoftReference}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling for buffers we need for encoding, decoding.
     */
    final protected static ThreadLocal<SoftReference<BufferRecycler>> recyclerRef
            = new ThreadLocal<SoftReference<BufferRecycler>>();


    private byte[] inputBuffer;
    private byte[] outputBuffer;

    private byte[] decodingBuffer;
    private byte[] encodingBuffer;

    private short[] encodingHash;


    /**
     * Accessor to get thread-local recycler instance
     */
    public static BufferRecycler instance()
    {
        SoftReference<BufferRecycler> ref = recyclerRef.get();

        BufferRecycler bufferRecycler;
        if (ref == null) {
            bufferRecycler = null;
        }
        else {
            bufferRecycler = ref.get();
        }

        if (bufferRecycler == null) {
            bufferRecycler = new BufferRecycler();
            recyclerRef.set(new SoftReference<BufferRecycler>(bufferRecycler));
        }
        return bufferRecycler;
    }

    ///////////////////////////////////////////////////////////////////////
    // Buffers for encoding (output)
    ///////////////////////////////////////////////////////////////////////

    public byte[] allocEncodingBuffer(int minSize)
    {
        byte[] buf = encodingBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_ENCODING_BUFFER)];
        }
        else {
            encodingBuffer = null;
        }
        return buf;
    }

    public void releaseEncodeBuffer(byte[] buffer)
    {
        if (encodingBuffer == null || buffer.length > encodingBuffer.length) {
            encodingBuffer = buffer;
        }
    }

    public byte[] allocOutputBuffer(int minSize)
    {
        byte[] buf = outputBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_OUTPUT_BUFFER)];
        }
        else {
            outputBuffer = null;
        }
        return buf;
    }

    public void releaseOutputBuffer(byte[] buffer)
    {
        if (outputBuffer == null || (buffer != null && buffer.length > outputBuffer.length)) {
            outputBuffer = buffer;
        }
    }

    public short[] allocEncodingHash(int suggestedSize)
    {
        short[] buf = encodingHash;
        if (buf == null || buf.length < suggestedSize) {
            buf = new short[suggestedSize];
        }
        else {
            encodingHash = null;
        }
        return buf;
    }

    public void releaseEncodingHash(short[] buffer)
    {
        if (encodingHash == null || (buffer != null && buffer.length > encodingHash.length)) {
            encodingHash = buffer;
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // Buffers for decoding (input)
    ///////////////////////////////////////////////////////////////////////

    public byte[] allocInputBuffer(int minSize)
    {
        byte[] buf = inputBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_OUTPUT_BUFFER)];
        }
        else {
            inputBuffer = null;
        }
        return buf;
    }

    public void releaseInputBuffer(byte[] buffer)
    {
        if (inputBuffer == null || (buffer != null && buffer.length > inputBuffer.length)) {
            inputBuffer = buffer;
        }
    }

    public byte[] allocDecodeBuffer(int size)
    {
        byte[] buf = decodingBuffer;
        if (buf == null || buf.length < size) {
            buf = new byte[size];
        }
        else {
            decodingBuffer = null;
        }
        return buf;
    }

    public void releaseDecodeBuffer(byte[] buffer)
    {
        if (decodingBuffer == null || (buffer != null && buffer.length > decodingBuffer.length)) {
            decodingBuffer = buffer;
        }
    }
}
