package com.alibaba.otter.node.extend.fileresolver;

import com.alibaba.otter.shared.etl.extend.fileresolver.FileResolver;

/**
 * @author jianghang 2012-10-23 下午04:11:14
 * @version 4.1.0
 */
public abstract class AbstractFileResolver implements FileResolver {

    public boolean isDeleteRequired() {
        return false;
    }

    public boolean isDistributed() {
        return false;
    }
}
