package com.alibaba.otter.shared.etl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件数据集合对象
 * 
 * @author jianghang 2012-10-31 下午05:56:01
 * @version 4.1.2
 */
public class FileBatch extends BatchObject<FileData> {

    private static final long serialVersionUID = -520456006652566067L;
    private List<FileData>    files            = new ArrayList<FileData>();

    public List<FileData> getFiles() {
        return files;
    }

    public void setFiles(List<FileData> files) {
        this.files = files;
    }

    public void merge(FileData data) {
        this.files.add(data);
    }

}
