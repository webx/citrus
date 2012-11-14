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

import java.io.PrintWriter;
import java.util.Locale;

class CommittingAwarePrintWriter extends PrintWriter {
    private final HeaderCommitter committer;
    private final PrintWriter     originalWriter;

    CommittingAwarePrintWriter(HeaderCommitter committer, PrintWriter originalWriter) {
        super(originalWriter);
        this.committer = committer;
        this.originalWriter = originalWriter;
    }

    @Override
    public void flush() {
        committer.commitHeaders();
        originalWriter.flush();
    }

    @Override
    public void write(int c) {
        committer.commitHeaders();
        originalWriter.write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        committer.commitHeaders();
        originalWriter.write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {
        committer.commitHeaders();
        originalWriter.write(buf);
    }

    @Override
    public void write(String s, int off, int len) {
        committer.commitHeaders();
        originalWriter.write(s, off, len);
    }

    @Override
    public void write(String s) {
        committer.commitHeaders();
        originalWriter.write(s);
    }

    @Override
    public void print(boolean b) {
        committer.commitHeaders();
        originalWriter.print(b);
    }

    @Override
    public void print(char c) {
        committer.commitHeaders();
        originalWriter.print(c);
    }

    @Override
    public void print(int i) {
        committer.commitHeaders();
        originalWriter.print(i);
    }

    @Override
    public void print(long l) {
        committer.commitHeaders();
        originalWriter.print(l);
    }

    @Override
    public void print(float f) {
        committer.commitHeaders();
        originalWriter.print(f);
    }

    @Override
    public void print(double d) {
        committer.commitHeaders();
        originalWriter.print(d);
    }

    @Override
    public void print(char[] s) {
        committer.commitHeaders();
        originalWriter.print(s);
    }

    @Override
    public void print(String s) {
        committer.commitHeaders();
        originalWriter.print(s);
    }

    @Override
    public void print(Object obj) {
        committer.commitHeaders();
        originalWriter.print(obj);
    }

    @Override
    public void println() {
        committer.commitHeaders();
        originalWriter.println();
    }

    @Override
    public void println(boolean x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(char x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(int x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(long x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(float x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(double x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(char[] x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(String x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public void println(Object x) {
        committer.commitHeaders();
        originalWriter.println(x);
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        committer.commitHeaders();
        return originalWriter.printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        committer.commitHeaders();
        return originalWriter.printf(l, format, args);
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        committer.commitHeaders();
        return originalWriter.format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        committer.commitHeaders();
        return originalWriter.format(l, format, args);
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        committer.commitHeaders();
        return originalWriter.append(csq);
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        committer.commitHeaders();
        return originalWriter.append(csq, start, end);
    }

    @Override
    public PrintWriter append(char c) {
        committer.commitHeaders();
        return originalWriter.append(c);
    }

    @Override
    public boolean checkError() {
        committer.commitHeaders();
        return originalWriter.checkError();
    }

    @Override
    public void close() {
        originalWriter.close();
    }

    @Override
    public String toString() {
        return originalWriter.toString();
    }
}
