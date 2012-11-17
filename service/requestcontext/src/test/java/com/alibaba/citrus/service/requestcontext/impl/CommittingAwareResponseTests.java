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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class CommittingAwareResponseTests {
    private HttpServletResponse     response;
    private HeaderCommitter         headerCommitter;
    private CommittingAwareResponse committingAwareResponse;
    private ServletOutputStream     originalStream;
    private PrintWriter             originalWriter;

    @Before
    public void init() {
        response = createMock(HttpServletResponse.class);
        headerCommitter = createMock(HeaderCommitter.class);
        originalStream = createMock(ServletOutputStream.class);
        originalWriter = createMock(PrintWriter.class);

        committingAwareResponse = new CommittingAwareResponse(response, headerCommitter);
    }

    @Test
    public void getOutputStream() throws Exception {
        expect(response.getOutputStream()).andReturn(originalStream).once();
        replay(response);

        CommittingAwareServletOutputStream stream = (CommittingAwareServletOutputStream) committingAwareResponse.getOutputStream();

        assertNotNull(stream);
        assertSame(stream, committingAwareResponse.getOutputStream()); // 多次调用返回同一个对象

        // 调用headCommitter
        headerCommitter.commitHeaders();
        expectLastCall().once();
        replay(headerCommitter);

        stream.flush();

        verify(headerCommitter, response);
    }

    @Test
    public void getWriter() throws Exception {
        expect(response.getWriter()).andReturn(originalWriter).once();
        replay(response);

        CommittingAwarePrintWriter writer = (CommittingAwarePrintWriter) committingAwareResponse.getWriter();

        assertNotNull(writer);
        assertSame(writer, committingAwareResponse.getWriter()); // 多次调用返回同一个对象

        // 调用headCommitter
        headerCommitter.commitHeaders();
        expectLastCall().once();
        replay(headerCommitter);

        writer.flush();

        verify(headerCommitter, response);
    }

    @Test
    public void _toString() {
        assertEquals(response.toString(), committingAwareResponse.toString());
    }
}
