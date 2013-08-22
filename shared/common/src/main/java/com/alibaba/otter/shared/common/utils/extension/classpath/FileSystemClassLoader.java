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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于文件系统的classloader.
 * 
 * @author xiaoqing.zhouxq
 */
public class FileSystemClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemClassLoader.class);
    private String              rootDir;

    public FileSystemClassLoader(String rootDir, ClassLoader parent){
        super(parent);
        this.rootDir = rootDir;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = getClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            return defineClass(name, classData, 0, classData.length);
        }
    }

    private byte[] getClassData(String className) {
        String path = classNameToPath(className);
        InputStream ins = null;
        try {
            ins = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesNumRead = 0;
            while ((bytesNumRead = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesNumRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("ERROR ## get class data has an error", e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                    logger.error("ERROR ## close inputstream has an error", e);
                }
            }
        }
        return null;
    }

    private String classNameToPath(String className) {
        return rootDir + File.separatorChar + className.replace('.', File.separatorChar) + ".class";
    }

}
