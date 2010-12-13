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
package com.alibaba.citrus.service.requestcontext.rundata;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.util.io.ByteArray;

/**
 * 测试<code>RunData</code>的buffer功能。
 * 
 * @author Michael Zhou
 */
public class BufferedRunDataTests extends AbstractRequestContextsTests<RunData> {
    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-rundata.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext();
    }

    @Override
    protected String getDefaultBeanName() {
        return "rundata";
    }

    @Test
    public void byteBuffer() throws Exception {
        OutputStream ostream = newResponse.getOutputStream();

        // write，pop
        ostream.write("abc".getBytes());
        ostream.flush();

        ByteArray bytes = requestContext.popByteBuffer();

        assertEquals("abc", new String(bytes.toByteArray()));

        // write, push, write, push, write, pop, pop, pop
        ostream.write("abc".getBytes());
        ostream.flush();

        requestContext.pushBuffer();
        ostream.write("def".getBytes());
        ostream.flush();

        requestContext.pushBuffer();
        ostream.write("ghi".getBytes());
        ostream.flush();

        bytes = requestContext.popByteBuffer();
        assertEquals("ghi", new String(bytes.toByteArray()));

        bytes = requestContext.popByteBuffer();
        assertEquals("def", new String(bytes.toByteArray()));

        bytes = requestContext.popByteBuffer();
        assertEquals("abc", new String(bytes.toByteArray()));
    }

    @Test
    public void charBuffer() throws Exception {
        PrintWriter writer = newResponse.getWriter();

        // write，pop
        writer.write("abc");
        writer.flush();

        String chars = requestContext.popCharBuffer();

        assertEquals("abc", chars);

        // write, push, write, push, write, pop, pop, pop
        writer.write("abc");
        writer.flush();

        requestContext.pushBuffer();
        writer.write("def");
        writer.flush();

        requestContext.pushBuffer();
        writer.write("ghi");
        writer.flush();

        chars = requestContext.popCharBuffer();
        assertEquals("ghi", chars);

        chars = requestContext.popCharBuffer();
        assertEquals("def", chars);

        chars = requestContext.popCharBuffer();
        assertEquals("abc", chars);
    }

    /**
     * 先getWriter，再getOutputStream，此时将创建一个适配器。
     */
    @Test
    public void writeBytesAndReadChars() throws Exception {
        @SuppressWarnings("unused")
        Writer writer = newResponse.getWriter();
        OutputStream ostream = newResponse.getOutputStream();

        // write，pop
        ostream.write("abc".getBytes());
        ostream.flush();

        String chars = requestContext.popCharBuffer();

        assertEquals("abc", chars);

        // write, push, write, push, write, pop, pop, pop
        ostream.write("abc".getBytes());
        ostream.flush();

        requestContext.pushBuffer();
        ostream.write("def".getBytes());
        ostream.flush();

        requestContext.pushBuffer();
        ostream.write("ghi".getBytes());
        ostream.flush();

        chars = requestContext.popCharBuffer();
        assertEquals("ghi", chars);

        chars = requestContext.popCharBuffer();
        assertEquals("def", chars);

        chars = requestContext.popCharBuffer();
        assertEquals("abc", chars);
    }

    /**
     * 先getOutputStream，再getWriter，此时将创建一个适配器。
     */
    @Test
    public void writeCharsAndReadBytes() throws Exception {
        @SuppressWarnings("unused")
        OutputStream ostream = newResponse.getOutputStream();
        Writer writer = newResponse.getWriter();

        // write，pop
        writer.write("abc");
        writer.flush();

        ByteArray bytes = requestContext.popByteBuffer();

        assertEquals("abc", new String(bytes.toByteArray()));

        // write, push, write, push, write, pop, pop, pop
        writer.write("abc");
        writer.flush();

        requestContext.pushBuffer();
        writer.write("def");
        writer.flush();

        requestContext.pushBuffer();
        writer.write("ghi");
        writer.flush();

        bytes = requestContext.popByteBuffer();
        assertEquals("ghi", new String(bytes.toByteArray()));

        bytes = requestContext.popByteBuffer();
        assertEquals("def", new String(bytes.toByteArray()));

        bytes = requestContext.popByteBuffer();
        assertEquals("abc", new String(bytes.toByteArray()));
    }

    @Test
    public void illegalStateExceptionForBytes() throws Exception {
        // getOutputStream之后试图popCharBuffer
        newResponse.getOutputStream();

        try {
            requestContext.popCharBuffer();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        // getOutputStream之后试图改变buffer模式
        initRequestContext();

        newResponse.getOutputStream();
        requestContext.setBuffering(true);

        try {
            requestContext.setBuffering(false);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        // 非buffering模式下操作byte buffer。
        initRequestContext();

        requestContext.setBuffering(false);
        newResponse.getOutputStream();

        try {
            requestContext.pushBuffer();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        try {
            requestContext.popByteBuffer();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        requestContexts.commitRequestContext(requestContext);
    }

    @Test
    public void illegalStateExceptionForWriter() throws Exception {
        // getWriter之后试图popByteBuffer
        newResponse.getWriter();

        try {
            requestContext.popByteBuffer();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        // getWriter之后试图改变buffer模式
        initRequestContext();

        newResponse.getWriter();
        requestContext.setBuffering(true);

        try {
            requestContext.setBuffering(false);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        // 非buffering模式下操作char buffer。
        initRequestContext();

        requestContext.setBuffering(false);
        newResponse.getWriter();

        try {
            requestContext.pushBuffer();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        try {
            requestContext.popCharBuffer();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }

        requestContexts.commitRequestContext(requestContext);
    }
}
