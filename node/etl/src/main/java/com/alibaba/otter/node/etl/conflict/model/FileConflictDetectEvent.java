package com.alibaba.otter.node.etl.conflict.model;

import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.etl.model.FileBatch;

/**
 * 文件冲突检测事件
 * 
 * @author jianghang
 */
public class FileConflictDetectEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private FileBatch         fileBatch;

    public FileConflictDetectEvent(){
        super(ConflictEventType.fileConflictDetect);
    }

    public FileBatch getFileBatch() {
        return fileBatch;
    }

    public void setFileBatch(FileBatch fileBatch) {
        this.fileBatch = fileBatch;
    }

}
