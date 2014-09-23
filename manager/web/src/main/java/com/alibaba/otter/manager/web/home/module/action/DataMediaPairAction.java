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

package com.alibaba.otter.manager.web.home.module.action;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.ExtensionData;
import com.alibaba.otter.shared.common.model.config.data.ExtensionDataType;

public class DataMediaPairAction {

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService   dataMediaPairService;

    @Resource(name = "dataMediaService")
    private DataMediaService       dataMediaService;

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    @Resource(name = "channelService")
    private ChannelService         channelService;

    /**
     * 添加DataMediaPair
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doAdd(@Param("submitKey") String submitKey, @FormGroup("dataMediaPairInfo") Group dataMediaPairInfo,
                      @FormField(name = "formDataMediaPairError", group = "dataMediaPairInfo") CustomErrors err,
                      Navigator nav) throws Exception {
        DataMediaPair dataMediaPair = new DataMediaPair();
        DataMedia sourceDataMedia = new DataMedia();
        DataMedia targetDataMedia = new DataMedia();
        dataMediaPairInfo.setProperties(dataMediaPair);

        // filter解析
        ExtensionDataType filterType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("filterType").getStringValue());
        ExtensionData filterData = new ExtensionData();
        filterData.setExtensionDataType(filterType);
        if (filterType.isClazz()) {
            filterData.setClazzPath(dataMediaPairInfo.getField("filterText").getStringValue());
        } else if (filterType.isSource()) {
            filterData.setSourceText(dataMediaPairInfo.getField("filterText").getStringValue());
        }
        dataMediaPair.setFilterData(filterData);

        // fileresovler解析
        ExtensionDataType resolverType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("resolverType").getStringValue());
        ExtensionData resolverData = new ExtensionData();
        resolverData.setExtensionDataType(resolverType);
        if (resolverType.isClazz()) {
            resolverData.setClazzPath(dataMediaPairInfo.getField("resolverText").getStringValue());
        } else if (resolverType.isSource()) {
            resolverData.setSourceText(dataMediaPairInfo.getField("resolverText").getStringValue());
        }
        dataMediaPair.setResolverData(resolverData);
        sourceDataMedia.setId(dataMediaPairInfo.getField("sourceDataMediaId").getLongValue());
        dataMediaPair.setSource(sourceDataMedia);
        targetDataMedia.setId(dataMediaPairInfo.getField("targetDataMediaId").getLongValue());
        dataMediaPair.setTarget(targetDataMedia);
        Long id = 0L;
        try {
            id = dataMediaPairService.createAndReturnId(dataMediaPair);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaPair");
            return;
        }
        if (submitKey.equals("保存")) {
            nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + dataMediaPair.getPipelineId());
        } else if (submitKey.equals("下一步")) {
            nav.redirectToLocation("addColumnPair.htm?dataMediaPairId=" + id + "&pipelineId="
                                   + dataMediaPair.getPipelineId() + "&dataMediaPairId=" + id + "&sourceMediaId="
                                   + sourceDataMedia.getId() + "&targetMediaId=" + targetDataMedia.getId());
        }
    }

    /**
     * 批量添加DataMediaPair
     * 
     * @param dataMediaPairInfo
     * @throws Exception
     */
    public void doBatchAdd(@FormGroup("batchDataMediaPairInfo") Group batchDataMediaPairInfo,
                           @Param("pipelineId") Long pipelineId,
                           @FormField(name = "formBatchDataMediaPairError", group = "batchDataMediaPairInfo") CustomErrors err,
                           Navigator nav) throws Exception {
        String batchPairContent = batchDataMediaPairInfo.getField("batchPairContent").getStringValue();
        List<String> StringPairs = Arrays.asList(batchPairContent.split("\r\n"));
        try {
            for (String stringPair : StringPairs) {
                List<String> pairData = Arrays.asList(stringPair.split(","));
                if (pairData.size() < 4) {
                    throw new ManagerException("[" + stringPair + "] the line not all parameters");
                }
                // build the pair source
                DataMedia sourceDataMedia = new DataMedia();
                DataMediaSource sourceDataMediaSource = dataMediaSourceService.findById(Long.parseLong(StringUtils.trimToNull(pairData.get(2))));
                sourceDataMedia.setNamespace(StringUtils.trimToNull(pairData.get(0)));
                sourceDataMedia.setName(StringUtils.trimToNull(pairData.get(1)));
                sourceDataMedia.setSource(sourceDataMediaSource);
                Long sourceMediaId = dataMediaService.createReturnId(sourceDataMedia);
                sourceDataMedia.setId(sourceMediaId);
                // build the pair target
                DataMedia targetDataMedia = new DataMedia();
                Long weight = 5L;
                if (StringUtils.isNumeric(pairData.get(3)) && pairData.size() <= 5) {// 如果是纯数字，那说明是简化配置模式
                    DataMediaSource targetDataMediaSource = dataMediaSourceService.findById(Long.parseLong(StringUtils.trimToNull(pairData.get(3))));
                    targetDataMedia.setNamespace(StringUtils.trimToNull(pairData.get(0)));
                    targetDataMedia.setName(StringUtils.trimToNull(pairData.get(1)));
                    targetDataMedia.setSource(targetDataMediaSource);
                    Long targetMediaId = dataMediaService.createReturnId(targetDataMedia);
                    targetDataMedia.setId(targetMediaId);

                    if (pairData.size() >= 5) {
                        weight = Long.parseLong(StringUtils.trimToNull(pairData.get(4)));
                    }
                } else {
                    DataMediaSource targetDataMediaSource = dataMediaSourceService.findById(Long.parseLong(StringUtils.trimToNull(pairData.get(5))));
                    targetDataMedia.setNamespace(StringUtils.trimToNull(pairData.get(3)));
                    targetDataMedia.setName(StringUtils.trimToNull(pairData.get(4)));
                    targetDataMedia.setSource(targetDataMediaSource);
                    Long targetMediaId = dataMediaService.createReturnId(targetDataMedia);
                    targetDataMedia.setId(targetMediaId);

                    if (pairData.size() >= 7) {
                        weight = Long.parseLong(StringUtils.trimToNull(pairData.get(6)));
                    }
                }

                // build the pair
                DataMediaPair dataMediaPair = new DataMediaPair();
                dataMediaPair.setSource(sourceDataMedia);
                dataMediaPair.setTarget(targetDataMedia);
                dataMediaPair.setPushWeight(weight);
                dataMediaPair.setPipelineId(pipelineId);

                dataMediaPairService.createIfNotExist(dataMediaPair);
            }
        } catch (Exception e) {
            err.setMessage("invalidBatchDataMediaPair");
            return;
        }
        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + pipelineId);
    }

    public void doEdit(@Param("submitKey") String submitKey, @Param("channelId") Long channelId,
                       @FormGroup("dataMediaPairInfo") Group dataMediaPairInfo,
                       @FormField(name = "formDataMediaPairError", group = "dataMediaPairInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        DataMediaPair dataMediaPair = new DataMediaPair();
        DataMedia sourceDataMedia = new DataMedia();
        DataMedia targetDataMedia = new DataMedia();
        dataMediaPairInfo.setProperties(dataMediaPair);

        // filter解析
        ExtensionDataType filterType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("filterType").getStringValue());
        ExtensionData filterData = new ExtensionData();
        filterData.setExtensionDataType(filterType);
        if (filterType.isClazz()) {
            filterData.setClazzPath(dataMediaPairInfo.getField("filterText").getStringValue());
        } else if (filterType.isSource()) {
            filterData.setSourceText(dataMediaPairInfo.getField("filterText").getStringValue());
        }
        dataMediaPair.setFilterData(filterData);

        // fileresovler解析
        ExtensionDataType resolverType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("resolverType").getStringValue());
        ExtensionData resolverData = new ExtensionData();
        resolverData.setExtensionDataType(resolverType);
        if (resolverType.isClazz()) {
            resolverData.setClazzPath(dataMediaPairInfo.getField("resolverText").getStringValue());
        } else if (resolverType.isSource()) {
            resolverData.setSourceText(dataMediaPairInfo.getField("resolverText").getStringValue());
        }
        dataMediaPair.setResolverData(resolverData);

        sourceDataMedia.setId(dataMediaPairInfo.getField("sourceDataMediaId").getLongValue());
        dataMediaPair.setSource(sourceDataMedia);
        targetDataMedia.setId(dataMediaPairInfo.getField("targetDataMediaId").getLongValue());
        dataMediaPair.setTarget(targetDataMedia);
        try {
            dataMediaPairService.modify(dataMediaPair);

        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaPair");
            return;
        }

        if (submitKey.equals("保存")) {
            nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + dataMediaPair.getPipelineId());
        } else if (submitKey.equals("下一步")) {
            nav.redirectToLocation("addColumnPair.htm?pipelineId=" + dataMediaPair.getPipelineId() + "&channelId="
                                   + channelId + "&dataMediaPairId=" + dataMediaPair.getId() + "&sourceMediaId="
                                   + sourceDataMedia.getId() + "&targetMediaId=" + targetDataMedia.getId());
        }
    }

    /**
     * 删除映射关系
     */
    public void doDelete(@Param("dataMediaPairId") Long dataMediaPairId, @Param("pipelineId") Long pipelineId,
                         Navigator nav) throws WebxException {
        Channel channel = channelService.findByPipelineId(pipelineId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }
        dataMediaPairService.remove(dataMediaPairId);
        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + pipelineId);
    }

    /**
     * 选择视图同步
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doNextToView(@FormGroup("dataMediaPairInfo") Group dataMediaPairInfo,
                             @FormField(name = "formDataMediaPairError", group = "dataMediaPairInfo") CustomErrors err,
                             Navigator nav) throws Exception {
        DataMediaPair dataMediaPair = new DataMediaPair();
        DataMedia sourceDataMedia = new DataMedia();
        DataMedia targetDataMedia = new DataMedia();
        dataMediaPairInfo.setProperties(dataMediaPair);
        sourceDataMedia.setId(dataMediaPairInfo.getField("sourceDataMediaId").getLongValue());
        dataMediaPair.setSource(sourceDataMedia);
        targetDataMedia.setId(dataMediaPairInfo.getField("targetDataMediaId").getLongValue());
        dataMediaPair.setTarget(targetDataMedia);
        try {
            dataMediaPairService.create(dataMediaPair);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaPair");
            return;
        }

        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + dataMediaPair.getPipelineId());
    }
}
