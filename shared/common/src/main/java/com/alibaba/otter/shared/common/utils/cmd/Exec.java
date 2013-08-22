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

package com.alibaba.otter.shared.common.utils.cmd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 外部脚本调用工具
 * 
 * <pre>
 * example:
 *  Result result = Exec.executre("pwd");
 *  String dir = result.getStdout();
 * </pre>
 * 
 * @author jianghang 2011-9-27 上午10:19:19
 * @version 4.0.0
 */
public class Exec {

    private static final Logger logger = LoggerFactory.getLogger(Exec.class);

    public static Result execute(String cmd) throws Exception {
        return execute(cmd, null, null, null);
    }

    public static Result execute(String cmd, String outputLog) throws Exception {
        return execute(cmd, outputLog, null, null);
    }

    public static Result execute(String cmd, String outputLog, byte[] input) throws Exception {
        return execute(cmd, outputLog, input, null);
    }

    public static Result execute(String cmd, String outputLog, byte[] input, File workingDir) throws Exception {
        // 注意单command命令和多command命令使用方式有所不同，不可完全兼容
        Process process = Runtime.getRuntime().exec(cmd, null, workingDir);
        return execute(process, cmd, outputLog, input, workingDir);
    }

    public static Result execute(String[] cmds) throws Exception {
        return execute(cmds, null, null, null);
    }

    public static Result execute(String[] cmds, String outputLog) throws Exception {
        return execute(cmds, outputLog, null, null);
    }

    public static Result execute(String[] cmds, String outputLog, byte[] input) throws Exception {
        return execute(cmds, outputLog, input, null);
    }

    public static Result execute(String[] cmds, String outputLog, byte[] input, File workingDir) throws Exception {
        // 注意单command命令和多command命令使用方式有所不同，不可完全兼容
        Process process = Runtime.getRuntime().exec(cmds, null, workingDir);
        return execute(process, StringUtils.join(cmds, " "), outputLog, input, workingDir);
    }

    public static Result execute(Process process, String cmd, String outputLog, byte[] input, File workingDir)
                                                                                                              throws Exception {
        // 处理输入参数流
        Thread inputThread = new InputPumper((input == null) ? new byte[] {} : input, process.getOutputStream());
        StreamCollector stderr = null;
        StreamCollector stdout = null;
        FileOutputStream fileOutput = null;
        StreamAppender outputLogger = null;
        String errString = null;
        String outString = null;

        try {
            if (outputLog == null) {
                stdout = new StreamCollector(process.getInputStream());
                stderr = new StreamCollector(process.getErrorStream());
                stdout.start();
                stderr.start();
            } else {
                errString = "stderr output redirected to file " + outputLog;
                outString = "stdout output redirected to file " + outputLog;
                fileOutput = new FileOutputStream(outputLog);
                outputLogger = new StreamAppender(fileOutput);
                outputLogger.writeInput(process.getErrorStream(), process.getInputStream());
            }

            inputThread.start();

            final int exitCode = process.waitFor();

            inputThread.join();

            if (outputLogger != null) {
                outputLogger.finish();
            }

            if (stdout != null) {
                stdout.join();
                outString = stdout.toString();
            }

            if (stderr != null) {
                stderr.join();
                errString = stderr.toString();
            }

            return new Result(cmd.toString(), outString, errString, exitCode);
        } finally {
            IOUtils.closeQuietly(fileOutput);

            if (process != null) {
                // evitons http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6462165
                process.getInputStream().close();
                process.getOutputStream().close();
                process.getErrorStream().close();
                process.destroy();
            }
        }
    }

    // 参数输入处理
    private static class InputPumper extends Thread {

        private final InputStream  input;
        private final OutputStream output;

        InputPumper(byte[] input, OutputStream output){
            this.output = output;
            this.input = new ByteArrayInputStream(input);
            this.setName("Input Pumper");
            this.setDaemon(true);
        }

        public void run() {
            try {
                IOUtils.copy(input, output);
            } catch (IOException e) {
                logger.error("", e);
            } finally {
                IOUtils.closeQuietly(output);
            }
        }
    }

    public static class Result {

        private final String cmd;
        private final int    exitCode;
        private final String stderr;
        private final String stdout;

        private Result(String cmd, String stdout, String stderr, int exitCode){
            this.cmd = cmd;
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
        }

        public String getStderr() {
            return stderr;
        }

        public String getStdout() {
            return stdout;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String toString() {
            return String.format("Command: %s%sexit code:%s%sstdout:%s%s%sstderr:%s%s%s", cmd,
                                 SystemUtils.LINE_SEPARATOR, exitCode, SystemUtils.LINE_SEPARATOR,
                                 SystemUtils.LINE_SEPARATOR, stdout, SystemUtils.LINE_SEPARATOR,
                                 SystemUtils.LINE_SEPARATOR, stderr, SystemUtils.LINE_SEPARATOR);
        }
    }
}
