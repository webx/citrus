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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 基于流的工具类。
 * 
 * @author Michael Zhou
 */
public class StreamUtil {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 从输入流读取内容，写入到输出流中。
     */
    public static void io(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
        int bufferSize = DEFAULT_BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];
        int amount;

        try {
            while ((amount = in.read(buffer)) >= 0) {
                out.write(buffer, 0, amount);
            }

            out.flush();
        } finally {
            if (closeIn) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (closeOut) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 从输入流读取内容，写入到输出流中。
     */
    public static void io(Reader in, Writer out, boolean closeIn, boolean closeOut) throws IOException {
        int bufferSize = DEFAULT_BUFFER_SIZE >> 1;
        char[] buffer = new char[bufferSize];
        int amount;

        try {
            while ((amount = in.read(buffer)) >= 0) {
                out.write(buffer, 0, amount);
            }

            out.flush();
        } finally {
            if (closeIn) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (closeOut) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 将指定输入流的所有文本全部读出到一个字符串中。
     */
    public static String readText(InputStream in, String charset, boolean closeIn) throws IOException {
        Reader reader = charset == null ? new InputStreamReader(in) : new InputStreamReader(in, charset);

        return readText(reader, closeIn);
    }

    /**
     * 将指定<code>Reader</code>的所有文本全部读出到一个字符串中。
     */
    public static String readText(Reader in, boolean closeIn) throws IOException {
        StringWriter out = new StringWriter();

        io(in, out, closeIn, true);

        return out.toString();
    }

    /**
     * 将指定<code>InputStream</code>的所有内容全部读出到一个byte数组中。
     */
    public static ByteArray readBytes(InputStream in, boolean closeIn) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        io(in, out, closeIn, true);

        return out.toByteArray();
    }

    /**
     * 将字符串写入到指定输出流中。
     */
    public static void writeText(CharSequence chars, OutputStream out, String charset, boolean closeOut)
            throws IOException {
        Writer writer = charset == null ? new OutputStreamWriter(out) : new OutputStreamWriter(out, charset);

        writeText(chars, writer, closeOut);
    }

    /**
     * 将字符串写入到指定<code>Writer</code>中。
     */
    public static void writeText(CharSequence chars, Writer out, boolean closeOut) throws IOException {
        try {
            out.write(chars.toString());
            out.flush();
        } finally {
            if (closeOut) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 将byte数组写入到指定<code>OutputStream</code>中。
     */
    public static void writeBytes(byte[] bytes, OutputStream out, boolean closeOut) throws IOException {
        writeBytes(new ByteArray(bytes), out, closeOut);
    }

    /**
     * 将byte数组写入到指定<code>OutputStream</code>中。
     */
    public static void writeBytes(ByteArray bytes, OutputStream out, boolean closeOut) throws IOException {
        try {
            out.write(bytes.getRawBytes(), bytes.getOffset(), bytes.getLength());
            out.flush();
        } finally {
            if (closeOut) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
