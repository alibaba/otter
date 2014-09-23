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

package com.alibaba.otter.shared.common.utils.cmd;

//~--- non-JDK imports --------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * A simple thread that copies one stream to another. Useful for copying a process's output/error streams to this
 * process's output/error streams.
 */
public class StreamCopier extends Thread {

    protected final String         identifier;
    protected final OutputStream   out;
    protected final BufferedReader reader;

    public StreamCopier(InputStream stream, OutputStream out){
        this(stream, out, null);
    }

    public StreamCopier(InputStream stream, OutputStream out, String identifier){
        if ((stream == null) || (out == null)) {
            throw new AssertionError("null streams not allowed");
        }

        this.reader = new BufferedReader(new InputStreamReader(stream));
        this.out = out;
        this.identifier = identifier;
        this.setName("Stream Copier");
        this.setDaemon(true);
    }

    public void run() {
        try {
            StringBuffer buf = new StringBuffer();
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (identifier != null) {
                    line = identifier + line;
                }

                if (false == StringUtils.isBlank(line)) {
                    buf.append(line).append(SystemUtils.LINE_SEPARATOR);
                }
            }

            out.write(buf.toString().getBytes());
            out.flush();
        } catch (IOException ioe) {
            //ignore
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
