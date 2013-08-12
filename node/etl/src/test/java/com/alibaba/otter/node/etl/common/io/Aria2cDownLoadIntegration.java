package com.alibaba.otter.node.etl.common.io;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.io.download.DataRetriever;
import com.alibaba.otter.node.etl.common.io.download.exception.DataRetrieveException;
import com.alibaba.otter.node.etl.common.io.download.impl.aria2c.Aria2cRetriever;
import com.alibaba.otter.node.etl.BaseOtterTest;

/**
 * @author jianghang 2011-10-10 下午06:23:33
 * @version 4.0.0
 */
public class Aria2cDownLoadIntegration extends BaseOtterTest {

    private static final String tmp = System.getProperty("java.io.tmpdir", "/tmp");

    @Test
    public void testDownLoad_ok() {
        DataRetriever retriever = new Aria2cRetriever("http://china.alibaba.com", tmp);
        try {
            retriever.connect();
            retriever.doRetrieve();
        } catch (DataRetrieveException ex) {
            retriever.abort();
        } finally {
            retriever.disconnect();
        }
    }

    @Test
    public void testDownLoad_failed() {
        DataRetriever retriever = new Aria2cRetriever("aaaaaaa/sssss", tmp);
        try {
            retriever.connect();
            retriever.doRetrieve();
        } catch (DataRetrieveException ex) {
            retriever.abort();
        } finally {
            retriever.disconnect();
        }
    }
}
