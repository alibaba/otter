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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaFileObject;

public final class JdkCompilerClassLoader extends ClassLoader {

    private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();

    public JdkCompilerClassLoader(ClassLoader parentClassLoader){
        super(parentClassLoader);
    }

    public Collection<JavaFileObject> files() {
        return Collections.unmodifiableCollection(classes.values());
    }

    public void clearCache() {
        this.classes.clear();
    }

    public JavaFileObject getJavaFileObject(String qualifiedClassName) {
        return classes.get(qualifiedClassName);
    }

    protected synchronized Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
        JavaFileObject file = classes.get(qualifiedClassName);
        if (file != null) {
            byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
            return defineClass(qualifiedClassName, bytes, 0, bytes.length);
        }

        try {
            return Class.forName(qualifiedClassName);
        } catch (ClassNotFoundException nf) {
            // Ignore and fall through
        }

        try {
            return Thread.currentThread().getContextClassLoader().loadClass(qualifiedClassName);
        } catch (ClassNotFoundException nf) {
            // Ignore and fall through
        }

        return super.findClass(qualifiedClassName);
    }

    public void add(String qualifiedClassName, final JavaFileObject javaFile) {
        classes.put(qualifiedClassName, javaFile);
    }

    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        try {
            Class c = findClass(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }

                return c;
            }
        } catch (ClassNotFoundException e) {
            // Ignore and fall through
        }

        return super.loadClass(name, resolve);
    }

    public InputStream getResourceAsStream(final String name) {
        if (name.endsWith(".class")) {
            String qualifiedClassName = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);

            if (file != null) {
                return new ByteArrayInputStream(file.getByteCode());
            }
        }

        return super.getResourceAsStream(name);
    }

}
