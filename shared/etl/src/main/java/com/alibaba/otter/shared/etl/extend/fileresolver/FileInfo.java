package com.alibaba.otter.shared.etl.extend.fileresolver;

/**
 * 文件描述信息
 * 
 * @version 4.1.0
 */
public class FileInfo {

    // private File file;
    private long   lastModifiedTime;
    private String namespace;
    private String path;
    private long   size;

    public FileInfo(String path){
        this.namespace = "";
        // this.file = new File(path);
        // this.size = file.length();
        // this.lastModifiedTime = file.lastModified();
        this.path = path;
    }

    public FileInfo(String namespace, String path){
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long getSize() {
        return size;
    }

    public String toString() {
        return "FileInfo [namespace=" + namespace + ", path=" + path + ", size=" + size + ", modifyTime="
               + lastModifiedTime + "]";
    }
}
