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
