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

import java.util.Comparator;

/**
 * 按照EventColumn的index进行排序.
 * 
 * @author xiaoqing.zhouxq 2012-3-8 上午11:38:25
 */
public class EventColumnIndexComparable implements Comparator<EventColumn> {

    public int compare(EventColumn o1, EventColumn o2) {
        if (o1.getIndex() < o2.getIndex()) {
            return -1;
        } else if (o1.getIndex() == o2.getIndex()) {
            return 0;
        } else {
            return 1;
        }
    }

}
