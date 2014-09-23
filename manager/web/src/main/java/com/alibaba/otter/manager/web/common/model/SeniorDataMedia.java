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
