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
package com.alibaba.citrus.logconfig;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import com.alibaba.citrus.logconfig.logback.LogbackConfigurator;

public class LogbackConfiguratorTests extends AbstractLogConfiguratorTests {
    @Test
    public void getConfigurator_autoDiscovery() throws Exception {
        invokeInLoader("logback", "getConfigurator");
    }

    @Test
    public void getConfigurator_withName() throws Exception {
        invokeInLoader("logback", "getConfigurator", (String) null);
        invokeInLoader("logback", "getConfigurator", " "); // trimming to null
        invokeInLoader("logback", "getConfigurator", " LogBack "); // case insensitive
    }

    @SuppressWarnings("unused")
    private void getConfigurator() throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator();
        assertThat(configurator, instanceOf(LogbackConfigurator.class));
    }

    @SuppressWarnings("unused")
    private void getConfigurator(String logSystem) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator(logSystem);
        assertThat(configurator, instanceOf(LogbackConfigurator.class));
    }

    @Test
    public void configure_wrongContext() throws Exception {
        // 在log4j+logback的环境中，创建logback成功，但configure失败。
        invokeInLoader("log4j, logback", "configure_wrongContext", "logback");

        assertThat(err, containsString("WARN: SLF4J chose [log4j] as its logging system, not [logback]"));
        assertThat(err, containsString("multiple SLF4J bindings"));
        assertThat(err, containsString("WARN: Failed to configure logback using "));
        assertThat(err, containsString("logback-default.xml"));
        assertThat(err, containsString("LogbackException: Expected LOGBACK binding with SLF4J, "
                + "but another log system has taken the place: Log4jLoggerFactory"));
    }

    @SuppressWarnings("unused")
    private void configure_wrongContext(String logSystem) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator(logSystem);
        assertThat(configurator, instanceOf(LogbackConfigurator.class));

        configurator.configureDefault();
    }

    @Test
    public void configureDefault_debug() throws Exception {
        invokeInLoader("logback", "configureDefault", true);

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

    @Test
    public void configureDefault_noDebug() throws Exception {
        invokeInLoader("logback", "configureDefault", false);

        assertThat(out, not(containsString("test trace")));
        assertThat(out, not(containsString("test debug")));
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
    private void configureDefault(Boolean debug) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator();

        if (debug == null) {
            configurator.configureDefault();
        } else {
            configurator.configureDefault(debug);
        }

        log();
    }

    @Test
    public void configure_failure() throws Exception {
        invokeInLoader("logback", "configure", (URL) null);

        assertThat(err, containsString("WARN: Failed to configure logback using null"));
        assertThat(err, containsString("NullPointerException"));
    }

    @Test
    public void configure() throws Exception {
        invokeInLoader("logback", "configure", getClass().getClassLoader().getResource("META-INF/my-logback.xml"));

        assertThat(err, containsString("INFO: configuring \"logback\" using "));
        assertThat(err, containsString("META-INF/my-logback.xml"));

        assertThat(out, not(containsString("test trace")));
        assertThat(out, not(containsString("test debug")));
        assertThat(out, containsString("test info"));
        assertThat(out, containsString("test warn"));
        assertThat(out, containsString("test error"));
    }

    @SuppressWarnings("unused")
    private void configure(URL configFile) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator("logback");
        configurator.configure(configFile, configurator.getDefaultProperties());
        log();
    }
}
