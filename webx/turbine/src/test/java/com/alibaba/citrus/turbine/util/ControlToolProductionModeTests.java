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
package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.webx.config.WebxConfiguration;

public class ControlToolProductionModeTests extends AbstractPullToolTests<ControlTool> {
    @BeforeClass
    public static void initWebx() throws Exception {
        System.setProperty("productionMode", "true");

        try {
            prepareServlet();
        } finally {
            System.clearProperty("productionMode");
        }

        WebxConfiguration webxConfiguration = (WebxConfiguration) factory.getBean("webxConfiguration");

        assertTrue(webxConfiguration.isProductionMode());
    }

    @Override
    protected String toolName() {
        return "control";
    }

    @Before
    public void init() throws Exception {
        rundata.getResponse().getWriter();
    }

    @Test
    public void render_withError() throws Exception {
        String content = tool.setModule("myControlWithError").setParameter("with_XSS", true).render();

        // prod mode, errorDetail == messageOnly, È·±£html escape¡£
        assertThat(
                content,
                containsAll(
                        "<!-- control failed: target=myControlWithError, exceptionType=java.lang.IllegalArgumentException -->",
                        "<div class=\"webx.error\">&lt;script&gt;alert(1)&lt;/script&gt;</div>"));

        assertThat(content, not(containsAll("<script>")));
    }

    @Test
    public void render_nest() throws Exception {
        String content = tool.setTemplate("nestedControl").render();

        // prod mode, errorDetail == messageOnly
        assertThat(
                content,
                containsAll(
                        "<!-- control failed: target=myControlWithError, exceptionType=java.lang.IllegalArgumentException -->",
                        "<div class=\"webx.error\">IllegalArgumentException</div>", "hello", "world"));
    }
}
