package com.alibaba.otter.manager.web.common.model;

import com.alibaba.otter.shared.common.model.config.data.DataMatrix;

public class SeniorDataMatrix extends DataMatrix {

    private static final long serialVersionUID = -4121314684324595191L;
    private boolean           used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}
