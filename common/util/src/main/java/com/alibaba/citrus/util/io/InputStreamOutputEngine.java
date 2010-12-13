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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 将数据从任意<code>InputStream</code>复制到<code>FilterOutputStream</code>的输出引擎.
 * 本代码移植自IBM developer works文章：
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
public class InputStreamOutputEngine implements OutputEngine {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private InputStream in;
    private OutputStreamFactory factory;
    private byte[] buffer;
    private OutputStream out;

    public InputStreamOutputEngine(InputStream in, OutputStreamFactory factory) {
        this(in, factory, DEFAULT_BUFFER_SIZE);
    }

    public InputStreamOutputEngine(InputStream in, OutputStreamFactory factory, int bufferSize) {
        this.in = in;
        this.factory = factory == null ? DEFAULT_OUTPUT_STREAM_FACTORY : factory;
        buffer = new byte[bufferSize];
    }

    public void open(OutputStream out) throws IOException {
        if (this.out != null) {
            throw new IOException("Already initialized");
        } else {
            this.out = factory.getOutputStream(out);
        }
    }

    public void execute() throws IOException {
        if (out == null) {
            throw new IOException("Not yet initialized");
        } else {
            int amount = in.read(buffer);

            if (amount < 0) {
                out.close();
            } else {
                out.write(buffer, 0, amount);
            }
        }
    }

    public void close() throws IOException {
        in.close();
    }
}
