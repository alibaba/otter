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

package com.alibaba.otter.shared.common.utils.extension.exceptions;

/**
 * 类ExtensionLoadException.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-10-23 下午9:19:20
 * @version 4.1.0
 */
public class ExtensionLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExtensionLoadException(String cause){
        super(cause);
    }

    public ExtensionLoadException(Throwable t){
        super(t);
    }

    public ExtensionLoadException(String cause, Throwable t){
        super(cause, t);
    }
}
