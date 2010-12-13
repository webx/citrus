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
 * 从<code>OutputEngine</code>取得数据的输入流, 作为<code>PipedInputStream</code>的替换方案,
 * 这个类提供了极高的性能. 本代码移植自IBM developer works文章：
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
public class OutputEngineInputStream extends InputStream {
    private static final int DEFAULT_INITIAL_BUFFER_SIZE = 8192;
    private OutputEngine engine;
    private byte[] buffer;
    private int index;
    private int limit;
    private int capacity;
    private boolean closed;
    private boolean eof;

    public OutputEngineInputStream(OutputEngine engine) throws IOException {
        this(engine, DEFAULT_INITIAL_BUFFER_SIZE);
    }

    public OutputEngineInputStream(OutputEngine engine, int initialBufferSize) throws IOException {
        this.engine = engine;
        capacity = initialBufferSize;
        buffer = new byte[capacity];
        engine.open(new OutputStreamImpl());
    }

    private byte[] one = new byte[1];

    @Override
    public int read() throws IOException {
        int amount = read(one, 0, 1);

        return amount < 0 ? -1 : one[0] & 0xff;
    }

    @Override
    public int read(byte[] data, int offset, int length) throws IOException {
        if (data == null) {
            throw new NullPointerException();
        } else if (offset < 0 || offset + length > data.length || length < 0) {
            throw new IndexOutOfBoundsException();
        } else if (closed) {
            throw new IOException("Stream closed");
        } else {
            while (index >= limit) {
                if (eof) {
                    return -1;
                }

                engine.execute();
            }

            if (limit - index < length) {
                length = limit - index;
            }

            System.arraycopy(buffer, index, data, offset, length);
            index += length;
            return length;
        }
    }

    @Override
    public long skip(long amount) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        } else if (amount <= 0) {
            return 0;
        } else {
            while (index >= limit) {
                if (eof) {
                    return 0;
                }

                engine.execute();
            }

            if (limit - index < amount) {
                amount = limit - index;
            }

            index += (int) amount;
            return amount;
        }
    }

    @Override
    public int available() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        } else {
            return limit - index;
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            engine.close();
        }
    }

    private void writeImpl(byte[] data, int offset, int length) {
        if (index >= limit) {
            index = limit = 0;
        }

        if (limit + length > capacity) {
            capacity = capacity * 2 + length;

            byte[] tmp = new byte[capacity];

            System.arraycopy(buffer, index, tmp, 0, limit - index);
            buffer = tmp;
            limit -= index;
            index = 0;
        }

        System.arraycopy(data, offset, buffer, limit, length);
        limit += length;
    }

    private class OutputStreamImpl extends OutputStream {
        @Override
        public void write(int datum) throws IOException {
            one[0] = (byte) datum;
            write(one, 0, 1);
        }

        @Override
        public void write(byte[] data, int offset, int length) throws IOException {
            if (data == null) {
                throw new NullPointerException();
            } else if (offset < 0 || offset + length > data.length || length < 0) {
                throw new IndexOutOfBoundsException();
            } else if (eof) {
                throw new IOException("Stream closed");
            } else {
                writeImpl(data, offset, length);
            }
        }

        @Override
        public void close() {
            eof = true;
        }
    }
}
