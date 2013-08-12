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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Main
{
    public static void main(String[] args)
            throws Exception
    {
        if ((args.length == 1) && (args[0].equals("-c"))) {
            compress();
        }
        else if ((args.length == 1) && (args[0].equals("-d"))) {
            uncompress();
        }
        else {
            usage();
        }
    }

    private static void usage()
    {
        System.err.println("Usage: java -jar snappy.jar OPTION");
        System.err.println("Compress or uncompress with Snappy.");
        System.err.println();
        System.err.println("  -c     compress from stdin to stdout");
        System.err.println("  -d     uncompress from stdin to stdout");
        System.exit(100);
    }

    private static void compress()
            throws IOException
    {
        copy(System.in, new SnappyOutputStream(System.out));
    }

    private static void uncompress()
            throws IOException
    {
        copy(new SnappyInputStream(System.in), System.out);
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException
    {
        byte[] buf = new byte[4096];
        while (true) {
            int r = in.read(buf);
            if (r == -1) {
                out.close();
                in.close();
                return;
            }
            out.write(buf, 0, r);
        }
    }
}
