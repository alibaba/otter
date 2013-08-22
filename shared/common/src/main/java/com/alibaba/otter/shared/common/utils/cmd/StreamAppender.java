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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;

public class StreamAppender {

    private Thread      errWriter;
    private Thread      outWriter;
    private PrintWriter output;

    public StreamAppender(OutputStream output){
        this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
    }

    public void writeInput(final InputStream err, final InputStream out) {
        errWriter = new Thread() {

            BufferedReader reader = new BufferedReader(new InputStreamReader(err));

            public void run() {
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        output.println(line);
                    }
                } catch (IOException e) {
                    //ignore
                } finally {
                    output.flush();// 关闭之前flush一下
                    IOUtils.closeQuietly(reader);
                }
            }
        };
        outWriter = new Thread() {

            BufferedReader reader = new BufferedReader(new InputStreamReader(out));

            public void run() {
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        output.println(line);
                    }
                } catch (IOException e) {
                    //ignore
                } finally {
                    output.flush();// 关闭之前flush一下
                    IOUtils.closeQuietly(reader);
                }
            }
        };
        errWriter.setDaemon(true);
        outWriter.setDaemon(true);
        errWriter.start();
        outWriter.start();
    }

    public void finish() throws Exception {
        outWriter.join();
        errWriter.join();
    }
}
