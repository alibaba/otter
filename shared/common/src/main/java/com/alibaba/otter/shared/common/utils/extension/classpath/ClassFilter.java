package com.alibaba.otter.shared.common.utils.extension.classpath;

public interface ClassFilter {

    boolean accept(Class<?> clazz);
}
