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

package com.alibaba.otter.node.etl.common.io.download.impl.observer;

//~--- JDK imports ------------------------------------------------------------

import java.util.Observable;
import java.util.Observer;

import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandDownload;

/**
 * @author brave.taoy
 */
public abstract class ExceptionObserver implements Observer {

    public abstract void exceptionOccured(AbstractCommandDownload download, Exception status);

    public void update(Observable o, Object arg) {
        if (arg instanceof Exception) {
            exceptionOccured((AbstractCommandDownload) o, (Exception) arg);
        }
    }
}
