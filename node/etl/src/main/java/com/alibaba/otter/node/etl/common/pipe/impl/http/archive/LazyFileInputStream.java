package com.alibaba.otter.node.etl.common.pipe.impl.http.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 基于lazy实现的InputSteam
 * 
 * @author jianghang 2013-3-6 下午02:24:30
 * @version 4.1.7
 */
public class LazyFileInputStream extends InputStream {

    private InputStream delegate;
    private File        file;

    public LazyFileInputStream(File file){
        this.file = file;
    }

    public InputStream getInputSteam() throws FileNotFoundException {
        delegate = new FileInputStream(file);
        return delegate;
    }

    public void close() throws IOException {
        if (delegate != null) {
            delegate.close();
        }
    }

    public int read() throws IOException {
        return 0;
    }
}
