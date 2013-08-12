package com.alibaba.otter.node.etl.load.loader;

/**
 * otter数据loader接口
 * 
 * @author jianghang 2011-10-27 上午11:15:13
 * @version 4.0.0
 */
public interface OtterLoader<P, R> {

    public R load(P data);
}
