package com.alibaba.otter.shared.common.utils.compile;

import com.alibaba.otter.shared.common.utils.compile.model.JavaSource;

public interface JavaSourceCompiler {

    Class compile(String sourceString);

    Class compile(JavaSource javaSource);

}
