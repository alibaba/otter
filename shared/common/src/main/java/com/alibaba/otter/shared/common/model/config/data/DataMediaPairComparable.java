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

package com.alibaba.otter.shared.common.model.config.data;

import java.util.Comparator;

/**
 * 按照源表的dataMeia的mode模式进行排序
 * 
 * @author jianghang 2013-9-13 下午2:10:52
 * @since 4.2.3
 */
public class DataMediaPairComparable implements Comparator<DataMediaPair> {

    public int compare(DataMediaPair o1, DataMediaPair o2) {
        int s1 = o1.getSource().getNamespaceMode().getMode().getValue()
                 + o1.getSource().getNameMode().getMode().getValue();

        int s2 = o2.getSource().getNamespaceMode().getMode().getValue()
                 + o2.getSource().getNameMode().getMode().getValue();

        if (s1 < s2) {
            return -1;
        } else if (s1 > s2) {
            return 1;
        } else {
            return o1.getId().compareTo(o2.getId());
        }
    }

}
