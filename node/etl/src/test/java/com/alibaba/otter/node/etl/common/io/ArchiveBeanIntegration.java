package com.alibaba.otter.node.etl.common.io;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveBean;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveRetriverCallback;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.LazyFileInputStream;
import com.alibaba.otter.shared.etl.model.FileData;

public class ArchiveBeanIntegration extends BaseOtterTest {

    // @Test
    public void test_pack() {
        ArchiveBean archiveBean = new ArchiveBean();
        try {
            archiveBean.afterPropertiesSet();
            archiveBean.setUseLocalFileMutliThread(false);
        } catch (Exception e1) {
            want.fail();
        }

        File file = new File("/tmp/otter/test");
        Collection<File> allFiles = FileUtils.listFiles(file, new String[] { "jpg" }, true);

        List<FileData> fileDatas = new ArrayList<FileData>();
        for (File files : allFiles) {
            FileData data = new FileData();
            // data.setPath("wsproduct_repository/product_sku/76/84/32/84/768432847_10.summ.jpg");
            data.setPath(StringUtils.substringAfter(files.getAbsolutePath(), "/tmp/otter/test"));
            fileDatas.add(data);
        }

        File archiveFile = new File("/tmp/otter/test.gzip");
        if (archiveFile.exists()) {
            archiveFile.delete();
        }

        boolean result = archiveBean.pack(archiveFile, fileDatas, new ArchiveRetriverCallback<FileData>() {

            public InputStream retrive(FileData source) {
                return new LazyFileInputStream(new File("/tmp/otter/test", source.getPath()));
            }
        });

        if (!result) {
            want.fail();
        }

    }

    @Test
    public void test_unpack() {
        ArchiveBean archiveBean = new ArchiveBean();
        try {
            archiveBean.afterPropertiesSet();
            archiveBean.setUseLocalFileMutliThread(false);
        } catch (Exception e1) {
            want.fail();
        }

        File archiveFile = new File("/tmp/otter/test.gzip");
        // for (File archiveFile : archiveFiles.listFiles()) {
        // if (archiveFile.isDirectory()) {
        // continue;
        // }
        //
        // if (!"FileBatch-2013-03-07-16-27-05-245-369-3209577.gzip".equals(archiveFile.getName())) {
        // continue;
        // }

        System.out.println("start unpack : " + archiveFile.getName());
        File targetFile = new File("/tmp/otter", FilenameUtils.removeExtension(archiveFile.getName()));
        archiveBean.unpack(archiveFile, targetFile);
        // }
    }

}
