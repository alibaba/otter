package com.alibaba.otter.shared.common.utils;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.utils.cmd.Exec;
import com.alibaba.otter.shared.common.utils.cmd.Exec.Result;

/**
 * Exec 单元测试
 * 
 * @author jianghang 2011-9-27 上午10:45:14
 * @version 4.0.0
 */
public class ExecIntegration extends BaseOtterTest {

    @Test
    public void testSample() {
        try {
            Result result = Exec.execute("dir");// liunx和windows度支持的命令
            want.object(result).notNull();
            want.string(result.getStdout()).notBlank();
        } catch (Exception e) {
            want.fail();
        }

    }

    @Test
    public void testAppender() {
        String tmp = System.getProperty("java.io.tmpdir", "/tmp");
        try {
            Result result = Exec.execute("dir", tmp + "/exec.log");// liunx和windows度支持的命令
            want.object(result).notNull();
            want.string(result.getStdout()).notBlank();
        } catch (Exception e) {
            e.printStackTrace();
            want.fail();
        }

    }

    @Test
    public void testInput() {
        String tmp = System.getProperty("java.io.tmpdir", "/tmp");
        try {
            Result result = Exec.execute("dir", tmp + "/exec.log", tmp.getBytes());// liunx和windows度支持的命令
            want.object(result).notNull();
            want.string(result.getStdout()).notBlank();
        } catch (Exception e) {
            want.fail();
        }

    }

    @Test
    public void testUserDir() {
        String tmp = System.getProperty("java.io.tmpdir", "/tmp");
        try {
            Result result = Exec.execute("dir", tmp + "/exec.log", tmp.getBytes(), new File(tmp));// liunx和windows度支持的命令
            want.object(result).notNull();
            want.string(result.getStdout()).notBlank();
        } catch (Exception e) {
            want.fail();
        }

    }

    @AfterMethod
    public void tearDown() {
        String tmp = System.getProperty("java.io.tmpdir", "/tmp");
        new File(tmp + "/exec.log").deleteOnExit();
    }
}
