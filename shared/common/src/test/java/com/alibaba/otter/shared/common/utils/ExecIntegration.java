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
