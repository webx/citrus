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
 * 非同步的<code>ByteArrayOutputStream</code>替换方案, 执行<code>toByteArray()</code>
 * 方法时返回的是只读的内部字节数组, 避免了没有必要的字节复制. 本代码移植自IBM developer works文章：
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
public class ByteArrayOutputStream extends OutputStream {
    private static final int DEFAULT_INITIAL_BUFFER_SIZE = 8192;

    // internal buffer
    private byte[] buffer;
    private int index;
    private int capacity;

    // is the stream closed?
    private boolean closed;

    // is the buffer shared?
    private boolean shared;

    public ByteArrayOutputStream() {
        this(DEFAULT_INITIAL_BUFFER_SIZE);
    }

    public ByteArrayOutputStream(int initialBufferSize) {
        capacity = initialBufferSize;
        buffer = new byte[capacity];
    }

    @Override
    public void write(int datum) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        } else {
            if (index >= capacity) {
                // expand the internal buffer
                capacity = capacity * 2 + 1;

                byte[] tmp = new byte[capacity];

                System.arraycopy(buffer, 0, tmp, 0, index);
                buffer = tmp;

                // the new buffer is not shared
                shared = false;
            }

            // store the byte
            buffer[index++] = (byte) datum;
        }
    }

    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        if (data == null) {
            throw new NullPointerException();
        } else if (offset < 0 || offset + length > data.length || length < 0) {
            throw new IndexOutOfBoundsException();
        } else if (closed) {
            throw new IOException("Stream closed");
        } else {
            if (index + length > capacity) {
                // expand the internal buffer
                capacity = capacity * 2 + length;

                byte[] tmp = new byte[capacity];

                System.arraycopy(buffer, 0, tmp, 0, index);
                buffer = tmp;

                // the new buffer is not shared
                shared = false;
            }

            // copy in the subarray
            System.arraycopy(data, offset, buffer, index, length);
            index += length;
        }
    }

    @Override
    public void close() {
        closed = true;
    }

    public void writeTo(OutputStream out) throws IOException {
        // write the internal buffer directly
        out.write(buffer, 0, index);
    }

    public ByteArray toByteArray() {
        shared = true;
        return new ByteArray(buffer, 0, index);
    }

    public InputStream toInputStream() {
        // return a stream reading from the shared internal buffer
        shared = true;
        return new ByteArrayInputStream(buffer, 0, index);
    }

    public void reset() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        } else {
            if (shared) {
                // create a new buffer if it is shared
                buffer = new byte[capacity];
                shared = false;
            }

            // reset index
            index = 0;
        }
    }
}
