package com.alibaba.otter.manager.biz.config.channel.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO;

/**
 * @author simon
 */
public interface ChannelDAO extends GenericDAO<ChannelDO> {

    public List<ChannelDO> listChannelPks();
}
