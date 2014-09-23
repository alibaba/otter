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

package com.alibaba.otter.shared.common.utils.compile.model;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

import com.alibaba.otter.shared.common.utils.compile.impl.JdkCompileTask;

public class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

    private final JdkCompilerClassLoader   classLoader;

    private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();

    public JavaFileManagerImpl(JavaFileManager fileManager, JdkCompilerClassLoader classLoader){
        super(fileManager);
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        FileObject o = fileObjects.get(uri(location, packageName, relativeName));

        if (o != null) {
            return o;
        }

        return super.getFileForInput(location, packageName, relativeName);
    }

    public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject file) {
        fileObjects.put(uri(location, packageName, relativeName), file);
    }

    private URI uri(Location location, String packageName, String relativeName) {
        return JdkCompileTask.toURI(location.getName() + '/' + packageName + '/' + relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject outputFile)
                                                                                                                         throws IOException {
        JavaFileObject file = new JavaFileObjectImpl(qualifiedName, kind);
        classLoader.add(qualifiedName, file);
        return file;
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return classLoader;
    }

    @Override
    public String inferBinaryName(Location loc, JavaFileObject file) {
        if (file instanceof JavaFileObjectImpl) {
            return file.getName();
        }

        return super.inferBinaryName(loc, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
                                                                                                                 throws IOException {
        Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        List<URL> urlList = new ArrayList<URL>();
        Enumeration<URL> e = contextClassLoader.getResources("com");
        while (e.hasMoreElements()) {
            urlList.add(e.nextElement());
        }

        ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();

        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }

            files.addAll(classLoader.files());
        } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }
        }

        for (JavaFileObject file : result) {
            files.add(file);
        }

        return files;
    }
}
