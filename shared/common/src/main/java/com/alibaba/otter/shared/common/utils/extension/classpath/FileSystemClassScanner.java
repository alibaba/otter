/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.utils.extension.classpath;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 外部文件的class扫描器.
 * 
 * @author xiaoqing.zhouxq
 */
public class FileSystemClassScanner implements InitializingBean, ClassScanner {

    private static final Logger   logger     = LoggerFactory.getLogger(FileSystemClassScanner.class);

    private static final String   CLASS_FILE = ".class";
    private static final String   JAR_FILE   = ".jar";
    private String                extendsDir;
    private FileSystemClassLoader fileClassLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.fileClassLoader = new FileSystemClassLoader(extendsDir, this.getClass().getClassLoader());
    }

    public Class<?> scan(String className) {
        return findInDirectory(extendsDir, className);
    }

    private Class<?> findInDirectory(String dirStr, String className) {
        File dir = StrToFile(dirStr);
        File[] files = dir.listFiles();
        String rootPath = dir.getPath();
        for (File file : files) {
            if (file.isFile()) {
                String classFileName = file.getPath();
                if (classFileName.endsWith(CLASS_FILE)) {
                    String tempClassName = classFileName.substring(rootPath.length() - className.lastIndexOf("."),
                                                                   classFileName.length() - CLASS_FILE.length());
                    if (className.equals(pathToDot(tempClassName))) {
                        try {
                            return fileClassLoader.loadClass(className);
                        } catch (Exception ex) {
                            logger.warn("WARN ## load this class has an error,the fileName is = " + className, ex);
                        }
                    }
                } else if (classFileName.endsWith(JAR_FILE)) {
                    // logger.info("INFO ## cannot scane filesystem's jar file");
                    return scanInJar(classFileName, className);
                }
            } else if (file.isDirectory()) {
                Class<?> clz = findInDirectory(file.toString(), className);
                if (clz != null) {
                    return clz;
                }
            }
        }

        return null;
    }

    private Class<?> scanInJar(String jarFileName, String className) {
        ZipFile zipfile = null;

        try {
            zipfile = new ZipFile(jarFileName);
            Enumeration<?> zipenum = zipfile.entries();
            ZipEntry entry = null;
            String tempClassName = null;

            while (zipenum.hasMoreElements()) {
                entry = (ZipEntry) zipenum.nextElement();
                tempClassName = entry.getName();

                if (tempClassName.endsWith(".class")) {
                    tempClassName = StringUtils.replace(FilenameUtils.removeExtension(tempClassName), "/", ".");
                    if (tempClassName.equals(className)) {
                        try {
                            return fileClassLoader.loadClass(className);
                        } catch (Exception ex) {
                            logger.warn("WARN ## load this class has an error,the fileName is = " + className, ex);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
        return null;
    }

    private File StrToFile(String dirString) {
        File file = new File(dirString);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    private String pathToDot(String s) {
        return s.replace('/', '.').replace('\\', '.');
    }

    public void setExtendsDir(String extendsDir) {
        this.extendsDir = extendsDir;

        File dir = new File(extendsDir);
        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                logger.error("##ERROR", e);
            }
        }
    }

    public void setFileClassLoader(FileSystemClassLoader fileClassLoader) {
        this.fileClassLoader = fileClassLoader;
    }

}
