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

package com.alibaba.otter.shared.etl.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据记录集合对象
 * 
 * @author jianghang 2012-10-31 下午05:51:42
 * @version 4.1.2
 */
public class RowBatch extends BatchObject<EventData> {

    private static final long serialVersionUID = -6117067964148581257L;

    private List<EventData>   datas            = new LinkedList<EventData>();

    public List<EventData> getDatas() {
        return datas;
    }

    public void setDatas(List<EventData> datas) {
        this.datas = datas;
    }

    public void merge(EventData data) {
        this.datas.add(data);
    }

}
