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

package com.alibaba.otter.node.etl.load.loader.db.context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 数据库load处理上下文
 * 
 * @author jianghang 2011-11-1 上午11:22:45
 * @version 4.0.0
 */
public class DbLoadContext extends AbstractLoadContext<EventData> {

    private static final long serialVersionUID = -4851977997313104740L;
    private List<EventData>   lastProcessedDatas;                      // 上一轮的已录入的记录，可能会有多次失败需要合并多次已录入的数据
    private DataMediaSource   dataMediaSource;

    public DbLoadContext(){
        lastProcessedDatas = Collections.synchronizedList(new LinkedList<EventData>());
        prepareDatas = Collections.synchronizedList(new LinkedList<EventData>());
        processedDatas = Collections.synchronizedList(new LinkedList<EventData>());
        failedDatas = Collections.synchronizedList(new LinkedList<EventData>());
    }

    public List<EventData> getLastProcessedDatas() {
        return lastProcessedDatas;
    }

    public void setLastProcessedDatas(List<EventData> lastProcessedDatas) {
        this.lastProcessedDatas = lastProcessedDatas;
    }

    public DataMediaSource getDataMediaSource() {
        return dataMediaSource;
    }

    public void setDataMediaSource(DataMediaSource dataMediaSource) {
        this.dataMediaSource = dataMediaSource;
    }

}
