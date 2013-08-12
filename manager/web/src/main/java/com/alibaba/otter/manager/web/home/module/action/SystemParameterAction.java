package com.alibaba.otter.manager.web.home.module.action;

import java.util.Arrays;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

public class SystemParameterAction extends AbstractAction {

    @Resource(name = "systemParameterService")
    private SystemParameterService systemParameterService;

    /**
     * 修改系统参数
     */
    public void doEdit(@FormGroup("systemParameterDetailInfo") Group systemParameterDetailInfo, Navigator nav)
                                                                                                              throws Exception {

        SystemParameter systemParameter = new SystemParameter();
        systemParameterDetailInfo.setProperties(systemParameter);

        String hzZkClustersString = systemParameterDetailInfo.getField("hzZkClusters").getStringValue();
        String hzStoreClustersString = systemParameterDetailInfo.getField("hzStoreClusters").getStringValue();
        systemParameter.setHzZkClusters(Arrays.asList(StringUtils.split(hzZkClustersString, ";")));
        systemParameter.setHzStoreClusters(Arrays.asList(StringUtils.split(hzStoreClustersString, ";")));

        String usZkClustersString = systemParameterDetailInfo.getField("usZkClusters").getStringValue();
        String usStoreClustersString = systemParameterDetailInfo.getField("usStoreClusters").getStringValue();
        systemParameter.setUsZkClusters(Arrays.asList(StringUtils.split(usZkClustersString, ";")));
        systemParameter.setUsStoreClusters(Arrays.asList(StringUtils.split(usStoreClustersString, ";")));

        systemParameterService.createOrUpdate(systemParameter);
        nav.redirectToLocation("systemParameter.htm?");
    }
}
