package com.alibaba.otter.common.push.supplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author zebin.xuzb 2013-1-23 下午4:45:44
 * @since 4.1.3
 */
public class HaDatasourceInfo {

    private DatasourceInfo       master;
    private List<DatasourceInfo> slavers = new ArrayList<DatasourceInfo>();

    public DatasourceInfo getMaster() {
        return master;
    }

    public void setMaster(DatasourceInfo master) {
        this.master = master;
    }

    public List<DatasourceInfo> getSlavers() {
        return slavers;
    }

    public void addSlaver(DatasourceInfo slaver) {
        this.slavers.add(slaver);
    }

    public void addSlavers(Collection<DatasourceInfo> slavers) {
        this.slavers.addAll(slavers);
    }
}
