package com.alibaba.otter.shared.etl.extend.fileresolver.support;

import java.io.IOException;

/**
 * @author zebin.xuzb 2013-2-25 上午10:51:14
 * @since 4.1.7
 */
public interface RemoteDirectory {

    public String getPath();

    /**
     * 删除目录
     * 
     * @return
     * @throws IOException
     */
    public boolean delete() throws IOException;

    /**
     * 判断目录是否存在。
     * 
     * @return 如果目录存在返回true，否则false
     */
    public boolean exists();

    /**
     * 返回当前目录下的文件列表
     * 
     * @return
     * @throws IOException
     */
    public String[] listFiles() throws IOException;
}
