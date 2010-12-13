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
package com.alibaba.citrus.service.freemarker;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.freemarker.impl.FreeMarkerEngineImpl;
import com.alibaba.citrus.test.TestEnvStatic;

import freemarker.log.Logger;

public class Slf4jLoggerFactoryTests {
    private Object logFactory;
    private Logger log;
    private org.slf4j.Logger slf4jLog;

    static {
        TestEnvStatic.init();

        try {
            Class.forName(FreeMarkerEngineImpl.class.getName());
        } catch (ClassNotFoundException e) {
            fail();
        }
    }

    @Before
    public void init() throws Exception {
        Class<?> logFactoryClass = Class.forName("freemarker.log.Slf4jLoggerFactory");
        logFactory = logFactoryClass.newInstance();
        log = getLogger(logFactory, getClass().getName());
        slf4jLog = LoggerFactory.getLogger(getClass());
    }

    private Logger getLogger(Object logFactory, String name) throws Exception {
        return (Logger) logFactory.getClass().getMethod("getLogger", String.class).invoke(logFactory, name);
    }

    @Test
    public void create() {
        try {
            getLogger(logFactory, null);
            fail();
        } catch (Exception e) {
            assertThat(e, exception(IllegalArgumentException.class, "category"));
        }
    }

    @Test
    public void log() {
        Throwable e = new Throwable("exception");

        log.debug("debug message");
        log.debug("debug message", e);

        log.info("info message");
        log.info("info message", e);

        log.warn("warn message");
        log.warn("warn message", e);

        log.error("error message");
        log.error("error message", e);
    }

    @Test
    public void logEnabled() {
        assertEquals(slf4jLog.isDebugEnabled(), log.isDebugEnabled());
        assertEquals(slf4jLog.isInfoEnabled(), log.isInfoEnabled());
        assertEquals(slf4jLog.isWarnEnabled(), log.isWarnEnabled());
        assertEquals(slf4jLog.isErrorEnabled(), log.isErrorEnabled());
        assertEquals(slf4jLog.isErrorEnabled(), log.isFatalEnabled());
    }

    @Test
    public void _toString() {
        assertEquals("Logger[" + getClass().getName() + "]", log.toString());
    }
}
