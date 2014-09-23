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

package com.alibaba.otter.shared.common.utils.extension;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.shared.common.model.config.data.ExtensionData;
import com.alibaba.otter.shared.common.utils.cache.ExtensionMemoryMirror;
import com.alibaba.otter.shared.common.utils.cache.ExtensionMemoryMirror.ComputeFunction;
import com.alibaba.otter.shared.common.utils.compile.impl.JdkCompiler;
import com.alibaba.otter.shared.common.utils.compile.model.JavaSource;
import com.alibaba.otter.shared.common.utils.extension.classpath.ClassScanner;
import com.alibaba.otter.shared.common.utils.extension.exceptions.ExtensionLoadException;

/**
 * @author jianghang 2012-11-7 下午02:11:46
 * @version 4.1.2
 */
public class DefaultExtensionFactory implements ExtensionFactory {

    private ExtensionMemoryMirror<ExtensionData, Object> resolverCache;
    private ClassScanner                                 classPathScanner;
    private ClassScanner                                 fileSystemScanner;
    private JdkCompiler                                  jdkCompiler;

    public DefaultExtensionFactory(){
        resolverCache = new ExtensionMemoryMirror<ExtensionData, Object>(new ComputeFunction<ExtensionData, Object>() {

            public Object apply(ExtensionData extensionData) {
                return getExtensionInternal(extensionData);
            }
        });
    }

    public <T> T getExtension(Class<T> type, ExtensionData extensionData) {
        return (T) resolverCache.get(extensionData);
    }

    private Object getExtensionInternal(ExtensionData extensionData) {
        Class<?> clazz = null;
        String fullname = StringUtils.EMPTY;

        if (extensionData.getExtensionDataType().isClazz() && StringUtils.isNotBlank(extensionData.getClazzPath())) {
            clazz = scan(extensionData.getClazzPath());
            fullname = "[" + extensionData.getClazzPath() + "]ClassPath";
        } else if (extensionData.getExtensionDataType().isSource()
                   && StringUtils.isNotBlank(extensionData.getSourceText())) {
            JavaSource javaSource = new JavaSource(extensionData.getSourceText());
            clazz = jdkCompiler.compile(javaSource);
            fullname = "[" + javaSource.toString() + "]SourceText";
        }

        if (clazz == null) {
            throw new ExtensionLoadException("ERROR ## classload this fileresolver=" + fullname + " has an error");
        }

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ExtensionLoadException("ERROR ## classload this fileresolver=" + fullname + " has an error", e);
        }
    }

    private Class scan(String fileResolverClassname) {
        Class<?> clazz = classPathScanner.scan(fileResolverClassname);
        if (clazz == null) {
            clazz = fileSystemScanner.scan(fileResolverClassname);
        }

        return clazz;
    }

    // =============setter / getter===============

    public void setClassPathScanner(ClassScanner classPathScanner) {
        this.classPathScanner = classPathScanner;
    }

    public void setFileSystemScanner(ClassScanner fileSystemScanner) {
        this.fileSystemScanner = fileSystemScanner;
    }

    public ClassScanner getFileSystemScanner() {
        return fileSystemScanner;
    }

    public void setJdkCompiler(JdkCompiler jdkCompiler) {
        this.jdkCompiler = jdkCompiler;
    }

}
