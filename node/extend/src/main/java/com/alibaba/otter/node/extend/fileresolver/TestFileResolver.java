package com.alibaba.otter.node.extend.fileresolver;

import java.util.Map;

import com.alibaba.otter.shared.etl.extend.fileresolver.FileInfo;

public class TestFileResolver extends AbstractFileResolver {

    public FileInfo[] getFileInfo(Map<String, String> rowMap) {
        String labelAddress = rowMap.get("REMARKS");
        String arandaAddress = rowMap.get("REMARKS2");
        FileInfo fileInfo = null;
        if (labelAddress != null && labelAddress.length() != 0) {
            if (arandaAddress != null && arandaAddress.length() != 0) {
                fileInfo = new FileInfo(arandaAddress, labelAddress);
            } else {
                fileInfo = new FileInfo(labelAddress);
            }

            return new FileInfo[] { fileInfo };
        } else {
            return null;
        }
    }

}
