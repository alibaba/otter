package com.alibaba.otter.node.etl.common.pipe;

import com.alibaba.otter.node.etl.common.pipe.exception.PipeException;

/**
 * S.E.T.L模块之间的数据交互工具
 * 
 * @author jianghang 2011-10-10 下午04:48:44
 * @version 4.0.0
 */
public interface Pipe<T, KEY extends PipeKey> {

    /**
     * 向管道中添加数据
     * 
     * @param data
     */
    public KEY put(T data) throws PipeException;

    /**
     * 通过key获取管道中的数据
     * 
     * @param key
     * @return
     */
    public T get(KEY key) throws PipeException;
}
