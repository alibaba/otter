package com.alibaba.otter.shared.common.model.config.data;

/**
 * 类ResolverType.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-10-16 下午7:47:01
 * @version 4.1.0
 */
public enum ExtensionDataType {
    CLAZZ, SOURCE;

    public boolean isClazz() {
        return this.equals(ExtensionDataType.CLAZZ);
    }

    public boolean isSource() {
        return this.equals(ExtensionDataType.SOURCE);
    }
}
