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

package com.alibaba.otter.shared.arbitrate.impl.setl.helper;

import java.util.Comparator;

import com.alibaba.otter.shared.arbitrate.model.RemedyIndexEventData;

/**
 * Remedy 排序，根据processId (process和startTime/endTime一定保持一致的排序性）
 * 
 * @author jianghang 2012-4-13 下午02:20:35
 * @version 4.0.2
 */
public class RemedyIndexComparator implements Comparator<RemedyIndexEventData> {

    public int compare(RemedyIndexEventData index1, RemedyIndexEventData index2) {
        return index1.getProcessId().compareTo(index2.getProcessId());
    }
}
