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

package com.alibaba.otter.manager.web.common.model;

import java.util.List;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.DataSourcing;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * @author sarah.lij 2012-7-24 下午01:31:06
 */
public class SeniorCanal extends Canal {

    private static final long serialVersionUID = -4121314684324595191L;
    private boolean           used;
    private List<Pipeline>    pipelines;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public String getUrl() {
        CanalParameter parameter = getCanalParameter();
        if (parameter.getHaMode().isMedia()) {
            return "media://" + parameter.getMediaGroup();
        } else {
            StringBuilder address = new StringBuilder("jdbc://");
            for (List<DataSourcing> groupAddress : parameter.getGroupDbAddresses()) {
                int i = 0;
                for (DataSourcing dbAddress : groupAddress) {
                    ++i;
                    address.append(dbAddress.getDbAddress().getAddress().getHostName())
                        .append(":")
                        .append(dbAddress.getDbAddress().getPort());

                    if (i < groupAddress.size()) {
                        address.append(',');
                    }
                }

                address.append(';');
            }

            return address.toString();
        }
    }
}
