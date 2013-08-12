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

import static com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.Snappy.COPY_1_BYTE_OFFSET;
import static com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.Snappy.COPY_2_BYTE_OFFSET;
import static com.alibaba.otter.node.etl.common.io.compress.impl.snappy.internal.Snappy.LITERAL;

import java.nio.ByteOrder;
import java.util.Arrays;

final class SnappyCompressor {
    private static final boolean NATIVE_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    // *** DO NOT CHANGE THE VALUE OF kBlockSize ***
    //
    // New Compression code chops up the input into blocks of at most
    // the following size.  This ensures that back-references in the
    // output never cross kBlockSize block boundaries.  This can be
    // helpful in implementing blocked decompression.  However the
    // decompression code should not rely on this guarantee since older
    // compression code may not obey it.
    private static final int     BLOCK_LOG            = 15;
    private static final int     BLOCK_SIZE           = 1 << BLOCK_LOG;

    private static final int     INPUT_MARGIN_BYTES   = 15;

    private static final int     MAX_HASH_TABLE_BITS  = 14;
    private static final int     MAX_HASH_TABLE_SIZE  = 1 << MAX_HASH_TABLE_BITS;

    public static int maxCompressedLength(int sourceLength) {
        // Compressed data can be defined as:
        //    compressed := item* literal*
        //    item       := literal* copy
        //
        // The trailing literal sequence has a space blowup of at most 62/60
        // since a literal of length 60 needs one tag byte + one extra byte
        // for length information.
        //
        // Item blowup is trickier to measure.  Suppose the "copy" op copies
        // 4 bytes of data.  Because of a special check in the encoding code,
        // we produce a 4-byte copy only if the offset is < 65536.  Therefore
        // the copy op takes 3 bytes to encode, and this type of item leads
        // to at most the 62/60 blowup for representing literals.
        //
        // Suppose the "copy" op copies 5 bytes of data.  If the offset is big
        // enough, it will take 5 bytes to encode the copy op.  Therefore the
        // worst case here is a one-byte literal followed by a five-byte copy.
        // I.e., 6 bytes of input turn into 7 bytes of "compressed" data.
        //
        // This last factor dominates the blowup, so the final estimate is:
        return 32 + sourceLength + sourceLength / 6;
    }

    public static int compress(final byte[] uncompressed, final int uncompressedOffset,
                               final int uncompressedLength, final byte[] compressed,
                               final int compressedOffset) {
        // First write the uncompressed size to the output as a variable length int
        int compressedIndex = writeUncompressedLength(compressed, compressedOffset,
                uncompressedLength);

        int hashTableSize = getHashTableSize(uncompressedLength);
        BufferRecycler recycler = BufferRecycler.instance();
        short[] table = recycler.allocEncodingHash(hashTableSize);

        for (int read = 0; read < uncompressedLength; read += BLOCK_SIZE) {
            // Get encoding table for compression
            Arrays.fill(table, (short) 0);

            compressedIndex = compressFragment(uncompressed, uncompressedOffset + read, Math.min(
                    uncompressedLength - read, BLOCK_SIZE), compressed, compressedIndex, table);
        }

        recycler.releaseEncodingHash(table);

        return compressedIndex - compressedOffset;
    }

