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
package com.alibaba.citrus.service.requestcontext.basic.impl;

import static com.alibaba.citrus.service.requestcontext.basic.impl.BasicResponseImpl.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class BasicResponseImplTests {
    private HttpServletRequest request;

    @Before
    public void init() {
        request = createMock(HttpServletRequest.class);
    }

    private HttpServletRequest request(String requestURL) {
        reset(request);
        expect(request.getRequestURL()).andReturn(new StringBuffer(requestURL)).anyTimes();
        replay(request);

        return request;
    }

    @Test
    public void normalizeLocation_emptyLocation() {
        try {
            normalizeLocation(" ", request(""));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no redirect location"));
        }

        try {
            normalizeLocation(null, request(""));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no redirect location"));
        }
    }

    @Test
    public void normalizeLocation_absLocation_normalized() {
        assertEquals("http://localhost:8080/test?x=1",
                normalizeLocation("http://localhost:8080//a/../test?x=1", request("")));
    }

    @Test
    public void normalizeLocation_relLocation_normalized() {
        // relative path: http://localhost:8080/a/../bb?x=1
        assertEquals("http://localhost:8080/bb?x=1",
                normalizeLocation("a/../bb?x=1", request("http://localhost:8080/test")));

        // relative path: http://localhost:8080/test/a/../bb?x=1
        assertEquals("http://localhost:8080/test/bb?x=1",
                normalizeLocation("a/../bb?x=1", request("http://localhost:8080/test/")));

        // absolute path: http://localhost:8080/a/../bb?x=1
        assertEquals("http://localhost:8080/bb?x=1",
                normalizeLocation("/a/../bb?x=1", request("http://localhost:8080/test/")));

        // relative path: http://localhost:8080/test/a/../../bb?x=1
        assertEquals("http://localhost:8080/bb?x=1",
                normalizeLocation("a/../../bb?x=1", request("http://localhost:8080/test/")));

        // relative path: http://localhost:8080/a/../../bb?x=1
        assertEquals("http://localhost:8080/../bb?x=1",
                normalizeLocation("a/../../bb?x=1", request("http://localhost:8080/test")));
    }
}
