/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
