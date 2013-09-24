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

package com.alibaba.otter.node.etl.common.pipe.impl.http;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveException;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 远程URL组装实现类
 */
public class RemoteUrlBuilder implements InitializingBean {

    private int                 defaultDownloadPort = 8080;
    private ConfigClientService configClientService;
    private String              urlFormat;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(configClientService);
        Assert.notNull(urlFormat);
    }

    public String getUrl(Long pipelineId, String filePath) {
        Node node = configClientService.currentNode();
        Pipeline pipeline = configClientService.findPipeline(pipelineId);
        String ip = node.getIp();
        if (node.getParameters().getUseExternalIp() || pipeline.getParameters().getUseExternalIp()) {
            ip = node.getParameters().getExternalIp();

            if (StringUtils.isEmpty(ip)) {
                throw new ArchiveException(String.format("pipelineId:%s useExternalIp by nid[%s] has no external ip",
                    String.valueOf(pipelineId),
                    String.valueOf(node.getId())));
            }
        }

        Integer port = node.getParameters().getDownloadPort();// 注意为其下载端口
        if (port == null || port < 0) {
            port = defaultDownloadPort;
        }

        return MessageFormat.format(urlFormat, ip, String.valueOf(port), filePath);
    }

    // ================= setter / getter =====================

    public void setDefaultDownloadPort(int defaultDownloadPort) {
        this.defaultDownloadPort = defaultDownloadPort;
    }

    public void setUrlFormat(String urlFormat) {
        this.urlFormat = urlFormat;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

}
