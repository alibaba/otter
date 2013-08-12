package com.alibaba.otter.manager.web.common.api;

import com.alibaba.citrus.turbine.TurbineRunData;

/**
 * @author zebin.xuzb @ 2012-5-20
 */
public class DefaultApiAuthService implements ApiAuthService {

    @Override
    public boolean auth(TurbineRunData rundata) {
        return true; // TODO 需要增加验证逻辑
    }

}
