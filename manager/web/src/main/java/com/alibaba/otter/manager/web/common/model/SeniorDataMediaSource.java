package com.alibaba.otter.manager.web.common.model;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;

/**
 * @author simon 2011-12-9 下午03:17:39
 */
public class SeniorDataMediaSource extends DbMediaSource {

    private static final long serialVersionUID = 3876613625471584350L;
    private boolean           used;
    private String            storePath;
    private List<DataMedia>   dataMedias;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public List<DataMedia> getDataMedias() {
        return dataMedias;
    }

    public void setDataMedias(List<DataMedia> dataMedias) {
        this.dataMedias = dataMedias;
    }

}
