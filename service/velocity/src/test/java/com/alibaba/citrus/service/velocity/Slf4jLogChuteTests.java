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
package com.alibaba.citrus.service.velocity;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.apache.velocity.runtime.log.LogChute.*;
import static org.junit.Assert.*;

import org.apache.velocity.runtime.log.LogChute;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.velocity.impl.Slf4jLogChute;
import com.alibaba.citrus.test.TestEnvStatic;

public class Slf4jLogChuteTests {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private LogChute logChute;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() {
        logChute = new Slf4jLogChute(log);
    }

    @Test
    public void create() {
        try {
            new Slf4jLogChute(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("log"));
        }
    }

    @Test
    public void log() {
        Throwable e = new Throwable("exception");

        logChute.log(TRACE_ID, "trace message", e);
        logChute.log(DEBUG_ID, "debug message", e);
        logChute.log(INFO_ID, "info message", e);
        logChute.log(WARN_ID, "warn message", e);
        logChute.log(ERROR_ID, "error message", e);
    }

    @Test
    public void logEnabled() {
        assertEquals(log.isTraceEnabled(), logChute.isLevelEnabled(TRACE_ID));
        assertEquals(log.isDebugEnabled(), logChute.isLevelEnabled(DEBUG_ID));
        assertEquals(log.isInfoEnabled(), logChute.isLevelEnabled(INFO_ID));
        assertEquals(log.isWarnEnabled(), logChute.isLevelEnabled(WARN_ID));
        assertEquals(log.isErrorEnabled(), logChute.isLevelEnabled(ERROR_ID));
    }

    @Test
    public void _toString() {
        assertEquals("Slf4jLogChute[" + getClass().getName() + "]", logChute.toString());
    }
}
