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
import static org.junit.Assert.*;

import java.lang.reflect.Field;

import javax.servlet.http.HttpSession;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;

public abstract class AbstractSessionListenerTests extends AbstractRequestContextsTests<SessionRequestContext> {
    protected HttpSession session;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-session-interceptors.xml");
    }

    /**
     * Ìæ»»log¡£
     */
    protected final void replaceLogger(Logger log, String logField, String originalLogName, int index) throws Exception {
        invokeNoopServlet("/servlet");
        initRequestContext();

        SessionRequestContext rc = getFieldValue(session, "requestContext", SessionRequestContext.class);
        SessionConfig config = rc.getSessionConfig();
        SecurityLogger slog = getFieldValue(config.getSessionInterceptors()[index], logField, SecurityLogger.class);

        Field field = getAccessibleField(slog.getClass(), "log");
        Logger originalLog = (Logger) field.get(slog);

        if (LoggerFactory.getLogger(getClass()).getClass().isInstance(originalLog)) {
            assertEquals(originalLogName, originalLog.getName());
        }

        field.set(slog, log);
    }

    protected final void setLevelEnabled(boolean enabled, Logger... logs) {
        for (Logger log : logs) {
            expect(log.isTraceEnabled()).andReturn(enabled).anyTimes();
            expect(log.isDebugEnabled()).andReturn(enabled).anyTimes();
            expect(log.isInfoEnabled()).andReturn(enabled).anyTimes();
            expect(log.isWarnEnabled()).andReturn(enabled).anyTimes();
            expect(log.isErrorEnabled()).andReturn(enabled).anyTimes();
        }
    }
}
