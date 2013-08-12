package com.alibaba.otter.manager.web.common.model;

import com.alibaba.otter.canal.instance.manager.model.Canal;

/**
 * @author sarah.lij 2012-7-24 下午01:31:06
 */
public class SeniorCanal extends Canal {

    private static final long serialVersionUID = -4121314684324595191L;
    private boolean           used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}
