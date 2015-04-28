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

package com.alibaba.otter.shared.common.utils.compile.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.alibaba.otter.shared.common.utils.compile.exception.JdkCompileException;
import com.alibaba.otter.shared.common.utils.compile.model.JavaFileManagerImpl;
import com.alibaba.otter.shared.common.utils.compile.model.JavaFileObjectImpl;
import com.alibaba.otter.shared.common.utils.compile.model.JdkCompilerClassLoader;

public class JdkCompileTask<T> {

    public static final String                  JAVA_EXTENSION = ".java";

    private final JdkCompilerClassLoader        classLoader;

    private final JavaCompiler                  compiler;

    private final List<String>                  options;

    private DiagnosticCollector<JavaFileObject> diagnostics;

    private JavaFileManagerImpl                 javaFileManager;

    public JdkCompileTask(JdkCompilerClassLoader classLoader, Iterable<String> options){
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Cannot find the system Java compiler. "
                                            + "Check that your class path includes tools.jar");
        }

        this.classLoader = classLoader;
        ClassLoader loader = classLoader.getParent();
        diagnostics = new DiagnosticCollector<JavaFileObject>();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        if (loader instanceof URLClassLoader
            && (!loader.getClass().getName().equalsIgnoreCase("sun.misc.Launcher$AppClassLoader"))) {
            try {
                @SuppressWarnings("resource")
                URLClassLoader urlClassLoader = (URLClassLoader) loader;
                List<File> path = new ArrayList<File>();
                for (URL url : urlClassLoader.getURLs()) {
                    File file = new File(url.getFile());
                    path.add(file);
                }

                fileManager.setLocation(StandardLocation.CLASS_PATH, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        javaFileManager = new JavaFileManagerImpl(fileManager, classLoader);
        this.options = new ArrayList<String>();
        if (options != null) { // make a save copy of input options
            for (String option : options) {
                this.options.add(option);
            }
        }
    }

    public synchronized Class compile(final String className, final CharSequence javaSource,
                                      final DiagnosticCollector<JavaFileObject> diagnosticsList)
                                                                                                throws JdkCompileException,
                                                                                                ClassCastException {
        if (diagnosticsList != null) {
            diagnostics = diagnosticsList;
        } else {
            diagnostics = new DiagnosticCollector<JavaFileObject>();
        }

        Map<String, CharSequence> classes = new HashMap<String, CharSequence>(1);
        classes.put(className, javaSource);

        Map<String, Class> compiled = compile(classes, diagnosticsList);
        Class newClass = compiled.get(className);
        return newClass;
    }

    public synchronized Map<String, Class> compile(final Map<String, CharSequence> classes,
                                                   final DiagnosticCollector<JavaFileObject> diagnosticsList)
                                                                                                             throws JdkCompileException {
        Map<String, Class> compiled = new HashMap<String, Class>();

        List<JavaFileObject> sources = new ArrayList<JavaFileObject>();
        for (Entry<String, CharSequence> entry : classes.entrySet()) {
            String qualifiedClassName = entry.getKey();
            CharSequence javaSource = entry.getValue();
            if (javaSource != null) {
                final int dotPos = qualifiedClassName.lastIndexOf('.');
                final String className = dotPos == -1 ? qualifiedClassName : qualifiedClassName.substring(dotPos + 1);
                final String packageName = dotPos == -1 ? "" : qualifiedClassName.substring(0, dotPos);
                final JavaFileObjectImpl source = new JavaFileObjectImpl(className, javaSource);
                sources.add(source);
                javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH,
                    packageName,
                    className + JAVA_EXTENSION,
                    source);
            }
        }

        // Get a CompliationTask from the compiler and compile the sources
        final CompilationTask task = compiler.getTask(null, javaFileManager, diagnostics, options, null, sources);
        final Boolean result = task.call();
        if (result == null || !result.booleanValue()) {
            throw new JdkCompileException("Compilation failed.", classes.keySet(), diagnostics);
        }

        try {
            // For each class name in the inpput map, get its compiled class and
            // put it in the output map
            for (String qualifiedClassName : classes.keySet()) {
                final Class<T> newClass = loadClass(qualifiedClassName);
                compiled.put(qualifiedClassName, (Class<?>) newClass);
            }

            return compiled;
        } catch (ClassNotFoundException e) {
            throw new JdkCompileException(classes.keySet(), e, diagnostics);
        } catch (IllegalArgumentException e) {
            throw new JdkCompileException(classes.keySet(), e, diagnostics);
        } catch (SecurityException e) {
            throw new JdkCompileException(classes.keySet(), e, diagnostics);
        }
    }

    public Class<T> loadClass(final String qualifiedClassName) throws ClassNotFoundException {
        return (Class<T>) classLoader.loadClass(qualifiedClassName);
    }

    public static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassLoader getClassLoader() {
        return javaFileManager.getClassLoader();
    }
}
