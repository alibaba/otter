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

package com.alibaba.otter.manager.biz.config.datamatrix;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.model.config.data.DataMatrix;

public interface DataMatrixService {

    public void create(DataMatrix DataMatrix);

    public void remove(Long DataMatrixId);

    public void modify(DataMatrix DataMatrix);

    public List<DataMatrix> listByIds(Long... identities);

    public List<DataMatrix> listAll();

    public DataMatrix findById(Long DataMatrixId);

    public DataMatrix findByGroupKey(String name);

    public int getCount(Map condition);

    public List<DataMatrix> listByCondition(Map condition);

}
