package com.alibaba.otter.shared.common.utils.extension;

import com.alibaba.otter.shared.common.model.config.data.ExtensionData;

/**
 * 扩展类获取接口
 * 
 * @author jianghang 2012-10-23 下午04:29:18
 * @version 4.1.0
 */
public interface ExtensionFactory {

    /**
     * Get extension.
     * 
     * @param type object type.
     * @param name object name.
     * @return object instance.
     */
    <T> T getExtension(Class<T> type, ExtensionData extensionData);

}
