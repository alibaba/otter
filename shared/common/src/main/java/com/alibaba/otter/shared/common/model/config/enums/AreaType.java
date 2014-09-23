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

package com.alibaba.otter.shared.common.model.config.enums;

/**
 * 标记一下地区信息，因为不同地区会有不同的配置信息
 * 
 * @author jianghang 2013-6-5 下午04:14:05
 * @version 4.1.9
 */
public enum AreaType {

    HZ, US;

    public boolean isHzArea() {
        return this.equals(AreaType.HZ);
    }

    public boolean isUsArea() {
        return this.equals(AreaType.US);
    }
}
