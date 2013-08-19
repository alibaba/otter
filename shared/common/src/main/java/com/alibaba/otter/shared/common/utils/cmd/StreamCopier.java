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
