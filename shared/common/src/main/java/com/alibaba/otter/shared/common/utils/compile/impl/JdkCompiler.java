package com.alibaba.otter.shared.common.utils.compile.impl;

import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import com.alibaba.otter.shared.common.utils.compile.JavaSourceCompiler;
import com.alibaba.otter.shared.common.utils.compile.exception.CompileExprException;
import com.alibaba.otter.shared.common.utils.compile.exception.JdkCompileException;
import com.alibaba.otter.shared.common.utils.compile.model.JavaSource;
import com.alibaba.otter.shared.common.utils.compile.model.JdkCompilerClassLoader;

public class JdkCompiler implements JavaSourceCompiler {

    private List<String> options;

    public JdkCompiler(){
        options = new ArrayList<String>();
        options.add("-target");
        options.add("1.6");
    }

    public Class compile(String sourceString) {
        JavaSource source = new JavaSource(sourceString);
        return compile(source);
    }

    public Class compile(JavaSource javaSource) {
        try {

            final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
            JdkCompileTask compileTask = new JdkCompileTask(
                                                            new JdkCompilerClassLoader(this.getClass().getClassLoader()),
                                                            options);
            String fullName = javaSource.getPackageName() + "." + javaSource.getClassName();
            Class newClass = compileTask.compile(fullName, javaSource.getSource(), errs);
            return newClass;
        } catch (JdkCompileException ex) {
            DiagnosticCollector<JavaFileObject> diagnostics = ex.getDiagnostics();
            throw new CompileExprException("compile error, source : \n" + javaSource + ", "
                                           + diagnostics.getDiagnostics(), ex);
        } catch (Exception ex) {
            throw new CompileExprException("compile error, source : \n" + javaSource, ex);
        }

    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

}
