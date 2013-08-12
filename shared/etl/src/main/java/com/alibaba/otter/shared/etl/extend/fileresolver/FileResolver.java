package com.alibaba.otter.shared.etl.extend.fileresolver;

import java.util.Map;

/**
 * 文件提取接口类，包含附件的业务表需要实现该接口获取记录对应的附件信息.
 * 
 * @author xiaoqing.zhouxq
 */
public interface FileResolver {

    /**
     * Get attachment file, the logic:
     * 
     * @param rowMap key=column_name(UpCase) value=column_value
     * @return FileInfo[] or null
     */
    public FileInfo[] getFileInfo(Map<String, String> rowMap);

    /**
     * 针对数据delete类型是否删除对应文件
     * 
     * @return
     */
    public boolean isDeleteRequired();

    public boolean isDistributed();

}
