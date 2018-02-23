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
 * @author jianghang 2011-9-2 上午11:36:21
 */
public enum DataMediaType {
    /** mysql DB */
    MYSQL,
    /** oracle DB */
    ORACLE,
    /** cobar */
    COBAR,
    /** tddl */
    TDDL,
    /** cache */
    MEMCACHE,
    /** mq */
    MQ,
    /** napoli */
    NAPOLI,
    /** diamond push for us */
    DIAMOND_PUSH;

    public boolean isMysql() {
        return this == DataMediaType.MYSQL;
    }

    public boolean isOracle() {
        return this == DataMediaType.ORACLE;
    }

    public boolean isTddl() {
        return this == DataMediaType.TDDL;
    }

    public boolean isCobar() {
        return this == DataMediaType.COBAR;
    }

    public boolean isMemcache() {
        return this == DataMediaType.MEMCACHE;
    }

    public boolean isMq() {
        return this == DataMediaType.MQ;
    }

    public boolean isNapoli() {
        return this == DataMediaType.NAPOLI;
    }

    public boolean isDiamondPush() {
        return this == DataMediaType.DIAMOND_PUSH;
    }
}
