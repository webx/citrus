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
package com.alibaba.citrus.service.requestcontext.session.interceptor;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.alibaba.citrus.service.requestcontext.util.CookieSupport;

public class LifecycleLoggerListenerTests extends AbstractSessionListenerTests {
    private String sessionId;
    private Logger log1;
    private Logger log2;

    @Before
    public void init() throws Exception {
        // 设置log1, log2
        invokeNoopServlet("/servlet");
        initRequestContext();

        log1 = createMock(Logger.class);
        log2 = createMock(Logger.class);

        setLevelEnabled(false, log1, log2); // 关闭log，以防出错
        replay(log1, log2);

        replaceLogger(log1, "log", "SECURITY", 0);
        replaceLogger(log2, "log", "hello.world", 1);

        // 预先确定session id 以便测试
        sessionId = "1234567890ABCDEFG";
        invokeNoopServlet("/servlet");

        CookieSupport cookie = new CookieSupport("JSESSIONID", sessionId);

        cookie.setPath("/");
        cookie.addCookie(response);

        commitToClient();

        assertEquals(sessionId, clientResponse.getNewCookieValue("JSESSIONID")); // new added cookie

        reset(log1, log2);
    }

    @Override
    protected String getDefaultBeanName() {
        return "logger";
    }

    @Override
    protected void afterInitRequestContext() throws Exception {
        session = requestContext.getRequest().getSession();
    }

    @Test
    public void test() throws Exception {
        setLevelEnabled(true, log1, log2); // 打开log，以防出错

        log("session created, id=" + sessionId);
        visitLog("session visited, id=" + sessionId);
        log("session invalidated, id=" + sessionId);

        replay(log1, log2);

        invokeNoopServlet("/servlet");
        initRequestContext();

        session.invalidate();

        verify(log1, log2);
    }

    private void log(String msg) {
        log1.debug(msg, (Throwable) null);
        log2.info(msg, (Throwable) null);
    }

    private void visitLog(String msg) {
        log1.trace(msg, (Throwable) null);
        log2.debug(msg, (Throwable) null);
    }
}