    private static int compressFragment(final byte[] input, final int inputOffset,
                                        final int inputSize, final byte[] output, int outputIndex,
                                        final short[] table) {
        int ipIndex = inputOffset;
        assert inputSize <= BLOCK_SIZE;
        final int ipEndIndex = inputOffset + inputSize;

        int hashTableSize = getHashTableSize(inputSize);
        // todo given that hashTableSize is required to be a power of 2, this is overly complex
        final int shift = 32 - log2Floor(hashTableSize);
        assert (hashTableSize & (hashTableSize - 1)) == 0 : "table must be power of two";
        assert 0xFFFFFFFF >>> shift == hashTableSize - 1;

        // Bytes in [nextEmitIndex, ipIndex) will be emitted as literal bytes.  Or
        // [nextEmitIndex, ipEndIndex) after the main loop.
        int nextEmitIndex = ipIndex;

        if (inputSize >= INPUT_MARGIN_BYTES) {
            final int ipLimit = inputOffset + inputSize - INPUT_MARGIN_BYTES;
            while (ipIndex <= ipLimit) {
                assert nextEmitIndex <= ipIndex;

                // The body of this loop calls EmitLiteral once and then EmitCopy one or
                // more times.  (The exception is that when we're close to exhausting
                // the input we exit and emit a literal.)
                //
                // In the first iteration of this loop we're just starting, so
                // there's nothing to copy, so calling EmitLiteral once is
                // necessary.  And we only start a new iteration when the
                // current iteration has determined that a call to EmitLiteral will
                // precede the next call to EmitCopy (if any).
                //
                // Step 1: Scan forward in the input looking for a 4-byte-long match.
                // If we get close to exhausting the input exit and emit a final literal.
                //
                // Heuristic match skipping: If 32 bytes are scanned with no matches
                // found, start looking only at every other byte. If 32 more bytes are
                // scanned, look at every third byte, etc.. When a match is found,
                // immediately go back to looking at every byte. This is a small loss
                // (~5% performance, ~0.1% density) for compressible data due to more
                // bookkeeping, but for non-compressible data (such as JPEG) it's a huge
                // win since the compressor quickly "realizes" the data is incompressible
                // and doesn't bother looking for matches everywhere.
                //
                // The "skip" variable keeps track of how many bytes there are since the
                // last match; dividing it by 32 (ie. right-shifting by five) gives the
                // number of bytes to move ahead for each iteration.
                int skip = 32;

                int[] candidateResult = findCandidate(input, ipIndex, ipLimit, inputOffset, shift,
                        table, skip);
                ipIndex = candidateResult[0];
                int candidateIndex = candidateResult[1];
                skip = candidateResult[2];
                if (ipIndex + bytesBetweenHashLookups(skip) > ipLimit) {
                    break;
                }

                // Step 2: A 4-byte match has been found.  We'll later see if more
                // than 4 bytes match.  But, prior to the match, input
                // bytes [nextEmit, ip) are unmatched.  Emit them as "literal bytes."
                assert nextEmitIndex + 16 <= ipEndIndex;
                outputIndex = emitLiteral(output, outputIndex, input, nextEmitIndex, ipIndex
                        - nextEmitIndex, true);

                // Step 3: Call EmitCopy, and then see if another EmitCopy could
                // be our next move.  Repeat until we find no match for the
                // input immediately after what was consumed by the last EmitCopy call.
                //
                // If we exit this loop normally then we need to call EmitLiteral next,
                // though we don't yet know how big the literal will be.  We handle that
                // by proceeding to the next iteration of the main loop.  We also can exit
                // this loop via goto if we get close to exhausting the input.
                int[] indexes = emitCopies(input, inputOffset, inputSize, ipIndex, output,
                        outputIndex, table, shift, candidateIndex);
                ipIndex = indexes[0];
                outputIndex = indexes[1];
                nextEmitIndex = ipIndex;
            }
        }

        // goto emitRemainder hack
        if (nextEmitIndex < ipEndIndex) {
            // Emit the remaining bytes as a literal
            outputIndex = emitLiteral(output, outputIndex, input, nextEmitIndex, ipEndIndex
                    - nextEmitIndex, false);
        }
        return outputIndex;
    }

    private static int[] findCandidate(byte[] input, int ipIndex, int ipLimit, int inputOffset,
                                       int shift, short[] table, int skip) {

        int candidateIndex = 0;
        for (ipIndex += 1; ipIndex + bytesBetweenHashLookups(skip) <= ipLimit; ipIndex += bytesBetweenHashLookups(skip++)) {
            // hash the 4 bytes starting at the input pointer
            int currentInt = SnappyInternalUtils.loadInt(input, ipIndex);
            int hash = hashBytes(currentInt, shift);

            // get the position of a 4 bytes sequence with the same hash
            candidateIndex = inputOffset + table[hash];
            assert candidateIndex >= 0;
            assert candidateIndex < ipIndex;

            // update the hash to point to the current position
            table[hash] = (short) (ipIndex - inputOffset);

            // if the 4 byte sequence a the candidate index matches the sequence at the
            // current position, proceed to the next phase
            if (currentInt == SnappyInternalUtils.loadInt(input, candidateIndex)) {
                break;
            }
        }
        return new int[] { ipIndex, candidateIndex, skip };
    }

