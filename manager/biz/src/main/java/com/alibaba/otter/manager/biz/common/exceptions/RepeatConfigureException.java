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

package com.alibaba.otter.manager.biz.common.exceptions;

/**
 * @author simon 2011-11-14 下午11:04:32
 */
public class RepeatConfigureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RepeatConfigureException(String cause){
        super(cause);
    }

    public RepeatConfigureException(Throwable t){
        super(t);
    }

    public RepeatConfigureException(String cause, Throwable t){
        super(cause, t);
    }

}
