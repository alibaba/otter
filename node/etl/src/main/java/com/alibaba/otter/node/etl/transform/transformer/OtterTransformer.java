package com.alibaba.otter.node.etl.transform.transformer;

/**
 * 数据提取过程，T
 * 
 * @author jianghang 2011-10-27 下午04:04:24
 * @version 4.0.0
 */
public interface OtterTransformer<S, T> {

    public S transform(T data, OtterTransformerContext context);
}
