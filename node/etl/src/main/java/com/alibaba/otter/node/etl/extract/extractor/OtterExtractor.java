package com.alibaba.otter.node.etl.extract.extractor;

import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;

/**
 * 组装数据,有多种来源，mysql,oracle,store,file等.
 */
public interface OtterExtractor<P> {

    /**
     * 数据装配
     */
    void extract(P param) throws ExtractException;
}
