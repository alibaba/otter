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

package com.alibaba.otter.shared.arbitrate.impl.setl.helper;

import java.text.MessageFormat;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.helper.ManagePathUtils;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 对应zookeeper path构建的helper类
 * 
 * @author jianghang
 */
public class StagePathUtils extends ManagePathUtils {

    /**
     * 返回对应的pipeline path
     */
    public static String getPipeline(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PIPELINE_FORMAT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId));
    }

    /**
     * 返回对应的opposite pipeline path
     */
    public static String getOppositePipeline(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PIPELINE_FORMAT, getChannelId(pipelineId),
                                    getOppositePipelineId(pipelineId));
    }

    /**
     * 返回对应的channel path
     */
    public static String getChannel(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_CHANNEL_FORMAT, getChannelId(pipelineId));
    }

    /**
     * 返回对应的remedy root path
     */
    public static String getRemedyRoot(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_REMEDY_ROOT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId));
    }

    /**
     * 返回对应的process root path
     */
    public static String getProcessRoot(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PROCESS_ROOT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId));
    }

    /**
     * 返回对应的process path
     */
    public static String getProcess(Long pipelineId, Long processId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PROCESS_FORMAT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId), getProcessNode(processId));
    }

    /**
     * 返回对应的termin root path
     */
    public static String getTerminRoot(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_TERMIN_ROOT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId));
    }

    /**
     * 返回对应的termin path
     */
    public static String getTermin(Long pipelineId, Long processId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_TERMIN_FORMAT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId), getProcessNode(processId));
    }

    /**
     * 返回对应的process path
     */
    public static String getProcess(Long pipelineId, String processNode) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_PROCESS_FORMAT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId), processNode);
    }

    /**
     * 返回对应的mainStem path
     */
    public static String getMainStem(Long pipelineId) {
        return getPipeline(pipelineId) + "/" + ArbitrateConstants.NODE_MAINSTEM;
    }

    /**
     * 返回对应的opposite mainStem path
     */
    public static String getOppositeMainStem(Long pipelineId) {
        return getOppositePipeline(pipelineId) + "/" + ArbitrateConstants.NODE_MAINSTEM;
    }

    /**
     * 返回对应的lock root path
     */
    public static String getLockRoot(Long pipelineId) {
        // 根据channelId , pipelineId构造path
        return MessageFormat.format(ArbitrateConstants.NODE_LOCK_ROOT, getChannelId(pipelineId),
                                    String.valueOf(pipelineId));
    }

    /**
     * 返回对应的load模块的lock路径
     */
    public static String getLoadLock(Long pipelineId) {
        return getLockRoot(pipelineId) + "/" + ArbitrateConstants.NODE_LOCK_LOAD;
    }

    // =========================== stage path =========================

    /**
     * 返回对应的select stage path
     */
    public static String getSelectStage(Long pipelineId, Long processId) {
        return getProcess(pipelineId, processId) + "/" + ArbitrateConstants.NODE_SELECTED;
    }

    /**
     * 返回对应的extract stage path
     */
    public static String getExtractStage(Long pipelineId, Long processId) {
        return getProcess(pipelineId, processId) + "/" + ArbitrateConstants.NODE_EXTRACTED;
    }

    /**
     * 返回对应的transform stage path
     */
    public static String getTransformStage(Long pipelineId, Long processId) {
        return getProcess(pipelineId, processId) + "/" + ArbitrateConstants.NODE_TRANSFORMED;
    }

    // /**
    // * 返回对应的load stage path
    // */
    // public static String getLoadStage(Long pipelineId, Long processId) {
    // return getProcess(pipelineId, processId) + "/" + ArbitrateConstants.NODE_LOADED;
    // }

    // ================ helper method ================

    private static String getChannelId(Long pipelineId) {
        Channel channel = ArbitrateConfigUtils.getChannel(pipelineId);
        return String.valueOf(channel.getId());
    }

    private static String getOppositePipelineId(Long pipelineId) {
        Pipeline pipeline = ArbitrateConfigUtils.getOppositePipeline(pipelineId);
        if (pipeline != null) {
            Long id = pipeline.getId();
            return String.valueOf(id);
        }

        throw new ArbitrateException("pipeline[" + pipelineId + "] has not opposite pipeline!");
    }

}
