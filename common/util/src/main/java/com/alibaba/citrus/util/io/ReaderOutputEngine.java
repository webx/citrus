/*
 * Copyright 2010 Alibaba Group Holding Limited.
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
 *
 */
package com.alibaba.citrus.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * 将数据从任意<code>Reader</code>复制到<code>OutputStreamWriter</code>的输出引擎. 本代码移植自IBM
 * developer works文章：
 * <ul>
 * <li><a
 * href="http://www.ibm.com/developerworks/cn/java/j-io1/index.shtml">彻底转变流，第 1
 * 部分：从输出流中读取</a>
 * <li><a
 * href="http://www.ibm.com/developerworks/cn/java/j-io2/index.shtml">彻底转变流，第 2
 * 部分：优化 Java 内部 I/O</a>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class ReaderOutputEngine implements OutputEngine {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private Reader reader;
    private String encoding;
    private OutputStreamFactory factory;
    private char[] buffer;
    private Writer writer;

    public ReaderOutputEngine(Reader reader) {
        this(reader, null, null, DEFAULT_BUFFER_SIZE);
    }

    public ReaderOutputEngine(Reader reader, OutputStreamFactory factory) {
        this(reader, factory, null, DEFAULT_BUFFER_SIZE);
    }

    public ReaderOutputEngine(Reader reader, OutputStreamFactory factory, String encoding) {
        this(reader, factory, encoding, DEFAULT_BUFFER_SIZE);
    }

    public ReaderOutputEngine(Reader reader, OutputStreamFactory factory, String encoding, int bufferSize) {
        this.reader = reader;
        this.encoding = encoding;
        this.factory = factory == null ? DEFAULT_OUTPUT_STREAM_FACTORY : factory;
        buffer = new char[bufferSize];
    }

    public void open(OutputStream out) throws IOException {
        if (writer != null) {
            throw new IOException("Already initialized");
        } else {
            writer = encoding == null ? new OutputStreamWriter(factory.getOutputStream(out)) : new OutputStreamWriter(
                    factory.getOutputStream(out), encoding);
        }
    }

    public void execute() throws IOException {
        if (writer == null) {
            throw new IOException("Not yet initialized");
        } else {
            int amount = reader.read(buffer);

            if (amount < 0) {
                writer.close();
            } else {
                writer.write(buffer, 0, amount);
            }
        }
    }

    public void close() throws IOException {
        reader.close();
    }
}
