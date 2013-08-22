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

package com.alibaba.otter.manager.biz.config.datamedia;

import java.util.List;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;

/**
 * @author simon
 */
public interface DataMediaService extends GenericService<DataMedia> {

    // public List<DataMedia> listDataMediaByIds(Long... dataMediaIds);

    public List<DataMedia> listByDataMediaSourceId(Long dataMediaSourceId);

    public Long createReturnId(DataMedia dataMedia);

    public List<String> queryColumnByMedia(DataMedia dataMedia);

    public List<String> queryColumnByMediaId(Long dataMediaId);

}
