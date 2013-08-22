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

package com.alibaba.otter.node.extend.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 中美同步，支付宝的会员数据不同同步到美国
 * 
 * @author jianghang 2012-8-9 下午03:34:34
 * @version 4.1.0
 */
public class HavanaMemberProcessor extends AbstractEventProcessor {

    public boolean process(EventData eventData) {
        EventColumn belong = getColumn(eventData, "belong_to");
        if (belong == null) {
            return doProcess(eventData);
        }

        // 判断规则：
        // (belong_to字段是包含4: || visited_idcs是数字并且visited_idcs & 2 >0) &&
        // write_source不为空且write_source不包含"OT"字符串且不包含"CIW"字符串)

        boolean pass = false;
        pass |= StringUtils.contains(belong.getColumnValue(), "4:");

        if (!pass) {// 第一个条件不满足，看一下第二个条件，两者是或者关系
            EventColumn visited = getColumn(eventData, "visited_idcs");
            if (visited == null) {
                return doProcess(eventData);
            }

            pass |= NumberUtils.isDigits(visited.getColumnValue())
                    && (Integer.valueOf(visited.getColumnValue()) & 2) > 0;
        }

        if (pass) {// 第三个条件和前两个条件是与关系，delete类型不判断write_source
            // write_source为字符类型
            EventColumn writeSource = getColumn(eventData, "write_source");
            if (writeSource == null) {
                return doProcess(eventData);
            } else {
                // 继续与操作
                if (eventData.getEventType().isDelete() // delete类型直接放过
                    || (StringUtils.isNotEmpty(writeSource.getColumnValue()) && !StringUtils.contains(writeSource.getColumnValue(),
                                                                                                      "CIW"))) {
                    return doProcess(eventData);
                }
            }
        }

        return false;
    }

    private boolean doProcess(EventData eventData) {
        if (!eventData.getEventType().isDelete()) {
            // 缺省值为“OT:时间戳:随机串”.
            EventColumn writeSource = getColumn(eventData, "write_source");
            if (writeSource != null) {
                // 填入otter产生的信息
                writeSource.setColumnValue("OT:" + System.currentTimeMillis() + ":" + RandomUtils.nextInt(10000));
            }
        }
        return true;
    }
}
