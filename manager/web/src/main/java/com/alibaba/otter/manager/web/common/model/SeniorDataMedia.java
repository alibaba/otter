package com.alibaba.otter.manager.web.common.model;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

/**
 * @author simon 2011-12-9 下午03:17:39
 */
public class SeniorDataMedia extends DataMedia<DataMediaSource> {

    private static final long   serialVersionUID = 1089669449690478640L;

    private boolean             used;

    private List<DataMediaPair> pairs;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public List<DataMediaPair> getPairs() {
        return pairs;
    }

    public void setPairs(List<DataMediaPair> pairs) {
        this.pairs = pairs;
    }

}
