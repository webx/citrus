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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 测试<code>StreamUtil</code>类。
 * 
 * @author Michael Zhou
 */
public class StreamUtilTests {
    private static byte[] bytes;
    private static String chars;

    @BeforeClass
    public static void init() throws IOException {
        chars = "中华人民共和国abcdefghijklmnopqrstuvwxyz";
        bytes = chars.getBytes("GBK");
    }

    @Test
    public void io_bytes() throws IOException {
        test(new CB() {
            public void run(boolean close) throws IOException {
                BAIS in = new BAIS(bytes);
                BAOS out = new BAOS();

                StreamUtil.io(in, out, close, close);

                assertArrayEquals(bytes, out.toByteArray());

                in.assertClosed(close);
                out.assertClosed(close);
            }
        });
    }

    @Test
    public void io_chars() throws IOException {
        test(new CB() {
            public void run(boolean close) throws IOException {
                SR in = new SR(chars);
                SW out = new SW();

                StreamUtil.io(in, out, close, close);

                assertEquals(chars, out.toString());

                in.assertClosed(close);
                out.assertClosed(close);
            }
        });
    }

    @Test
    public void readText() throws IOException {
        test(new CB() {
            public void run(boolean close) throws IOException {
                SR sr = new SR(chars);
                assertEquals(chars, StreamUtil.readText(sr, close));
                sr.assertClosed(close);

                BAIS bais = new BAIS(bytes);
                assertEquals(chars, StreamUtil.readText(bais, "GBK", close));
                bais.assertClosed(close);
            }
        });
    }

    @Test
    public void readBytes() throws IOException {
        test(new CB() {
            public void run(boolean close) throws IOException {
                BAIS bais = new BAIS(bytes);
                assertArrayEquals(bytes, StreamUtil.readBytes(bais, close).toByteArray());
                bais.assertClosed(close);
            }
        });
    }

    @Test
    public void writeText() throws IOException {
        test(new CB() {
            public void run(boolean close) throws IOException {
                SW writer = new SW();

                StreamUtil.writeText(chars, writer, close);

                assertEquals(chars, writer.toString());
                writer.assertClosed(close);

                BAOS stream = new BAOS();

                StreamUtil.writeText(chars, stream, "GBK", close);

                assertArrayEquals(bytes, stream.toByteArray());
                stream.assertClosed(close);
            }
        });
    }

    @Test
    public void writeBytes() throws IOException {
        test(new CB() {
            public void run(boolean close) throws IOException {
                BAOS baos = new BAOS();

                StreamUtil.writeBytes(bytes, baos, close);

                assertArrayEquals(bytes, baos.toByteArray());
                baos.assertClosed(close);

                baos = new BAOS();

                StreamUtil.writeBytes(new ByteArray(bytes), baos, close);

                assertArrayEquals(bytes, baos.toByteArray());
                baos.assertClosed(close);
            }
        });
    }

    private void test(CB cb) throws IOException {
        boolean close = false;

        for (int i = 0; i < 2; i++, close = !close) {
            cb.run(close);
        }
    }

    private interface CB {
        void run(boolean close) throws IOException;
    }

    private class BAIS extends ByteArrayInputStream {
        private boolean closed;

        public BAIS(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() {
            closed = true;
        }

        public void assertClosed(boolean expectedClosed) {
            assertEquals(expectedClosed, closed);
        }
    }

    private class BAOS extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        public void assertClosed(boolean expectedClosed) {
            assertEquals(expectedClosed, closed);
        }
    }

    private class SR extends StringReader {
        private boolean closed;

        public SR(String buf) {
            super(buf);
        }

        @Override
        public void close() {
            closed = true;
        }

        public void assertClosed(boolean expectedClosed) {
            assertEquals(expectedClosed, closed);
        }
    }

    private class SW extends StringWriter {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        public void assertClosed(boolean expectedClosed) {
            assertEquals(expectedClosed, closed);
        }
    }
}
