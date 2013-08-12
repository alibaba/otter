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

final class SnappyInternalUtils {
    private SnappyInternalUtils() {
    }

    private static final Memory memory;

    static {
        // Try to only load one implementation of Memory to assure the call sites are monomorphic (fast)
        Memory memoryInstance = null;
        //                try {
        //                    Class<? extends Memory> unsafeMemoryClass = SnappyInternalUtils.class.getClassLoader().loadClass("com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.UnsafeMemory").asSubclass(Memory.class);
        //                    Memory unsafeMemory = unsafeMemoryClass.newInstance();
        //                    if (unsafeMemory.loadInt(new byte[4], 0) == 0) {
        //                        memoryInstance = unsafeMemory;
        //                    }
        //                }
        //                catch (Throwable ignored) {
        //                }
        if (memoryInstance == null) {
            try {
                Class<? extends Memory> slowMemoryClass = SnappyInternalUtils.class
                        .getClassLoader()
                        .loadClass(
                                "com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.SlowMemory")
                        .asSubclass(Memory.class);
                Memory slowMemory = slowMemoryClass.newInstance();
                if (slowMemory.loadInt(new byte[4], 0) == 0) {
                    memoryInstance = slowMemory;
                } else {
                    throw new AssertionError("SlowMemory class is broken!");
                }
            } catch (Throwable ignored) {
                throw new AssertionError("Could not find SlowMemory class");
            }
        }
        memory = memoryInstance;
    }

    static final boolean        HAS_UNSAFE = memory.fastAccessSupported();

    static boolean equals(byte[] left, int leftIndex, byte[] right, int rightIndex, int length) {
        checkPositionIndexes(leftIndex, leftIndex + length, left.length);
        checkPositionIndexes(rightIndex, rightIndex + length, right.length);

        for (int i = 0; i < length; i++) {
            if (left[leftIndex + i] != right[rightIndex + i]) {
                return false;
            }
        }
        return true;
    }

    public static int lookupShort(short[] data, int index) {
        return memory.lookupShort(data, index);
    }

    public static int loadByte(byte[] data, int index) {
        return memory.loadByte(data, index);
    }

    static int loadInt(byte[] data, int index) {
        return memory.loadInt(data, index);
    }

    static void copyLong(byte[] src, int srcIndex, byte[] dest, int destIndex) {
        memory.copyLong(src, srcIndex, dest, destIndex);
    }

    static long loadLong(byte[] data, int index) {
        return memory.loadLong(data, index);
    }

    static void copyMemory(byte[] input, int inputIndex, byte[] output, int outputIndex, int length) {
        memory.copyMemory(input, inputIndex, output, outputIndex, length);
    }

    //
    // Copied from Guava Preconditions
    static <T> T checkNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference == null) {
            // If either of these parameters is null, the right thing happens anyway
            throw new NullPointerException(String.format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }

    static void checkArgument(boolean expression, String errorMessageTemplate,
                              Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(String
                    .format(errorMessageTemplate, errorMessageArgs));
        }
    }

    static void checkPositionIndexes(int start, int end, int size) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        // end < start
        return String.format("end index (%s) must not be less than start index (%s)", end, start);
    }

    static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return String.format("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index > size
            return String.format("%s (%s) must not be greater than size (%s)", desc, index, size);
        }
    }
}
