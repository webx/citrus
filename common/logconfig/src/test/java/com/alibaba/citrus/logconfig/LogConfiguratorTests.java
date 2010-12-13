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

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.logconfig.spi.AbstractLogConfigurator;

public class LogConfiguratorTests extends AbstractLogConfiguratorTests {
    @Test
    public void getConfigurator_failure() throws Exception {
        // 既没有指定logsystem，也没找到系统默认的logsystem。
        assertGetConfiguratorFailure(null, "", "No log system bound with SLF4J");

        // provider not found
        assertGetConfiguratorFailure(null, "notexist", "Could not find LogConfigurator for \"notexist\" "
                + "by searching in META-INF/logconfig.providers");

        // provider class not found
        assertGetConfiguratorFailure(null, "cnf", ClassNotFoundException.class,
                "Could not find LogConfigurator for cnf");

        // provider class is not LogConfigurator
        assertGetConfiguratorFailure(null, "string", "string class java.lang.String is not a sub-class of "
                + LogConfigurator.class.getName());

        // fail to create instance
        assertGetConfiguratorFailure(null, "abstract", InstantiationException.class,
                "Could not create instance of class " + AbstractLogConfigurator.class.getName() + " for abstract");
    }

    private void assertGetConfiguratorFailure(String envLogSystem, String logSystem, String... strs) throws Exception {
        assertGetConfiguratorFailure(envLogSystem, logSystem, null, strs);
    }

    private void assertGetConfiguratorFailure(String envLogSystem, String logSystem, Class<?> causeClass,
                                              String... strs) throws Exception {
        invokeInLoader(envLogSystem, "assertGetConfiguratorFailure_", logSystem, causeClass, strs);
    }

    @SuppressWarnings("unused")
    private void assertGetConfiguratorFailure_(String logSystem, Class<?> causeClass, String... strs) {
        for (int i = 0; i < 2; i++) {
            try {
                if (i == 0) {
                    LogConfigurator.getConfigurator(logSystem);
                } else {
                    LogConfigurator.getConfigurators(logSystem);
                }

                fail(logSystem);
            } catch (IllegalArgumentException e) {
                if (causeClass != null) {
                    assertThat(e.getCause(), instanceOf(causeClass));
                }

                if (strs != null) {
                    for (String str : strs) {
                        assertThat(e.getMessage(), containsString(str));
                    }
                }
            }
        }
    }

