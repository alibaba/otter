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

interface Memory
{
    boolean fastAccessSupported();

    int lookupShort(short[] data, int index);

    int loadByte(byte[] data, int index);

    int loadInt(byte[] data, int index);

    void copyLong(byte[] src, int srcIndex, byte[] dest, int destIndex);

    long loadLong(byte[] data, int index);

    void copyMemory(byte[] input, int inputIndex, byte[] output, int outputIndex, int length);
}
