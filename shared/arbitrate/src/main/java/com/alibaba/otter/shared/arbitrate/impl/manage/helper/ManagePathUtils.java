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

package com.alibaba.otter.shared.arbitrate.impl.manage.helper;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;

/**
 * 对应zookeeper path构建的helper类
 * 
 * @author jianghang
 */
public class ManagePathUtils {

    /**
     * 返回对应的otter root path
     */
    public static String getRoot() {
        return ArbitrateConstants.NODE_OTTER_ROOT;
    }

    /**
     * 返回对应的node root path
     */
    public static String getNodeRoot() {
        return ArbitrateConstants.NODE_NID_ROOT;
    }

    /**
     * 返回对应的channel root path
     */
    public static String getChannelRoot() {
        return ArbitrateConstants.NODE_CHANNEL_ROOT;
    }

    /**
     * 返回对应的node path
     */
    public static String getNode(Long nodeId) {
        // 根据nodeId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_NID_FORMAT, String.valueOf(nodeId));
    }

    /**
     * 返回对应的channel path (不依赖对应的config信息)
     */
    public static String getChannelByChannelId(Long channelId) {
        // 根据channelId 构造path
        return MessageFormat.format(ArbitrateConstants.NODE_CHANNEL_FORMAT, String.valueOf(channelId));
    }

    /**
     * 返回对应的pipeline path (不依赖对应的config信息)
     */
    public static String getPipeline(Long channelId, Long pipelineId) {
        // 根据channelId , pipelineId 构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PIPELINE_FORMAT,
            String.valueOf(channelId),
            String.valueOf(pipelineId));
    }

    /**
     * 返回对应的remedy root path
     */
    public static String getRemedyRoot(Long channelId, Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_REMEDY_ROOT,
            String.valueOf(channelId),
            String.valueOf(pipelineId));
    }

    /**
     * 返回对应的getProcess path (不依赖对应的config信息)
     */
    public static String getProcessRoot(Long channelId, Long pipelineId) {
        // 根据channelId , pipelineId 构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PROCESS_ROOT,
            String.valueOf(channelId),
            String.valueOf(pipelineId));
    }

    /**
     * 返回对应的mainStem path
     */
    public static String getMainStem(Long channelId, Long pipelineId) {
        return MessageFormat.format(ArbitrateConstants.NODE_PIPELINE_FORMAT,
            String.valueOf(channelId),
            String.valueOf(pipelineId))
               + "/" + ArbitrateConstants.NODE_MAINSTEM;
    }

    /**
     * 返回对应的termin root path(不依赖对应的config信息)
     */
    public static String getTerminRoot(Long channelId, Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_TERMIN_ROOT,
            String.valueOf(channelId),
            String.valueOf(pipelineId));
    }

    /**
     * 返回对应的process path
     */
    public static String getProcess(Long channelId, Long pipelineId, Long processId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PROCESS_FORMAT,
            String.valueOf(channelId),
            String.valueOf(pipelineId),
            getProcessNode(processId));
    }

    /**
     * 返回对应的process path
     */
    public static String getProcess(Long channelId, Long pipelineId, String processNode) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PROCESS_FORMAT,
            String.valueOf(channelId),
            String.valueOf(pipelineId),
            processNode);
    }

    /**
     * 返回对应的termin path
     */
    public static String getTermin(Long channelId, Long pipelineId, Long processId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_TERMIN_FORMAT,
            String.valueOf(channelId),
            String.valueOf(pipelineId),
            getProcessNode(processId));
    }

    /**
     * 返回对应的lock root path(不依赖对应的config信息)
     */
    public static String getLockRoot(Long channelId, Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_LOCK_ROOT,
            String.valueOf(channelId),
            String.valueOf(pipelineId));
    }

    // ======================== hleper method============================

    /**
     * zookeeper中的node名称转化为processId
     */
    public static Long getProcessId(String processNode) {
        return Long.valueOf(processNode);
    }

    /**
     * 将processId转化为zookeeper中的node名称
     */
    public static String getProcessNode(Long processId) {
        return StringUtils.leftPad(String.valueOf(processId.intValue()), 10, '0');
    }

}
