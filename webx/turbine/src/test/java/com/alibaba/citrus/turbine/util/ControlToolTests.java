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
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.webx.WebxComponents;

public class ControlToolTests extends AbstractPullToolTests<ControlTool> {
    @Override
    protected String toolName() {
        return "control";
    }

    @Before
    public void init() throws Exception {
        rundata.getResponse().getWriter();
    }

    @Test
    public void init_noAutowiring() throws Exception {
        assertInit("components");
        assertInit("moduleLoaderService");
        assertInit("mappingRuleService");
        assertInit("templateService");
        assertInit("request");
        assertInit("bufferedRequestContext");
    }

    private void assertInit(String missingName) throws Exception {
        tool = new ControlTool();

        if (!"components".equals(missingName)) {
            getAccessibleField(ControlTool.class, "components").set(tool, createMock(WebxComponents.class));
        }

        if (!"moduleLoaderService".equals(missingName)) {
            getAccessibleField(ControlTool.class, "moduleLoaderService").set(tool,
                    createMock(ModuleLoaderService.class));
        }

        if (!"mappingRuleService".equals(missingName)) {
            getAccessibleField(ControlTool.class, "mappingRuleService").set(tool, createMock(MappingRuleService.class));
        }

        if (!"templateService".equals(missingName)) {
            getAccessibleField(ControlTool.class, "templateService").set(tool, createMock(TemplateService.class));
        }

        if (!"request".equals(missingName)) {
            getAccessibleField(ControlTool.class, "request").set(tool, createMock(HttpServletRequest.class));
        }

        if (!"bufferedRequestContext".equals(missingName)) {
            getAccessibleField(ControlTool.class, "bufferedRequestContext").set(tool,
                    createMock(BufferedRequestContext.class));
        }

        try {
            tool.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no " + missingName));
        }
    }

    @Test
    public void checkScope() throws Exception {
        assertNotSame(tool, getTool()); // request scope
    }

    @Test
    public void render() throws Exception {
        String content = tool.setTemplate("myControl").render();
        assertEquals("hello, baobao", content);
    }

    @Test
    public void render_notInit() throws Exception {
        tool = new ControlTool();

        try {
            tool.render();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Bean instance of " + ControlTool.class.getName()
                    + " has not been initialized yet."));
        }
    }

    @Test
    public void render_noTemplate() throws Exception {
        String content = tool.setModule("myControlNoTemplate").render();
        assertEquals("hello, baobao without template", content);
    }

    @Test
    public void render_withError() throws Exception {
        String content = tool.setModule("myControlWithError").setParameter("with_XSS", true).render();

        // dev mode, errorDetail == stackTrace, È·±£html escape¡£
        assertThat(
                content,
                containsAll(
                        "<!-- control failed: target=myControlWithError, exceptionType=java.lang.IllegalArgumentException -->",
                        "<div class=\"webx.error\">java.lang.IllegalArgumentException: &lt;script&gt;alert(1)&lt;/script&gt;"));

        assertThat(content, not(containsAll("<script>")));
    }

    @Test
    public void render_nest() throws Exception {
        String content = tool.setTemplate("nestedControl").render();

        // dev mode, errorDetail == stackTrace
        assertThat(
                content,
                containsAll(
                        "<!-- control failed: target=myControlWithError, exceptionType=java.lang.IllegalArgumentException -->",
                        "<div class=\"webx.error\">java.lang.IllegalArgumentException", "hello", "world"));
    }

    @Test
    public void render_crossComponent() throws Exception {
        String content = tool.setTemplate("app2:myControl").render();
        assertEquals("hi, baobao app2", content);

        content = tool.setModule("app2:myControlNoTemplate").render();
        assertEquals("hi, baobao app2 without template", content);
    }
}
