package com.alibaba.otter.node.etl.common.io.download.impl.aria2c;

import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandRetriever;

public class Aria2cRetriever extends AbstractCommandRetriever {

    public static final String NAME = "aria2c";

    public Aria2cRetriever(String url, String targetDir){
        super(NAME, url, targetDir);
    }

    public Aria2cRetriever(String cmdPath, String url, String targetDir){
        super(cmdPath, url, targetDir);
    }

    public Aria2cRetriever(String cmdPath, String url, String targetDir, String[] params){
        super(cmdPath, url, targetDir, params);
    }

    @Override
    protected void buildDownload(String cmdPath, String url, String targetDir, String[] params) {
        download = new Aria2cDownload(cmdPath, url, targetDir, params);
    }

}
