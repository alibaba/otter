package com.alibaba.otter.node.etl.common.pipe.impl.http.archive;

import java.io.InputStream;

/**
 * archive数据提取的callback接口
 * 
 * @author jianghang 2011-10-11 下午04:49:05
 * @version 4.0.0
 */
public interface ArchiveRetriverCallback<SOURCE> {

    /**
     * 根据source，打开对应的输入流
     */
    public InputStream retrive(SOURCE source);

}
