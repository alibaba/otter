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

package com.alibaba.otter.node.etl.common.io.compress.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super object for Compressor and Archiver classes.
 */
public abstract class PackableObject {

    /* Type for archive choosing: String */
    protected static final int CHOOSE_EXTENSION = 1;

    /* Type for archive choosing: Long */
    protected static final int CHOOSE_NAME      = 2;

    protected final Logger     logger           = LoggerFactory.getLogger(getClass());

    /**
     * Header byte array for this archive.
     */
    public abstract byte[] getHeader();

    /**
     * Returns the default FileExtension for this archive, for example "zip",
     * "tar"...
     */
    public abstract String getDefaultFileExtension();

    /**
     * Returns the ArchiveName for this archive.
     */
    public abstract String getName();

    /**
     * String Chooser.
     */
    protected boolean isPackableWith(Object value, int choose) {
        if (value == null) {
            return false;
        }

        if (choose == CHOOSE_EXTENSION) {
            if (value.equals(getDefaultFileExtension())) {
                return true;
            }
        } else if (choose == CHOOSE_NAME) {
            if (value.equals(getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compares a file to a list of packables and identifies an object by
     * header. If no matching header is found, it identifies the file by file
     * extension. If identification was not successfull, null is returned
     * 
     * @param file the file to identify
     * @param packables a list of packables
     * @return a matching packable object, or null
     * @throws IOException
     */
    public static PackableObject identifyByHeader(File file, List packables) throws IOException {
        FileInputStream fis = null;
        try {
            /* Archive result */
            // PackableObject packable = null;
            // identify archive by header
            fis = new FileInputStream(file);
            byte[] headerBytes = new byte[20];
            fis.read(headerBytes, 0, 20);

            Iterator iter = packables.iterator();
            while (iter.hasNext()) {
                PackableObject p = (PackableObject) iter.next();
                byte[] fieldHeader = p.getHeader();

                if (fieldHeader != null) {
                    if (compareByteArrays(headerBytes, fieldHeader)) {
                        return p;
                    }
                }
            }

            // if we couldn't find an archiver by header bytes, we'll give it a
            // try
            // with the default name extension. This is useful, cause some
            // archives
            // like tar have no header.
            String name = file.getName();
            String extension = null;
            String[] s = name.split("\\.");

            if (s.length > 1) {
                extension = s[s.length - 1];
            }

            Iterator it = packables.iterator();

            while (it.hasNext()) {
                PackableObject p = (PackableObject) it.next();
                if (p.isPackableWith(extension, PackableObject.CHOOSE_EXTENSION)) {
                    return p;
                }
            }
            // No implementation found
            return null;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private static boolean compareByteArrays(byte[] source, byte[] match) {
        int i = 0;
        while ((source.length < i) || (i < match.length)) {
            if (source[i] != match[i]) {
                return false;
            }
            i++;
        }
        return true;
    }
}
