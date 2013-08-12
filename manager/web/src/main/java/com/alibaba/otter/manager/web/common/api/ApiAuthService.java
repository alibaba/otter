package com.alibaba.otter.manager.web.common.api;

import com.alibaba.citrus.turbine.TurbineRunData;

/**
 * @author zebin.xuzb @ 2012-5-20
 */
public interface ApiAuthService {

    // 考虑不依赖 webx
    public boolean auth(TurbineRunData rundata);

}