    @Test
    public void getConfigurators() throws Exception {
        // getConfigurators() - 无参数 - 默认值
        invokeInLoader("log4j", "getConfigurators", new String[0], new String[] { "log4j" });

        // getConfigurators("log4j", "logback") - 多个参数
        invokeInLoader("log4j", "getConfigurators", new String[] { "log4j", "logback" }, new String[] { "log4j",
                "logback" });

        // getConfigurators(null, "  ") - 空参数 - 默认值
        invokeInLoader("log4j", "getConfigurators", new String[] { null, "  " }, new String[] { "log4j", "log4j" });

        // getConfigurators("logback") - 指定值与slf4j不匹配
        invokeInLoader("log4j", "getConfigurators", new String[] { "logback" }, new String[] { "logback" });

        assertEquals("", out);
        assertEquals("WARN: SLF4J chose [log4j] as its logging system, not [logback]", err.trim());

        // getConfigurators("logback", null) - 找不到默认值
        try {
            invokeInLoader("", "getConfigurators", new String[] { "logback", null }, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("No log system bound with SLF4J", e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void getConfigurators(String[] logSystems, String[] results) throws Exception {
        LogConfigurator[] logConfigurators = LogConfigurator.getConfigurators(logSystems);

        assertEquals(results.length, logConfigurators.length);

        for (int i = 0; i < logConfigurators.length; i++) {
            assertEquals(results[i], logConfigurators[i].getLogSystem());
        }
    }

    @Test
    public void configureDefault_failure() throws Exception {
        invokeInLoader(null, "configureDefault", "nodefault", true);
        assertThat(err, containsString("ERROR: could not find default config file for \"nodefault\""));
    }

    @Test
    public void configureDefault() throws Exception {
        invokeInLoader(null, "configureDefault", "ok", true);
        String sysPropKey = (String) System.getProperties().keySet().iterator().next();

        assertThat(err, containsString("INFO: configuring \"ok\" using "));
        assertThat(err, containsString("ok-default.xml"));
        assertThat(err, containsString("- with property loggingLevel = TRACE"));
        assertThat(err, containsString("- with property loggingCharset = " + Charset.defaultCharset().name()));
        assertThat(err, containsString("- with property localHost = "));
        assertThat(err, containsString("- with property localAddress = "));
        assertThat(err, containsString("- with property loggingRoot = " + System.getProperty("user.home")
                + File.separator + "logs"));
        assertThat(err, not(containsString("- with property " + sysPropKey))); // do not list system props

        invokeInLoader(null, "configureDefault", "ok", false);

        assertThat(err, containsString("INFO: configuring \"ok\" using "));
        assertThat(err, containsString("ok-default.xml"));
        assertThat(err, containsString("- with property loggingLevel = INFO"));
        assertThat(err, containsString("- with property loggingCharset = " + Charset.defaultCharset().name()));
        assertThat(err, containsString("- with property localHost = "));
        assertThat(err, containsString("- with property localAddress = "));
        assertThat(err, containsString("- with property loggingRoot = " + System.getProperty("user.home")
                + File.separator + "logs"));
        assertThat(err, not(containsString("- with property " + sysPropKey))); // do not list system props
    }

    @SuppressWarnings("unused")
    private void configureDefault(String logSystem, boolean debug) throws Exception {
        LogConfigurator configurator = LogConfigurator.getConfigurator(logSystem);
        configurator.configureDefault(debug);
    }

    @Test
    public void getLogSystemAndDefaultConfigFile() throws Exception {
        invokeInLoader(null, "getLogSystemAndDefaultConfigFile", "ok", "ok-default.xml");
        invokeInLoader("logback", "getLogSystemAndDefaultConfigFile", "LOGBACK", "logback-default.xml");
        invokeInLoader("log4J", "getLogSystemAndDefaultConfigFile", "LOG4J", "log4j-default.xml");
    }

    @SuppressWarnings("unused")
    private void getLogSystemAndDefaultConfigFile(String logSystem, String defaultConfigFile) {
        LogConfigurator configurator = LogConfigurator.getConfigurator(logSystem);

        // log system
        assertEquals(logSystem.toLowerCase(), configurator.getLogSystem());

        // default config file
        assertThat(configurator.getDefaultConfigFile().toExternalForm(), endsWith(defaultConfigFile));
    }

    @Test
    public void getDefaultProperties() throws Exception {
        String defaultCharset = Charset.defaultCharset().name();

        // default values
        invokeInLoader(null, "getDefaultProperties", "INFO", defaultCharset, null, null);

        // override level & charset
        String charset = "UTF-8".equalsIgnoreCase(defaultCharset) ? "GBK" : "UTF-8";
        invokeInLoader(null, "getDefaultProperties", "ERROR", charset, "ERROR", charset);
    }

    @SuppressWarnings("unused")
    private void getDefaultProperties(String level, String charset, String overrideLevel, String overrideCharset) {
        if (overrideCharset != null) {
            System.setProperty("loggingCharset", overrideCharset);
        }

        if (overrideLevel != null) {
            System.setProperty("loggingLevel", overrideLevel);
        }

        try {
            LogConfigurator configurator = LogConfigurator.getConfigurator("ok");
            Map<String, String> props = configurator.getDefaultProperties();

            assertEquals(charset, props.get("loggingCharset"));
            assertEquals(level, props.get("loggingLevel"));
            assertEquals("world", props.get("hello")); // override setDefaultProperties()
        } finally {
            System.clearProperty("loggingCharset");
            System.clearProperty("loggingLevel");
        }
    }
}
