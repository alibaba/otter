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

package com.alibaba.otter.manager.biz.config.channel.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.otter.manager.biz.common.exceptions.InvalidConfigureException;
import com.alibaba.otter.manager.biz.common.exceptions.InvalidConfigureException.INVALID_TYPE;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.channel.dal.ChannelDAO;
import com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.remote.ConfigRemoteService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * 主要提供增加、删除、修改、列表功能； 提供开启和停止channel方法，需要调用仲裁器方法
 * 
 * @author simon
 */
public class ChannelServiceImpl implements ChannelService {

    private static final Logger    logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    private SystemParameterService systemParameterService;
    private ArbitrateManageService arbitrateManageService;
    private TransactionTemplate    transactionTemplate;
    private ConfigRemoteService    configRemoteService;
    private PipelineService        pipelineService;
    private ChannelDAO             channelDao;

    /**
     * 添加Channel
     */
    public void create(final Channel channel) {
        Assert.assertNotNull(channel);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {

                    ChannelDO channelDo = modelToDo(channel);
                    channelDo.setId(0L);

                    if (!channelDao.checkUnique(channelDo)) {
                        String exceptionCause = "exist the same name channel in the database.";
                        logger.warn("WARN ## " + exceptionCause);
                        throw new RepeatConfigureException(exceptionCause);
                    }
                    channelDao.insert(channelDo);
                    arbitrateManageService.channelEvent().init(channelDo.getId());

                } catch (RepeatConfigureException rce) {
                    throw rce;
                } catch (Exception e) {
                    logger.error("ERROR ## create channel has an exception ", e);
                    throw new ManagerException(e);
                }
            }
        });
    }

    /**
     * 修改Channel
     */
    public void modify(Channel channel) {

        Assert.assertNotNull(channel);

        try {
            ChannelDO channelDo = modelToDo(channel);
            if (channelDao.checkUnique(channelDo)) {
                channelDao.update(channelDo);
            } else {
                String exceptionCause = "exist the same name channel in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }

        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## modify channel has an exception ", e);
            throw new ManagerException(e);
        }

    }

    /**
     * 删除Channel
     */
    public void remove(final Long channelId) {
        Assert.assertNotNull(channelId);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    arbitrateManageService.channelEvent().destory(channelId);
                    channelDao.delete(channelId);
                } catch (Exception e) {
                    logger.error("ERROR ## remove channel has an exception ", e);
                    throw new ManagerException(e);
                }
            }
        });

    }

    /*--------------------优化内容：listAll、listByIds、findById合并-------------------------------*/

    public List<Channel> listByIds(Long... identities) {

        List<Channel> channels = new ArrayList<Channel>();
        try {
            List<ChannelDO> channelDos = null;
            if (identities.length < 1) {
                channelDos = channelDao.listAll();
                if (channelDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any channel, maybe hasn't create any channel.");
                    return channels;
                }
            } else {
                channelDos = channelDao.listByMultiId(identities);
                if (channelDos.isEmpty()) {
                    String exceptionCause = "couldn't query any channel by channelIds:" + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            channels = doToModel(channelDos);
        } catch (Exception e) {
            logger.error("ERROR ## query channels has an exception!");
            throw new ManagerException(e);
        }

        return channels;
    }

    /**
     * 列出所有的Channel对象
     */
    public List<Channel> listAll() {
        return listByIds();
    }

    public List<Channel> listOnlyChannels(Long... identities) {

        List<Channel> channels = new ArrayList<Channel>();
        try {
            List<ChannelDO> channelDos = null;
            if (identities.length < 1) {
                channelDos = channelDao.listAll();
                if (channelDos.isEmpty()) {
                    logger.debug("DEBUG ## couldn't query any channel, maybe hasn't create any channel.");
                    return channels;
                }
            } else {
                channelDos = channelDao.listByMultiId(identities);
                if (channelDos.isEmpty()) {
                    String exceptionCause = "couldn't query any channel by channelIds:" + Arrays.toString(identities);
                    logger.error("ERROR ## " + exceptionCause);
                    throw new ManagerException(exceptionCause);
                }
            }
            channels = doToModelOnlyChannels(channelDos);
        } catch (Exception e) {
            logger.error("ERROR ## query channels has an exception!");
            throw new ManagerException(e);
        }

        return channels;
    }

    public List<Channel> listByCondition(Map condition) {
        List<ChannelDO> channelDos = channelDao.listByCondition(condition);
        if (channelDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any channel by the condition:" + JsonUtils.marshalToString(condition));
            return new ArrayList<Channel>();
        }
        return doToModel(channelDos);
    }

    public List<Channel> listByConditionWithoutColumn(Map condition) {
        List<ChannelDO> channelDos = channelDao.listByCondition(condition);
        if (channelDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any channel by the condition:" + JsonUtils.marshalToString(condition));
            return new ArrayList<Channel>();
        }
        return doToModelWithColumn(channelDos);
    }

    public List<Long> listAllChannelId() {
        List<ChannelDO> channelDos = channelDao.listChannelPks();
        List<Long> channelPks = new ArrayList<Long>();
        if (channelDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any channel");
        }
        for (ChannelDO channelDo : channelDos) {
            channelPks.add(channelDo.getId());
        }
        return channelPks;
    }

    /**
     * <pre>
     * 通过ChannelId找到对应的Channel对象
     * 并且根据ChannelId找到对应的所有Pipeline。
     * </pre>
     */
    public Channel findById(Long channelId) {
        Assert.assertNotNull(channelId);
        List<Channel> channels = listByIds(channelId);
        if (channels.size() != 1) {
            String exceptionCause = "query channelId:" + channelId + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }
        return channels.get(0);
    }

    public Channel findByIdWithoutColumn(Long channelId) {
        List<ChannelDO> channelDos = channelDao.listByMultiId(channelId);
        if (channelDos.size() != 1) {
            String exceptionCause = "query channelId:" + channelId + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        List<Channel> channels = doToModelWithColumn(channelDos);
        return channels.get(0);
    }

    /*--------------------外部关联查询Channel-----------------------*/

    /**
     * <pre>
     * 根据PipelineID找到对应的Channel
     * 优化设想：
     *    应该通过变长参数达到后期扩展的方便性
     * </pre>
     */
    public Channel findByPipelineId(Long pipelineId) {
        Pipeline pipeline = pipelineService.findById(pipelineId);
        Channel channel = findById(pipeline.getChannelId());
        return channel;
    }

    /**
     * <pre>
     * 根据PipelineID找到对应的Channel
     * 优化设想：
     *    应该通过变长参数达到后期扩展的方便性
     * </pre>
     */
    public List<Channel> listByPipelineIds(Long... pipelineIds) {
        List<Channel> channels = new ArrayList<Channel>();
        try {
            List<Pipeline> pipelines = pipelineService.listByIds(pipelineIds);

            List<Long> channelIds = new ArrayList<Long>();

            for (Pipeline pipeline : pipelines) {
                if (!channelIds.contains(pipeline.getChannelId())) {
                    channelIds.add(pipeline.getChannelId());
                }
            }
            channels = listByIds(channelIds.toArray(new Long[channelIds.size()]));
        } catch (Exception e) {
            logger.error("ERROR ## list query channel by pipelineIds:" + pipelineIds.toString() + " has an exception!");
            throw new ManagerException(e);
        }
        return channels;
    }

    /**
     * pipelineService 根据NodeId找到对应已启动的Channel列表。
     */
    public List<Channel> listByNodeId(Long nodeId) {
        return listByNodeId(nodeId, new ChannelStatus[] {});
    }

    /**
     * 根据NodeId和Channel状态找到对应的Channel列表。
     */
    public List<Channel> listByNodeId(Long nodeId, ChannelStatus... statuses) {
        List<Channel> channels = new ArrayList<Channel>();
        List<Channel> results = new ArrayList<Channel>();
        try {
            List<Pipeline> pipelines = pipelineService.listByNodeId(nodeId);
            List<Long> pipelineIds = new ArrayList<Long>();
            for (Pipeline pipeline : pipelines) {
                pipelineIds.add(pipeline.getId());
            }

            if (pipelineIds.isEmpty()) { // 没有关联任务直接返回
                return channels;
            }

            // 反查对应的channel
            channels = listByPipelineIds(pipelineIds.toArray(new Long[pipelineIds.size()]));
            if (null == statuses || statuses.length == 0) {
                return channels;
            }

            for (Channel channel : channels) {
                for (ChannelStatus status : statuses) {
                    if (channel.getStatus().equals(status)) {
                        results.add(channel);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ERROR ## list query channel by nodeId:" + nodeId + " has an exception!");
            throw new ManagerException(e);
        }
        return results;
    }

    /**
     * 拿到channel总数进行分页
     */
    public int getCount() {
        return channelDao.getCount();
    }

    public int getCount(Map condition) {
        return channelDao.getCount(condition);
    }

    /*----------------------Start/Stop Channel 短期优化：增加异常和条件判断--------------------------*/
    /**
     * <pre>
     * 切换Channel状态
     *      1.首先判断Channel是否为空或状态位是否正确
     *      2.通知总裁器，更新节点
     *      3.数据库数据库更新状态
     *      4.调用远程方法，推送Channel到node节点
     * </pre>
     */
    private void switchChannelStatus(final Long channelId, final ChannelStatus channelStatus) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    final ChannelDO channelDo = channelDao.findById(channelId);

                    if (null == channelDo) {
                        String exceptionCause = "query channelId:" + channelId + " return null.";
                        logger.error("ERROR ## " + exceptionCause);
                        throw new ManagerException(exceptionCause);
                    }

                    ChannelStatus oldStatus = arbitrateManageService.channelEvent().status(channelDo.getId());
                    Channel channel = doToModel(channelDo);
                    // 检查下ddl/home配置
                    List<Pipeline> pipelines = channel.getPipelines();
                    if (pipelines.size() > 1) {
                        boolean ddlSync = true;
                        boolean homeSync = true;
                        for (Pipeline pipeline : pipelines) {
                            homeSync &= pipeline.getParameters().isHome();
                            ddlSync &= pipeline.getParameters().getDdlSync();
                        }

                        if (ddlSync) {
                            throw new InvalidConfigureException(INVALID_TYPE.DDL);
                        }

                        if (homeSync) {
                            throw new InvalidConfigureException(INVALID_TYPE.HOME);
                        }
                    }

                    channel.setStatus(oldStatus);
                    ChannelStatus newStatus = channelStatus;
                    if (newStatus != null) {
                        if (newStatus.equals(oldStatus)) {
                            // String exceptionCause = "switch the channel(" +
                            // channelId + ") status to " +
                            // channelStatus
                            // + " but it had the status:" + oldStatus;
                            // logger.error("ERROR ## " + exceptionCause);
                            // throw new ManagerException(exceptionCause);
                            // ignored
                            return;
                        } else {
                            channel.setStatus(newStatus);// 强制修改为当前变更状态
                        }
                    } else {
                        newStatus = oldStatus;
                    }

                    // 针对关闭操作，要优先更改对应的status，避免node工作线程继续往下跑
                    if (newStatus.isStop()) {
                        arbitrateManageService.channelEvent().stop(channelId);
                    } else if (newStatus.isPause()) {
                        arbitrateManageService.channelEvent().pause(channelId);
                    }

                    // 通知变更内容
                    boolean result = configRemoteService.notifyChannel(channel);// 客户端响应成功，才更改对应的状态

                    if (result) {
                        // 针对启动的话，需要先通知到客户端，客户端启动线程后，再更改channel状态
                        if (newStatus.isStart()) {
                            arbitrateManageService.channelEvent().start(channelId);
                        }
                    }

                } catch (Exception e) {
                    logger.error("ERROR ## switch the channel(" + channelId + ") status has an exception.");
                    throw new ManagerException(e);
                }
            }
        });

    }

    public void stopChannel(Long channelId) {
        switchChannelStatus(channelId, ChannelStatus.STOP);
    }

    public void startChannel(Long channelId) {
        switchChannelStatus(channelId, ChannelStatus.START);
    }

    public void notifyChannel(Long channelId) {
        switchChannelStatus(channelId, null);
    }

    /*----------------------DO <-> MODEL 组装方法--------------------------*/
    /**
     * <pre>
     * 用于Model对象转化为DO对象
     * 优化：
     *      无SQL交互，只是简单进行字段组装，暂时无须优化
     * </pre>
     * 
     * @param channel
     * @return ChannelDO
     */
    private ChannelDO modelToDo(Channel channel) {

        ChannelDO channelDO = new ChannelDO();
        try {
            channelDO.setId(channel.getId());
            channelDO.setName(channel.getName());
            channelDO.setDescription(channel.getDescription());
            channelDO.setStatus(channel.getStatus());
            channelDO.setParameters(channel.getParameters());
            channelDO.setGmtCreate(channel.getGmtCreate());
            channelDO.setGmtModified(channel.getGmtModified());
        } catch (Exception e) {
            logger.error("ERROR ## change the channel Model to Do has an exception");
            throw new ManagerException(e);
        }
        return channelDO;
    }

    /**
     * <pre>
     * 用于DO对象转化为Model对象
     * 现阶段优化：
     *      需要五次SQL交互:pipeline\node\dataMediaPair\dataMedia\dataMediaSource（五个层面）
     *      目前优化方案为单层只执行一次SQL，避免重复循环造成IO及数据库查询开销
     * 长期优化：
     *      对SQL进行改造，尽量减小SQL调用次数
     * </pre>
     * 
     * @param channelDO
     * @return Channel
     */

    private Channel doToModel(ChannelDO channelDo) {
        Channel channel = new Channel();
        try {
            channel.setId(channelDo.getId());
            channel.setName(channelDo.getName());
            channel.setDescription(channelDo.getDescription());
            channel.setStatus(arbitrateManageService.channelEvent().status(channelDo.getId()));
            channel.setParameters(channelDo.getParameters());
            channel.setGmtCreate(channelDo.getGmtCreate());
            channel.setGmtModified(channelDo.getGmtModified());
            List<Pipeline> pipelines = pipelineService.listByChannelIds(channelDo.getId());
            // 合并PipelineParameter和ChannelParameter
            SystemParameter systemParameter = systemParameterService.find();
            for (Pipeline pipeline : pipelines) {
                PipelineParameter parameter = new PipelineParameter();
                parameter.merge(systemParameter);
                parameter.merge(channel.getParameters());
                // 最后复制pipelineId参数
                parameter.merge(pipeline.getParameters());
                pipeline.setParameters(parameter);
                // pipeline.getParameters().merge(channel.getParameters());
            }
            channel.setPipelines(pipelines);
        } catch (Exception e) {
            logger.error("ERROR ## change the channel DO to Model has an exception");
            throw new ManagerException(e);
        }

        return channel;
    }

    /**
     * <pre>
     * 用于DO对象数组转化为Model对象数组
     * 现阶段优化：
     *      需要五次SQL交互:pipeline\node\dataMediaPair\dataMedia\dataMediaSource（五个层面）
     *      目前优化方案为单层只执行一次SQL，避免重复循环造成IO及数据库查询开销
     * 长期优化：
     *      对SQL进行改造，尽量减小SQL调用次数
     * </pre>
     * 
     * @param channelDO
     * @return Channel
     */
    private List<Channel> doToModel(List<ChannelDO> channelDos) {
        List<Channel> channels = new ArrayList<Channel>();
        try {
            // 1.将ChannelID单独拿出来
            List<Long> channelIds = new ArrayList<Long>();
            for (ChannelDO channelDo : channelDos) {
                channelIds.add(channelDo.getId());
            }
            Long[] idArray = new Long[channelIds.size()];

            // 拿到所有的Pipeline进行ChannelID过滤，避免重复查询。
            List<Pipeline> pipelines = pipelineService.listByChannelIds(channelIds.toArray(idArray));
            SystemParameter systemParameter = systemParameterService.find();
            for (ChannelDO channelDo : channelDos) {
                Channel channel = new Channel();
                channel.setId(channelDo.getId());
                channel.setName(channelDo.getName());
                channel.setDescription(channelDo.getDescription());
                ChannelStatus channelStatus = arbitrateManageService.channelEvent().status(channelDo.getId());
                channel.setStatus(null == channelStatus ? ChannelStatus.STOP : channelStatus);
                channel.setParameters(channelDo.getParameters());
                channel.setGmtCreate(channelDo.getGmtCreate());
                channel.setGmtModified(channelDo.getGmtModified());
                // 遍历，将该Channel节点下的Pipeline提取出来。
                List<Pipeline> subPipelines = new ArrayList<Pipeline>();
                for (Pipeline pipeline : pipelines) {
                    if (pipeline.getChannelId().equals(channelDo.getId())) {
                        // 合并PipelineParameter和ChannelParameter
                        PipelineParameter parameter = new PipelineParameter();
                        parameter.merge(systemParameter);
                        parameter.merge(channel.getParameters());
                        // 最后复制pipelineId参数
                        parameter.merge(pipeline.getParameters());
                        pipeline.setParameters(parameter);
                        subPipelines.add(pipeline);
                    }
                }

                channel.setPipelines(subPipelines);
                channels.add(channel);
            }
        } catch (Exception e) {
            logger.error("ERROR ## change the channels DO to Model has an exception");
            throw new ManagerException(e);
        }

        return channels;
    }

    private List<Channel> doToModelWithColumn(List<ChannelDO> channelDos) {
        List<Channel> channels = new ArrayList<Channel>();
        try {
            // 1.将ChannelID单独拿出来
            List<Long> channelIds = new ArrayList<Long>();
            for (ChannelDO channelDo : channelDos) {
                channelIds.add(channelDo.getId());
            }
            Long[] idArray = new Long[channelIds.size()];

            // 拿到所有的Pipeline进行ChannelID过滤，避免重复查询。
            List<Pipeline> pipelines = pipelineService.listByChannelIdsWithoutColumn(channelIds.toArray(idArray));
            SystemParameter systemParameter = systemParameterService.find();
            for (ChannelDO channelDo : channelDos) {
                Channel channel = new Channel();
                channel.setId(channelDo.getId());
                channel.setName(channelDo.getName());
                channel.setDescription(channelDo.getDescription());
                ChannelStatus channelStatus = arbitrateManageService.channelEvent().status(channelDo.getId());
                channel.setStatus(null == channelStatus ? ChannelStatus.STOP : channelStatus);
                channel.setParameters(channelDo.getParameters());
                channel.setGmtCreate(channelDo.getGmtCreate());
                channel.setGmtModified(channelDo.getGmtModified());
                // 遍历，将该Channel节点下的Pipeline提取出来。
                List<Pipeline> subPipelines = new ArrayList<Pipeline>();
                for (Pipeline pipeline : pipelines) {
                    if (pipeline.getChannelId().equals(channelDo.getId())) {
                        // 合并PipelineParameter和ChannelParameter
                        PipelineParameter parameter = new PipelineParameter();
                        parameter.merge(systemParameter);
                        parameter.merge(channel.getParameters());
                        // 最后复制pipelineId参数
                        parameter.merge(pipeline.getParameters());
                        pipeline.setParameters(parameter);
                        subPipelines.add(pipeline);
                    }
                }

                channel.setPipelines(subPipelines);
                channels.add(channel);
            }
        } catch (Exception e) {
            logger.error("ERROR ## change the channels DO to Model has an exception");
            throw new ManagerException(e);
        }

        return channels;
    }

    private List<Channel> doToModelOnlyChannels(List<ChannelDO> channelDos) {
        List<Channel> channels = new ArrayList<Channel>();
        try {
            // 1.将ChannelID单独拿出来
            List<Long> channelIds = new ArrayList<Long>();
            for (ChannelDO channelDo : channelDos) {
                channelIds.add(channelDo.getId());
            }

            for (ChannelDO channelDo : channelDos) {
                Channel channel = new Channel();
                channel.setId(channelDo.getId());
                channel.setName(channelDo.getName());
                channel.setDescription(channelDo.getDescription());
                ChannelStatus channelStatus = arbitrateManageService.channelEvent().status(channelDo.getId());
                channel.setStatus(null == channelStatus ? ChannelStatus.STOP : channelStatus);
                channel.setParameters(channelDo.getParameters());
                channel.setGmtCreate(channelDo.getGmtCreate());
                channel.setGmtModified(channelDo.getGmtModified());
                // 遍历，将该Channel节点下的Pipeline提取出来。
                List<Pipeline> subPipelines = new ArrayList<Pipeline>();
                channel.setPipelines(subPipelines);
                channels.add(channel);
            }
        } catch (Exception e) {
            logger.error("ERROR ## change the channels doToModelOnlyChannels has an exception");
            throw new ManagerException(e);
        }

        return channels;
    }

    /* ------------------------setter / getter--------------------------- */

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setChannelDao(ChannelDAO channelDao) {
        this.channelDao = channelDao;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setConfigRemoteService(ConfigRemoteService configRemoteService) {
        this.configRemoteService = configRemoteService;
    }

    public void setSystemParameterService(SystemParameterService systemParameterService) {
        this.systemParameterService = systemParameterService;
    }

}
