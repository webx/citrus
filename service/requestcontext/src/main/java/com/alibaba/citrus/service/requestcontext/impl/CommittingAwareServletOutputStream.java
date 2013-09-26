/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.citrus.service.requestcontext.impl;

import java.io.IOException;
import javax.servlet.ServletOutputStream;

import com.alibaba.citrus.util.internal.Servlet3Util.Servlet3OutputStream;

class CommittingAwareServletOutputStream extends Servlet3OutputStream {
    private final HeaderCommitter committer;

    public CommittingAwareServletOutputStream(HeaderCommitter committer, ServletOutputStream originalStream) {
        super(originalStream);
        this.committer = committer;
    }

    @Override
    public void print(String s) throws IOException {
        committer.commitHeaders();
        originalStream.print(s);
    }

    @Override
    public void print(boolean b) throws IOException {
        committer.commitHeaders();
        originalStream.print(b);
    }

    @Override
    public void print(char c) throws IOException {
        committer.commitHeaders();
        originalStream.print(c);
    }

    @Override
    public void print(int i) throws IOException {
        committer.commitHeaders();
        originalStream.print(i);
    }

    @Override
    public void print(long l) throws IOException {
        committer.commitHeaders();
        originalStream.print(l);
    }

    @Override
    public void print(float f) throws IOException {
        committer.commitHeaders();
        originalStream.print(f);
    }

    @Override
    public void print(double d) throws IOException {
        committer.commitHeaders();
        originalStream.print(d);
    }

    @Override
    public void println() throws IOException {
        committer.commitHeaders();
        originalStream.println();
    }

    @Override
    public void println(String s) throws IOException {
        committer.commitHeaders();
        originalStream.println(s);
    }

    @Override
    public void println(boolean b) throws IOException {
        committer.commitHeaders();
        originalStream.println(b);
    }

    @Override
    public void println(char c) throws IOException {
        committer.commitHeaders();
        originalStream.println(c);
    }

    @Override
    public void println(int i) throws IOException {
        committer.commitHeaders();
        originalStream.println(i);
    }

    @Override
    public void println(long l) throws IOException {
        committer.commitHeaders();
        originalStream.println(l);
    }

    @Override
    public void println(float f) throws IOException {
        committer.commitHeaders();
        originalStream.println(f);
    }

    @Override
    public void println(double d) throws IOException {
        committer.commitHeaders();
        originalStream.println(d);
    }

    @Override
    public void write(int b) throws IOException {
        committer.commitHeaders();
        originalStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        committer.commitHeaders();
        originalStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        committer.commitHeaders();
        originalStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        committer.commitHeaders();
        originalStream.flush();
    }

    @Override
    public void close() throws IOException {
        originalStream.close();
    }

    @Override
    public String toString() {
        return originalStream.toString();
    }
}
