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

class SlowMemory implements Memory
{
    @Override
    public boolean fastAccessSupported()
    {
        return false;
    }

    @Override
    public int lookupShort(short[] data, int index)
    {
        return data[index] & 0xFFFF;
    }

    @Override
    public int loadByte(byte[] data, int index)
    {
        return data[index] & 0xFF;
    }

    @Override
    public int loadInt(byte[] data, int index)
    {
        return (data[index] & 0xff) |
                (data[index + 1] & 0xff) << 8 |
                (data[index + 2] & 0xff) << 16 |
                (data[index + 3] & 0xff) << 24;
    }

    @Override
    public void copyLong(byte[] src, int srcIndex, byte[] dest, int destIndex)
    {
        for (int i = 0; i < 8; i++) {
            dest[destIndex + i] = src[srcIndex + i];
        }
    }

    @Override
    public long loadLong(byte[] data, int index)
    {
        return (data[index] & 0xffL) |
                (data[index + 1] & 0xffL) << 8 |
                (data[index + 2] & 0xffL) << 16 |
                (data[index + 3] & 0xffL) << 24 |
                (data[index + 4] & 0xffL) << 32 |
                (data[index + 5] & 0xffL) << 40 |
                (data[index + 6] & 0xffL) << 48 |
                (data[index + 7] & 0xffL) << 56;
    }

    @Override
    public void copyMemory(byte[] input, int inputIndex, byte[] output, int outputIndex, int length)
    {
        System.arraycopy(input, inputIndex, output, outputIndex, length);
    }
}
