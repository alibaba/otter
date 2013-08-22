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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import com.alibaba.otter.shared.common.utils.compile.impl.JdkCompileTask;

public final class JavaFileObjectImpl extends SimpleJavaFileObject {

    // If kind == CLASS, this stores byte code from openOutputStream
    private ByteArrayOutputStream byteCode = new ByteArrayOutputStream();

    // if kind == SOURCE, this contains the source text
    private final CharSequence    source;

    public JavaFileObjectImpl(final String baseName, final CharSequence source){
        super(JdkCompileTask.toURI(baseName + JdkCompileTask.JAVA_EXTENSION), Kind.SOURCE);
        this.source = source;
    }

    public JavaFileObjectImpl(final String name, final Kind kind){
        super(JdkCompileTask.toURI(name), kind);
        source = null;
    }

    public JavaFileObjectImpl(URI uri, Kind kind){
        super(uri, kind);
        source = null;
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws UnsupportedOperationException {
        if (source == null) {
            throw new UnsupportedOperationException();
        }

        return source;
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(getByteCode());
    }

    @Override
    public OutputStream openOutputStream() {
        return byteCode;
    }

    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }
}