    private static int bytesBetweenHashLookups(int skip) {
        return (skip >>> 5);
    }

    private static int[] emitCopies(byte[] input, final int inputOffset, final int inputSize,
                                    int ipIndex, byte[] output, int outputIndex, short[] table,
                                    int shift, int candidateIndex) {
        // Step 3: Call EmitCopy, and then see if another EmitCopy could
        // be our next move.  Repeat until we find no match for the
        // input immediately after what was consumed by the last EmitCopy call.
        //
        // If we exit this loop normally then we need to call EmitLiteral next,
        // though we don't yet know how big the literal will be.  We handle that
        // by proceeding to the next iteration of the main loop.  We also can exit
        // this loop via goto if we get close to exhausting the input.
        int inputBytes;
        do {
            // We have a 4-byte match at ip, and no need to emit any
            // "literal bytes" prior to ip.
            int matched = 4 + findMatchLength(input, candidateIndex + 4, input, ipIndex + 4,
                    inputOffset + inputSize);
            int offset = ipIndex - candidateIndex;
            assert SnappyInternalUtils.equals(input, ipIndex, input, candidateIndex, matched);
            ipIndex += matched;

            // emit the copy operation for this chunk
            outputIndex = emitCopy(output, outputIndex, offset, matched);

            // are we done?
            if (ipIndex >= inputOffset + inputSize - INPUT_MARGIN_BYTES) {
                return new int[] { ipIndex, outputIndex };
            }

            // We could immediately start working at ip now, but to improve
            // compression we first update table[Hash(ip - 1, ...)].
            int prevInt;
            if (SnappyInternalUtils.HAS_UNSAFE) {
                long foo = SnappyInternalUtils.loadLong(input, ipIndex - 1);
                prevInt = (int) foo;
                inputBytes = (int) (foo >>> 8);
            } else {
                prevInt = SnappyInternalUtils.loadInt(input, ipIndex - 1);
                inputBytes = SnappyInternalUtils.loadInt(input, ipIndex);
            }

            // add hash starting with previous byte
            int prevHash = hashBytes(prevInt, shift);
            table[prevHash] = (short) (ipIndex - inputOffset - 1);

            // update hash of current byte
            int curHash = hashBytes(inputBytes, shift);

            candidateIndex = inputOffset + table[curHash];
            table[curHash] = (short) (ipIndex - inputOffset);

        } while (inputBytes == SnappyInternalUtils.loadInt(input, candidateIndex));
        return new int[] { ipIndex, outputIndex };
    }

