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

package com.alibaba.otter.node.etl.load.loader;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 类LoadContext.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-7-3 下午2:22:51
 * @version 4.1.0
 */
public abstract class AbstractLoadContext<T> implements LoadContext, Serializable {

    private static final long serialVersionUID = -2052280419851872736L;
    private Identity          identity;
    private Channel           channel;
    private Pipeline          pipeline;
    protected List<T>         prepareDatas;                            // 准备处理的数据
    protected List<T>         processedDatas;                          // 已处理完成的数据
    protected List<T>         failedDatas;

    public AbstractLoadContext(){
        this.prepareDatas = Collections.synchronizedList(new LinkedList<T>());
        this.processedDatas = Collections.synchronizedList(new LinkedList<T>());
        this.failedDatas = Collections.synchronizedList(new LinkedList<T>());
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public List<T> getPrepareDatas() {
        return prepareDatas;
    }

    public void setPrepareDatas(List<T> prepareDatas) {
        this.prepareDatas = prepareDatas;
    }

    public void addProcessData(T processData) {
        this.processedDatas.add(processData);
    }

    public List<T> getProcessedDatas() {
        return processedDatas;
    }

    public void setProcessedDatas(List<T> processedDatas) {
        this.processedDatas = processedDatas;
    }

    public List<T> getFailedDatas() {
        return failedDatas;
    }

    public void addFailedData(T failedData) {
        this.failedDatas.add(failedData);
    }

    public void setFailedDatas(List<T> failedDatas) {
        this.failedDatas = failedDatas;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
