package com.alibaba.otter.node.etl.conflict;

import com.alibaba.otter.shared.etl.model.FileBatch;

/**
 * 文件冲突检测service
 * 
 * @author jianghang 2011-11-10 上午09:34:44
 * @version 4.0.0
 */
public interface FileBatchConflictDetectService {

    /**
     * 和本地的file进行冲突检测，过滤冲突记录，返回无冲突的记录
     * 
     * <pre>
     * <strong>注意：在extract之前调用该方法</strong>
     * </pre>
     */
    public FileBatch detect(FileBatch fileBatch, Long targetNodeId);

}