    private static int emitLiteral(byte[] output, int outputIndex, byte[] literal,
                                   final int literalIndex, final int length,
                                   final boolean allowFastPath) {
        SnappyInternalUtils.checkPositionIndexes(literalIndex, literalIndex + length,
                literal.length);

        int n = length - 1; // Zero-length literals are disallowed
        if (n < 60) {
            // Size fits in tag byte
            output[outputIndex++] = (byte) (LITERAL | n << 2);

            // The vast majority of copies are below 16 bytes, for which a
            // call to memcpy is overkill. This fast path can sometimes
            // copy up to 15 bytes too much, but that is okay in the
            // main loop, since we have a bit to go on for both sides:
            //
            //   - The input will always have kInputMarginBytes = 15 extra
            //     available bytes, as long as we're in the main loop, and
            //     if not, allowFastPath = false.
            //   - The output will always have 32 spare bytes (see
            //     MaxCompressedLength).
            if (allowFastPath && length <= 16) {
                SnappyInternalUtils.copyLong(literal, literalIndex, output, outputIndex);
                SnappyInternalUtils.copyLong(literal, literalIndex + 8, output, outputIndex + 8);
                outputIndex += length;
                return outputIndex;
            }
        } else if (n < (1 << 8)) {
            output[outputIndex++] = (byte) (LITERAL | 59 + 1 << 2);
            output[outputIndex++] = (byte) (n);
        } else if (n < (1 << 16)) {
            output[outputIndex++] = (byte) (LITERAL | 59 + 2 << 2);
            output[outputIndex++] = (byte) (n);
            output[outputIndex++] = (byte) (n >>> 8);
        } else if (n < (1 << 24)) {
            output[outputIndex++] = (byte) (LITERAL | 59 + 3 << 2);
            output[outputIndex++] = (byte) (n);
            output[outputIndex++] = (byte) (n >>> 8);
            output[outputIndex++] = (byte) (n >>> 16);
        } else {
            output[outputIndex++] = (byte) (LITERAL | 59 + 4 << 2);
            output[outputIndex++] = (byte) (n);
            output[outputIndex++] = (byte) (n >>> 8);
            output[outputIndex++] = (byte) (n >>> 16);
            output[outputIndex++] = (byte) (n >>> 24);
        }

        SnappyInternalUtils.checkPositionIndexes(literalIndex, literalIndex + length,
                literal.length);

        System.arraycopy(literal, literalIndex, output, outputIndex, length);
        outputIndex += length;
        return outputIndex;
    }

    private static int emitCopyLessThan64(byte[] output, int outputIndex, int offset, int length) {
        assert offset >= 0;
        assert length <= 64;
        assert length >= 4;
        assert offset < 65536;

        if ((length < 12) && (offset < 2048)) {
            int lenMinus4 = length - 4;
            assert (lenMinus4 < 8); // Must fit in 3 bits
            output[outputIndex++] = (byte) (COPY_1_BYTE_OFFSET | ((lenMinus4) << 2) | ((offset >>> 8) << 5));
            output[outputIndex++] = (byte) (offset);
        } else {
            output[outputIndex++] = (byte) (COPY_2_BYTE_OFFSET | ((length - 1) << 2));
            output[outputIndex++] = (byte) (offset);
            output[outputIndex++] = (byte) (offset >>> 8);
        }
        return outputIndex;
    }

    private static int emitCopy(byte[] output, int outputIndex, int offset, int length) {
        // Emit 64 byte copies but make sure to keep at least four bytes reserved
        while (length >= 68) {
            outputIndex = emitCopyLessThan64(output, outputIndex, offset, 64);
            length -= 64;
        }

        // Emit an extra 60 byte copy if have too much data to fit in one copy
        if (length > 64) {
            outputIndex = emitCopyLessThan64(output, outputIndex, offset, 60);
            length -= 60;
        }

        // Emit remainder
        outputIndex = emitCopyLessThan64(output, outputIndex, offset, length);
        return outputIndex;
    }

    private static int findMatchLength(byte[] s1, int s1Index, byte[] s2, final int s2Index,
                                       int s2Limit) {
        assert (s2Limit >= s2Index);

        if (SnappyInternalUtils.HAS_UNSAFE) {
            int matched = 0;

            while (s2Index + matched <= s2Limit - 4
                    && SnappyInternalUtils.loadInt(s2, s2Index + matched) == SnappyInternalUtils
                            .loadInt(s1, s1Index + matched)) {
                matched += 4;
            }

            if (NATIVE_LITTLE_ENDIAN && s2Index + matched <= s2Limit - 4) {
                int x = SnappyInternalUtils.loadInt(s2, s2Index + matched)
                        ^ SnappyInternalUtils.loadInt(s1, s1Index + matched);
                int matchingBits = Integer.numberOfTrailingZeros(x);
                matched += matchingBits >> 3;
            } else {
                while (s2Index + matched < s2Limit
                        && s1[s1Index + matched] == s2[s2Index + matched]) {
                    ++matched;
                }
            }
            return matched;
        } else {
            int length = s2Limit - s2Index;
            for (int matched = 0; matched < length; matched++) {
                if (s1[s1Index + matched] != s2[s2Index + matched]) {
                    return matched;
                }
            }
            return length;
        }
    }

