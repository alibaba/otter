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
