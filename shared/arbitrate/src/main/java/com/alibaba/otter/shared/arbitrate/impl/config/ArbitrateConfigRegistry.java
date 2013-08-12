package com.alibaba.otter.shared.arbitrate.impl.config;

/**
 * 配置获取注册接口
 * 
 * @author jianghang 2011-11-3 上午10:19:50
 * @version 4.0.0
 */
public class ArbitrateConfigRegistry {

    private static ArbitrateConfig config;

    public static void regist(ArbitrateConfig config) {
        ArbitrateConfigRegistry.config = config;
    }

    public static void unRegist(ArbitrateConfig config) {
        ArbitrateConfigRegistry.config = config;
    }

    public static ArbitrateConfig getConfig() {
        return config;
    }
}
