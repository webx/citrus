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

package com.alibaba.citrus.test.context;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.test.util.ServletTestContainer;
import com.meterware.httpunit.PostMethodWebRequest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = SpringextContextLoader.class)
public class SpringextWebContextLoaderTests {
    @Autowired
    private ServletTestContainer server;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @After
    public void dispose() {
        server.cleanup();
    }

    @Test
    public void get() {
        server.request("/noop?a=1&b=2");

        assertEquals("1", request.getParameter("a"));
        assertEquals("2", request.getParameter("b"));
    }

    @Test
    public void post() {
        PostMethodWebRequest req = new PostMethodWebRequest("http://www.test.com/noop");
        req.setParameter("a", "3");
        req.setParameter("b", "4");

        server.request(req);

        assertEquals("3", request.getParameter("a"));
        assertEquals("4", request.getParameter("b"));
    }

    @Test
    public void response() throws Exception {
        server.request("/noop?a=1&b=2");

        response.getWriter().print("hello");

        server.commit();
        assertEquals(200, server.getClientResponse().getResponseCode());
        assertEquals("hello", server.getClientResponse().getText());
    }

    @Test
    public void myservlet() throws Exception {
        server.request("/test?a=1&b=2");

        server.getInvocationContext().getServlet().service(request, response);

        server.commit();
        assertEquals(200, server.getClientResponse().getResponseCode());
        assertEquals("myservlet", server.getClientResponse().getText());
    }
}
