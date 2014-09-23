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

package com.alibaba.otter.shared.arbitrate.impl;

import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.impl.setl.ExtractArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.LoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.MainStemArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.SelectArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.TerminArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.ToolArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.TransformArbitrateEvent;

/**
 * 仲裁器事件service的默认实现
 * 
 * @author jianghang 2011-9-22 下午04:04:00
 * @version 4.0.0
 */
public class ArbitrateEventServiceImpl implements ArbitrateEventService {

    private MainStemArbitrateEvent  mainStemEvent;
    private SelectArbitrateEvent    selectEvent;
    private ExtractArbitrateEvent   extractEvent;
    private TransformArbitrateEvent transformEvent;
    private LoadArbitrateEvent      loadEvent;
    private TerminArbitrateEvent    terminEvent;
    private ToolArbitrateEvent      toolEvent;

    public MainStemArbitrateEvent mainStemEvent() {
        return mainStemEvent;
    }

    public SelectArbitrateEvent selectEvent() {
        return selectEvent;
    }

    public ExtractArbitrateEvent extractEvent() {
        return extractEvent;
    }

    public TransformArbitrateEvent transformEvent() {
        return transformEvent;
    }

    public LoadArbitrateEvent loadEvent() {
        return loadEvent;
    }

    public TerminArbitrateEvent terminEvent() {
        return terminEvent;
    }

    public ToolArbitrateEvent toolEvent() {
        return toolEvent;
    }

    // ================ setter / getter ====================

    public void setTransformEvent(TransformArbitrateEvent transformEvent) {
        this.transformEvent = transformEvent;
    }

    public void setMainStemEvent(MainStemArbitrateEvent mainStemEvent) {
        this.mainStemEvent = mainStemEvent;
    }

    public void setSelectEvent(SelectArbitrateEvent selectEvent) {
        this.selectEvent = selectEvent;
    }

    public void setExtractEvent(ExtractArbitrateEvent extractEvent) {
        this.extractEvent = extractEvent;
    }

    public void setLoadEvent(LoadArbitrateEvent loadEvent) {
        this.loadEvent = loadEvent;
    }

    public void setTerminEvent(TerminArbitrateEvent terminEvent) {
        this.terminEvent = terminEvent;
    }

    public void setToolEvent(ToolArbitrateEvent toolEvent) {
        this.toolEvent = toolEvent;
    }

}
