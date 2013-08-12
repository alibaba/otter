package com.alibaba.otter.node.etl.common.io.download;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 * @author brave.taoy
 */
public interface Download {

    public long getContentLength();

    public File getAssociatedLocalFile();

    public byte[] getAssociatedMemoryData();
}
