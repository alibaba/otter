package com.alibaba.otter.node.etl;

/**
 * otter 常量定义
 * 
 * @author jianghang 2012-4-21 下午04:20:18
 * @version 4.0.2
 */
public interface OtterConstants {

    public String NID_NAME                      = "nid";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline进行日志文件输出的键值.
     */
    public String splitPipelineLogFileKey       = "otter";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline在load时，归档输出的键值.
     */
    public String splitPipelineLoadLogFileKey   = "load";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline在select时，归档输出的键值.
     */
    public String splitPipelineSelectLogFileKey = "select";
}
