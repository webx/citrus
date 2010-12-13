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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;

public class AttributeWhitelistTests extends AbstractSessionListenerTests {
    private Logger log;

    @Before
    public void init() throws Exception {
        log = createMock(Logger.class);
        replaceLogger(log, "log", "hello.world", 0);
        setLevelEnabled(true, log);

        reset(log);

        // init session
        invokeNoopServlet("/servlet");
        initRequestContext();

        assertTrue(session.isNew());
    }

    @After
    public void verify_mock() {
        verify(log);
    }

    @Override
    protected String getDefaultBeanName() {
        return "whitelist";
    }

    @Override
    protected void afterInitRequestContext() throws Exception {
        session = requestContext.getRequest().getSession();
    }

    @Test
    public void init_() {
        replay(log);

        SessionConfig sessionConfig = createMock(SessionConfig.class);
        expect(sessionConfig.getModelKey()).andReturn("MY_SESSION_MODEL");
        replay(sessionConfig);

        SessionAttributeWhitelist whitelist = new SessionAttributeWhitelist();
        whitelist.init(sessionConfig);

        assertEquals("SECURITY", getFieldValue(whitelist, "log", SecurityLogger.class).getLogger().getName());
        assertTrue(getFieldValue(whitelist, "allowedAttributes", Map.class).isEmpty());
    }

    @Test
    public void access_sessionModelOnly() throws Exception {
        replay(log);
    }

    @Test
    public void read_notExistAttr() throws Exception {
        replay(log);
        assertNull(session.getAttribute("aaa"));
    }

    @Test
    public void readWrite_anyType() throws Exception {
        replay(log);
        session.setAttribute("aaa", 123); // integer
        assertEquals(123, session.getAttribute("aaa")); // integer
    }

    @Test
    public void readWrite_anyType2() throws Exception {
        replay(log);
        session.setAttribute("aaa", "hello"); // string
        assertEquals("hello", session.getAttribute("aaa")); // string
    }

    @Test
    public void readWrite_specifiedType() throws Exception {
        replay(log);
        session.setAttribute("bbb", "hello"); // string
        assertEquals("hello", session.getAttribute("bbb")); // string
    }

    @Test
    public void readWrite_wrongType() throws Exception {
        log.warn("Attribute to write is not in whitelist: name={}, type={}", "bbb", "java.lang.Integer");
        log.warn("Attribute to read is not in whitelist: name={}, type={}", "bbb", "java.lang.Integer");
        replay(log);
        session.setAttribute("bbb", 123); // integer
        assertEquals(123, session.getAttribute("bbb")); // integer
    }

    @Test
    public void readWrite_primitiveType() throws Exception {
        replay(log);
        session.setAttribute("ccc", 123); // int
        assertEquals(123, session.getAttribute("ccc")); // int
    }

    @Test
    public void readWrite_wrongPrimitiveType() throws Exception {
        log.warn("Attribute to write is not in whitelist: name={}, type={}", "ccc", "java.lang.String");
        log.warn("Attribute to read is not in whitelist: name={}, type={}", "ccc", "java.lang.String");
        replay(log);
        session.setAttribute("ccc", "hello"); // string
        assertEquals("hello", session.getAttribute("ccc")); // string
    }

    @Test
    public void readWrite_nameNotInWhitelist() throws Exception {
        log.warn("Attribute to write is not in whitelist: name={}, type={}", "ddd", "java.lang.String");
        log.warn("Attribute to read is not in whitelist: name={}, type={}", "ddd", "java.lang.String");
        replay(log);
        session.setAttribute("ddd", "hello"); // string
        assertEquals("hello", session.getAttribute("ddd")); // string
    }
}
