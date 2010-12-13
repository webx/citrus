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
package com.alibaba.citrus.logconfig.support;

import static com.alibaba.citrus.logconfig.support.ConfigurableLogger.Level.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.logconfig.AbstractLogConfiguratorTests;
import com.alibaba.citrus.logconfig.LogConfigurator;
import com.alibaba.citrus.logconfig.support.ConfigurableLogger.Level;

public class ConfigurableLoggerTests extends AbstractLogConfiguratorTests {
    @Test
    public void logWithLevel() throws Exception {
        invokeInLoader("logback", "log_level");

        assertThat(out, containsString("test trace"));
        assertThat(out, containsString("test debug"));
        assertThat(out, containsString("test info"));
        assertThat(out, not(containsString("test warn")));
        assertThat(out, not(containsString("test error")));

        assertThat(err, not(containsString("test trace")));
        assertThat(err, not(containsString("test debug")));
        assertThat(err, not(containsString("test info")));
        assertThat(err, containsString("test warn"));
        assertThat(err, containsString("test error"));
    }

    @SuppressWarnings("unused")
    private void log_level() throws Exception {
        LogConfigurator.getConfigurator().configureDefault(true);

        MyLogger log = new MyLogger();

        log.log(trace, "test trace");
        log.log(debug, "test debug");
        log.log(info, "test info");
        log.log(warn, "test warn");
        log.log(error, "test error");

        try {
            log.log(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown log level: null", e.getMessage());
        }
    }

    @Test
    public void isLevelEnabled() throws Exception {
        invokeInLoader("logback", "log_levelEnabled", "trace", true, false);
        invokeInLoader("logback", "log_levelEnabled", "debug", true, false);
        invokeInLoader("logback", "log_levelEnabled", "info", true, true);
        invokeInLoader("logback", "log_levelEnabled", "warn", true, true);
        invokeInLoader("logback", "log_levelEnabled", "error", true, true);
    }

    @SuppressWarnings("unused")
    private void log_levelEnabled(String level, boolean enabled1, boolean enabled2) throws Exception {
        LogConfigurator.getConfigurator().configureDefault(true);
        assertEquals(enabled1, new MyLogger().isLevelEnabled(Level.valueOf(level)));

        LogConfigurator.getConfigurator().configureDefault(false);
        assertEquals(enabled2, new MyLogger().isLevelEnabled(Level.valueOf(level)));

        try {
            new MyLogger().isLevelEnabled(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown log level: null", e.getMessage());
        }
    }

    @Test
    public void defaultLogger() throws Exception {
        invokeInLoader("logback", "defaultLogger_");
    }

    @SuppressWarnings("unused")
    private void defaultLogger_() {
        MyLogger l = new MyLogger();

        assertNotNull(l.getLogger());
        assertEquals("MyDefaultLogger", l.getLogger().getName());

        assertSame(l.getLogger(), new MyLogger().getLogger());
    }

    @Test
    public void setLogName() throws Exception {
        invokeInLoader("logback", "setLogName_");
    }

    @SuppressWarnings("unused")
    private void setLogName_() {
        MyLogger l = new MyLogger();

        l.setLogName(" com.alibaba.security ");
        assertEquals("com.alibaba.security", l.getLogger().getName());

        l.setLogName("  ");
        assertEquals("com.alibaba.security", l.getLogger().getName());

        l.setLogName(null);
        assertEquals("com.alibaba.security", l.getLogger().getName());

        assertNotSame(l.getLogger(), new MyLogger().getLogger());
    }

    @Test
    public void toString_() throws Exception {
        invokeInLoader("logback", "toString__");
    }

    @SuppressWarnings("unused")
    private void toString__() {
        MyLogger l = new MyLogger();

        assertEquals("MyLogger[MyDefaultLogger]", l.toString());

        l.setLogName("com.alibaba.security");
        assertEquals("MyLogger[com.alibaba.security]", l.toString());
    }

    private static class MyLogger extends ConfigurableLogger {
        @Override
        protected Logger getDefaultLogger() {
            return LoggerFactory.getLogger("MyDefaultLogger");
        }
    }
}
