package com.alibaba.otter.node.etl.transform.transformer;

import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 数据转换过程中的上下文
 * 
 * @author jianghang 2011-10-27 下午05:12:53
 * @version 4.0.0
 */
public class OtterTransformerContext {

    private Identity      identity;
    private Pipeline      pipeline;
    private DataMediaPair dataMediaPair;

    public OtterTransformerContext(Identity identity, DataMediaPair dataMediaPair, Pipeline pipeline){
        this.identity = identity;
        this.dataMediaPair = dataMediaPair;
        this.pipeline = pipeline;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public DataMediaPair getDataMediaPair() {
        return dataMediaPair;
    }

    public void setDataMediaPair(DataMediaPair dataMediaPair) {
        this.dataMediaPair = dataMediaPair;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

}
