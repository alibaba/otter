package com.alibaba.otter.manager.web.common.model;

import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * @author simon 2011-12-8 下午09:26:01
 */
public class SeniorNode extends Node {

    private static final long serialVersionUID = -4121314684324595191L;
    private boolean           used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}
