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

/**
 * 类ResolverType.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-10-16 下午7:47:01
 * @version 4.1.0
 */
public enum ExtensionDataType {
    CLAZZ, SOURCE;

    public boolean isClazz() {
        return this.equals(ExtensionDataType.CLAZZ);
    }

    public boolean isSource() {
        return this.equals(ExtensionDataType.SOURCE);
    }
}
