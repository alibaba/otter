package com.alibaba.otter.shared.common.utils.extension.classpath;

/**
 * 用于扫描classpath下面和外部文件系统中类.
 * 
 * @author jianghang 2012-10-23 下午04:36:53
 * @version 4.1.0
 */
public interface ClassScanner {

    Class<?> scan(String className);
}
