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

import org.junit.Test;

public class LogConfiguratorListenerTests extends AbstractLogConfiguratorListenerTests {
    public LogConfiguratorListenerTests() throws Exception {
    }

    @Test
    public void defaultValues() throws Exception {
        invokeInLoader("logback", "servletInit", "logback");

        assertThat(out, containsString("Initializing logback system"));

        assertThat(err, containsString("INFO: configuring \"logback\" using "
                + new File("src/test/config/WEB-INF/logback.xml").toURI().toURL().toExternalForm()));

        assertThat(err, containsString("- with property localAddress = "));
        assertThat(err, containsString("- with property localHost = "));
        assertThat(err, containsString("- with property logXXX = 111")); // from init-param
        assertThat(err, containsString("- with property logYYY = 222")); // from init-param
        assertThat(err, containsString("- with property loggingCharset = UTF-8")); // overrided by init-param
        assertThat(err, containsString("- with property loggingLevel = ERROR")); // overrided by init-param
        assertThat(err, containsString("- with property loggingRoot = " + System.getProperty("user.home")
                + File.separator + "logs"));

        assertThat(err, not(containsString("- with property other"))); // excluded from init-params
    }

    @SuppressWarnings("unused")
    private void servletInit(String logSystem) throws Exception {
        updateSystemOut();
        listener.contextInitialized(event);
    }
}
