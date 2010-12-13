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

import com.alibaba.citrus.logconfig.log4j.Log4jConfigurator;

public class Log4jConfiguratorTests extends AbstractLogConfiguratorTests {
    @Test
    public void getConfigurator_autoDiscovery() throws Exception {
        invokeInLoader("log4j", "getConfigurator");
    }

    @Test
    public void getConfigurator_withName() throws Exception {
        invokeInLoader("log4j", "getConfigurator", (String) null);
        invokeInLoader("log4j", "getConfigurator", "  "); // trimming to null
        invokeInLoader("log4j", "getConfigurator", "loG4j"); // case insensitive
    }

    @Test
    public void getConfigurator_wrongContext() throws Exception {
        // 在logback的环境中，创建log4j，成功，但打印警告。
        invokeInLoader("logback", "getConfigurator", "log4j");
        assertThat(err, containsString("WARN: SLF4J chose [logback] as its logging system, not [log4j]"));
    }

    @SuppressWarnings("unused")
    private void getConfigurator() throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator();
        assertThat(configurator, instanceOf(Log4jConfigurator.class));
    }

    @SuppressWarnings("unused")
    private void getConfigurator(String logSystem) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator(logSystem);
        assertThat(configurator, instanceOf(Log4jConfigurator.class));
    }

    @Test
    public void configureDefault_debug() throws Exception {
        invokeInLoader("log4j", "configureDefault", true);

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
        invokeInLoader("log4j", "configureDefault", false);

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
        invokeInLoader("log4j", "configure", (URL) null);

        assertThat(err, containsString("WARN: Failed to configure log4j using null"));
        assertThat(err, containsString("NullPointerException"));
    }

    @Test
    public void configure() throws Exception {
        invokeInLoader("log4j", "configure", getClass().getClassLoader().getResource("META-INF/my-log4j.xml"));

        assertThat(err, containsString("INFO: configuring \"log4j\" using "));
        assertThat(err, containsString("META-INF/my-log4j.xml"));

        assertThat(out, not(containsString("test trace")));
        assertThat(out, not(containsString("test debug")));
        assertThat(out, containsString("test info"));
        assertThat(out, containsString("test warn"));
        assertThat(out, containsString("test error"));
    }

    @SuppressWarnings("unused")
    private void configure(URL configFile) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator("log4j");
        configurator.configure(configFile, configurator.getDefaultProperties());
        log();
    }
}
