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

package com.alibaba.otter.manager.biz.config.canal;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.canal.instance.manager.model.Canal;

/**
 * @author sarah.lij 2012-7-25 下午04:02:20
 */
public interface CanalService {

    public void create(Canal canal);

    public void remove(Long canalId);

    public void modify(Canal canal);

    public List<Canal> listByIds(Long... identities);

    public List<Canal> listAll();

    public Canal findById(Long canalId);

    public Canal findByName(String name);

    public int getCount(Map condition);

    public List<Canal> listByCondition(Map condition);

}
