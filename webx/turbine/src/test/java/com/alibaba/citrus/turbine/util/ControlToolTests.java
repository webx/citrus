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
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.support.AbstractContext;
import com.alibaba.citrus.turbine.util.ControlTool.ControlParameters;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.test.app1.module.control.MyControlChangingTemplate;

public class ControlToolTests extends AbstractPullToolTests<ControlTool> {
    @Override
    protected String toolName() {
        return "control";
    }

    @Before
    public void init() throws Exception {
        rundata.getResponse().getWriter();
    }

    @After
    public void destroy() {
        while (rundata.getCurrentContext() != null) {
            rundata.popContext();
        }
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
    public void setTemplateModule() {
        tool.setTemplate("mytemplate").setModule("mymodule");
        assertEquals("mytemplate", getFieldValue(tool.getControlParameters(), "template", String.class));
        assertEquals(null, getFieldValue(tool.getControlParameters(), "module", String.class));
    }

    @Test
    public void setModuleTemplate() {
        tool.setModule("mymodule").setTemplate("mytemplate");
        assertEquals("mymodule", getFieldValue(tool.getControlParameters(), "module", String.class));
        assertEquals(null, getFieldValue(tool.getControlParameters(), "template", String.class));
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
    public void render_changingTemplate() throws Exception {
        // setTemplate
        MyControlChangingTemplate.expectedTemplateName = "myControlChangingTemplate";
        MyControlChangingTemplate.changedTemplateName = "myOtherControl";
        String content = tool.setTemplate("myControlChangingTemplate").render();
        assertEquals("other control", content);

        // setModule
        MyControlChangingTemplate.expectedTemplateName = null;
        MyControlChangingTemplate.changedTemplateName = "myOtherControl";
        content = tool.setModule("myControlChangingTemplate").render();
        assertEquals("other control", content);

        // setTemplate - remove template
        MyControlChangingTemplate.expectedTemplateName = "myControlChangingTemplate";
        MyControlChangingTemplate.changedTemplateName = " ";
        content = tool.setTemplate("myControlChangingTemplate").render();
        assertEquals("", content);
    }

    @Test
    public void render_contextValue() throws Exception {
        String content = tool.setTemplate("myControlContextValue").setParameter("template", "myOtherControl").render();
        assertEquals("other control", content);
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

    @Test
    public void controlContexts() throws Exception {
        Context context1 = rundata.getContext();
        Context context2 = rundata.getContext("app2");
        assertFalse(tool.exportAll);

        context1.put("var1", "init");
        context1.put("var2", "init");

        // no current context
        AbstractContext controlContext1 = (AbstractContext) createContextForControl(null, null);
        assertSame(context1, controlContext1.getParentContext());

        controlContext1.put("var1", "value1");
        assertEquals("value1", controlContext1.get("var1"));
        assertEquals("init", context1.get("var1"));

        controlContext1.remove("var2");
        assertEquals(null, controlContext1.get("var2"));
        assertEquals("init", context1.get("var2"));

        // context1 -> controlContext1, without exports
        rundata.pushContext(context1);

        controlContext1 = (AbstractContext) createContextForControl(null, null);
        assertSame(context1, controlContext1.getParentContext());

        controlContext1.put("var1", "value1");
        assertEquals("value1", controlContext1.get("var1"));
        assertEquals("init", context1.get("var1"));

        controlContext1.remove("var2");
        assertEquals(null, controlContext1.get("var2"));
        assertEquals("init", context1.get("var2"));

        // context1 -> controlContext.export(var1, var2)
        controlContext1 = (AbstractContext) createContextForControl(null, null, "var1", "var2");
        assertSame(context1, controlContext1.getParentContext());

        controlContext1.put("var1", "value1");
        assertEquals("value1", controlContext1.get("var1"));
        assertEquals("value1", context1.get("var1"));

        controlContext1.remove("var2");
        assertEquals(null, controlContext1.get("var2"));
        assertEquals(null, context1.get("var2"));

        // context1 -> controlContext1 -> app2:controlContext2.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");

        controlContext1 = (AbstractContext) createContextForControl(null, null);
        assertSame(context1, controlContext1.getParentContext());

        controlContext1.put("var1", "init");
        controlContext1.put("var2", "init");

        rundata.pushContext(controlContext1);

        AbstractContext controlContext2 = (AbstractContext) createContextForControl(null, "app2", "var1", "var2");
        assertSame(context2, controlContext2.getParentContext());

        controlContext2.put("var1", "value1");
        assertEquals("value1", controlContext2.get("var1"));
        assertEquals("value1", controlContext1.get("var1"));
        assertEquals("init", context1.get("var1"));

        controlContext2.remove("var2");
        assertEquals(null, controlContext2.get("var2"));
        assertEquals(null, controlContext1.get("var2"));
        assertEquals("init", context1.get("var2"));

        rundata.popContext();

        // context1 -> controlContext1.export(var1, var2) -> app2:controlContext2.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");

        controlContext1 = (AbstractContext) createContextForControl(null, null, "var1", "var2");
        assertSame(context1, controlContext1.getParentContext());

        controlContext1.put("var1", "init");
        controlContext1.put("var2", "init");

        rundata.pushContext(controlContext1);

        controlContext2 = (AbstractContext) createContextForControl(null, "app2", "var1", "var2");
        assertSame(context2, controlContext2.getParentContext());

        controlContext2.put("var1", "value1");
        assertEquals("value1", controlContext2.get("var1"));
        assertEquals("value1", controlContext1.get("var1"));
        assertEquals("value1", context1.get("var1"));

        controlContext2.remove("var2");
        assertEquals(null, controlContext2.get("var2"));
        assertEquals(null, controlContext1.get("var2"));
        assertEquals(null, context1.get("var2"));

        rundata.popContext();
    }

    private Context createContextForControl(Map<String, Object> params, String component, String... exports)
            throws Exception {
        Method method = getAccessibleMethod(tool.getClass(), "createContextForControl", new Class<?>[] {
                ControlParameters.class, String.class });

        ControlParameters controlParameters = new ControlParameters();

        if (params != null) {
            controlParameters.putAll(params);
        }

        if (exports != null) {
            getAccessibleField(controlParameters.getClass(), "exportVars").set(controlParameters,
                    createHashSet(exports));
        }

        return (Context) method.invoke(tool, controlParameters, component);
    }

    @Test
    public void render_exportVars() throws Exception {
        Context context1 = rundata.getContext();
        Context context2 = rundata.getContext("app2");
        assertFalse(tool.exportAll);

        context1.put("var1", "init");
        context1.put("var2", "init");

        // no current context
        tool.setTemplate("control_set").render();
        assertEquals("init", context1.get("var1"));
        assertEquals("init", context1.get("var2"));

        // app1:context -> app1:control, without exports
        rundata.pushContext(context1);

        tool.setTemplate("control_set").render();
        assertEquals("init", context1.get("var1"));
        assertEquals("init", context1.get("var2"));

        // app1:context -> app1:control.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");

        tool.setTemplate("control_set").export("var1").render();
        assertEquals("app1", context1.get("var1"));
        assertEquals("init", context1.get("var2"));

        context1.put("var1", "init");
        context1.put("var2", "init");

        tool.setTemplate("control_set").export("var2").render();
        assertEquals("init", context1.get("var1"));
        assertEquals(null, context1.get("var2"));

        context1.put("var1", "init");
        context1.put("var2", "init");

        tool.setTemplate("control_set").export("var1", "var2").render();
        assertEquals("app1", context1.get("var1"));
        assertEquals(null, context1.get("var2"));

        // app1:context -> app1:control -> app2:control.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");
        context2.put("var1", "init");
        context2.put("var2", "init");

        String content = tool.setTemplate("control_nest").render();

        assertEquals("init", context1.get("var1"));
        assertEquals("init", context1.get("var2"));
        assertEquals("init", context2.get("var1"));
        assertEquals("init", context2.get("var2"));

        assertThat(content, containsAll("1. app2", "2. $var2"));

        // app1:context -> app1:control.export(var1, var2) -> app2:control.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");
        context2.put("var1", "init");
        context2.put("var2", "init");

        content = tool.setTemplate("control_nest").export("var1", "var2").render();

        assertEquals("app2", context1.get("var1"));
        assertEquals(null, context1.get("var2"));
        assertEquals("init", context2.get("var1"));
        assertEquals("init", context2.get("var2"));

        assertThat(content, containsAll("1. app2", "2. $var2"));
    }
}
