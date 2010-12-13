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

public class LogConfiguratorListenerMultiLogSystemsTests extends AbstractLogConfiguratorListenerTests {
    public LogConfiguratorListenerMultiLogSystemsTests() throws Exception {
    }

    @Override
    protected String webxml() {
        return "src/test/config/WEB-INF/web2.xml";
    }

    @Test
    public void multi_log_systems() throws Exception {
        invokeInLoader("logback", "servletInit", "logback");

        assertThat(out, containsString("Initializing log4j system"));
        assertThat(out, containsString("Initializing logback system"));

        assertThat(err, containsString("INFO: configuring \"logback\" using "
                + new File("src/test/config/WEB-INF/logback.xml").toURI().toURL().toExternalForm()));

        assertThat(err, containsString("INFO: configuring \"log4j\" using "
                + new File("src/test/config/WEB-INF/log4j.xml").toURI().toURL().toExternalForm()));
    }

    @SuppressWarnings("unused")
    private void servletInit(String logSystem) throws Exception {
        updateSystemOut();
        listener.contextInitialized(event);
    }
}