    private static int getHashTableSize(int inputSize) {
        // Use smaller hash table when input.size() is smaller, since we
        // fill the table, incurring O(hash table size) overhead for
        // compression, and if the input is short, we won't need that
        // many hash table entries anyway.
        assert (MAX_HASH_TABLE_SIZE >= 256);

        int hashTableSize = 256;
        while (hashTableSize < MAX_HASH_TABLE_SIZE && hashTableSize < inputSize) {
            hashTableSize <<= 1;
        }
        assert 0 == (hashTableSize & (hashTableSize - 1)) : "hash must be power of two";
        assert hashTableSize <= MAX_HASH_TABLE_SIZE : "hash table too large";
        return hashTableSize;

        //        // todo should be faster but is not
        //        int newHashTableSize;
        //        if (inputSize < 256) {
        //            newHashTableSize = 256;
        //        } else if (inputSize > kMaxHashTableSize) {
        //            newHashTableSize = kMaxHashTableSize;
        //        } else {
        //            int leadingZeros = Integer.numberOfLeadingZeros(inputSize - 1);
        //            newHashTableSize = 1 << (32 - leadingZeros);
        //        }
        //
        //        assert 0 == (newHashTableSize & (newHashTableSize - 1)) : "hash must be power of two";
        //        assert newHashTableSize <= kMaxHashTableSize : "hash table too large";
        //        return newHashTableSize;
    }

    // Any hash function will produce a valid compressed bitstream, but a good
    // hash function reduces the number of collisions and thus yields better
    // compression for compressible input, and more speed for incompressible
    // input. Of course, it doesn't hurt if the hash function is reasonably fast
    // either, as it gets called a lot.
    private static int hashBytes(int bytes, int shift) {
        int kMul = 0x1e35a7bd;
        return (bytes * kMul) >>> shift;
    }

    private static int log2Floor(int n) {
        return n == 0 ? -1 : 31 ^ Integer.numberOfLeadingZeros(n);
    }

    /**
     * Writes the uncompressed length as variable length integer.
     */
    private static int writeUncompressedLength(byte[] compressed, int compressedOffset,
                                               int uncompressedLength) {
        int highBitMask = 0x80;
        if (uncompressedLength < (1 << 7) && uncompressedLength >= 0) {
            compressed[compressedOffset++] = (byte) (uncompressedLength);
        } else if (uncompressedLength < (1 << 14) && uncompressedLength > 0) {
            compressed[compressedOffset++] = (byte) (uncompressedLength | highBitMask);
            compressed[compressedOffset++] = (byte) (uncompressedLength >>> 7);
        } else if (uncompressedLength < (1 << 21) && uncompressedLength > 0) {
            compressed[compressedOffset++] = (byte) (uncompressedLength | highBitMask);
            compressed[compressedOffset++] = (byte) ((uncompressedLength >>> 7) | highBitMask);
            compressed[compressedOffset++] = (byte) (uncompressedLength >>> 14);
        } else if (uncompressedLength < (1 << 28) && uncompressedLength > 0) {
            compressed[compressedOffset++] = (byte) (uncompressedLength | highBitMask);
            compressed[compressedOffset++] = (byte) ((uncompressedLength >>> 7) | highBitMask);
            compressed[compressedOffset++] = (byte) ((uncompressedLength >>> 14) | highBitMask);
            compressed[compressedOffset++] = (byte) (uncompressedLength >>> 21);
        } else {
            compressed[compressedOffset++] = (byte) (uncompressedLength | highBitMask);
            compressed[compressedOffset++] = (byte) ((uncompressedLength >>> 7) | highBitMask);
            compressed[compressedOffset++] = (byte) ((uncompressedLength >>> 14) | highBitMask);
            compressed[compressedOffset++] = (byte) ((uncompressedLength >>> 21) | highBitMask);
            compressed[compressedOffset++] = (byte) (uncompressedLength >>> 28);
        }
        return compressedOffset;
    }
}
