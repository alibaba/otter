package com.alibaba.otter.shared.common.utils;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.utils.compile.impl.JdkCompiler;

public class JavaSourceCompilerTest extends BaseOtterTest {

    @Test
    public void testSimple() {

        String javasource = null;
        try {
            List<String> lines = IOUtils.readLines(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                                                                                                                      "compiler.txt"));
            javasource = StringUtils.join(lines, "\n");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        JdkCompiler compiler = new JdkCompiler();

        Class<?> clazz = compiler.compile(javasource);
        System.out.println(clazz.getName());
    }

}
