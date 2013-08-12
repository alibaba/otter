package com.alibaba.otter.manager.biz.config.parameter;

import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

/**
 * @author sarah.lij 2012-4-13 下午04:28:00
 */
public interface SystemParameterService {

    public void createOrUpdate(SystemParameter systemParameter);

    public SystemParameter find();
}
