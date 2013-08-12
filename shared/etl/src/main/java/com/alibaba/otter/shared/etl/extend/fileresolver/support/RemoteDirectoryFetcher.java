package com.alibaba.otter.shared.etl.extend.fileresolver.support;

/**
 * @author zebin.xuzb 2013-2-25 上午10:49:50
 * @since 4.1.7
 */
public interface RemoteDirectoryFetcher {

    /**
     * 获取 RemoteDirectory， 可能为 null!
     * 
     * @param namespace
     * @param path
     * @return
     */
    RemoteDirectory fetch(String namespace, String path);
}
