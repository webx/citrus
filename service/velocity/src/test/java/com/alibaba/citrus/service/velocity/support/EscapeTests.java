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
package com.alibaba.citrus.service.velocity.support;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.velocity.exception.ParseErrorException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;
import com.alibaba.citrus.service.velocity.AbstractVelocityEngineTests;
import com.alibaba.citrus.service.velocity.VelocityEngineTests.Counter;
import com.alibaba.citrus.service.velocity.VelocityEngineTests.MyRenderable;
import com.alibaba.citrus.service.velocity.support.EscapeSupport.EscapeType;
import com.alibaba.citrus.test.TestUtil;
import com.alibaba.citrus.util.StringEscapeUtil;

public class EscapeTests extends AbstractVelocityEngineTests {
    @BeforeClass
    public static void initFactory() {
        factory = createFactory("services_escape.xml");
    }

    @Test
    public void normalizeReference_() throws Exception {
        // empty
        assertEquals(" ", normalizeReference(" "));
        assertEquals("", normalizeReference(null));
        assertEquals("", normalizeReference("$ "));
        assertEquals("", normalizeReference("$! "));

        // 有{}
        assertEquals("\" a${b}c \"", normalizeReference(" $ { \" a${b}c \" } "));
        assertEquals("\" a${b}c \"", normalizeReference(" $ ! { \" a${b}c \" } "));

        // 无{}
        assertEquals("\" a${b}c \"", normalizeReference(" $  \" a${b}c \"  "));
        assertEquals("\" a${b}c \"", normalizeReference(" $ !  \" a${b}c \"  "));
        assertEquals("abc", normalizeReference(" $ ! abc  "));
        assertEquals("abc", normalizeReference(" $ abc  "));

        // 非法
        assertEquals("  { \" a${b}c \"  ", normalizeReference("  { \" a${b}c \"  "));
    }

    private String normalizeReference(String ref) throws Exception {
        Method method = TestUtil.getAccessibleMethod(EscapeSupport.class, "normalizeReference",
                new Class<?>[] { String.class });

        return (String) method.invoke(null, ref);
    }

    @Test
    public void escape_directive_wrong() throws Exception {
        getEngine("with_escape", factory);

        TemplateContext ctx = new MappedTemplateContext();

        try {
            templateService.getText("escape/test_escape_wrong_args_0.vm", ctx);
            fail();
        } catch (TemplateException e) {
            assertThat(
                    e,
                    exception(ParseErrorException.class,
                            "Error rendering Velocity template: /escape/test_escape_wrong_args_0.vm",
                            "Invalid args for #escape.  Expected 1 and only 1 string arg."));
        }

        try {
            templateService.getText("escape/test_escape_wrong_args_1.vm", ctx);
            fail();
        } catch (TemplateException e) {
            assertThat(
                    e,
                    exception(ParseErrorException.class,
                            "Error rendering Velocity template: /escape/test_escape_wrong_args_1.vm",
                            "Invalid args for #noescape.  Expected 0 args."));
        }

        try {
            templateService.getText("escape/test_escape_wrong_args_2.vm", ctx);
            fail();
        } catch (TemplateException e) {
            assertThat(
                    e,
                    exception(ParseErrorException.class,
                            "Error rendering Velocity template: /escape/test_escape_wrong_args_2.vm",
                            "Invalid args for #escape.  Expected 1 and only 1 string arg."));
        }

        try {
            templateService.getText("escape/test_escape_wrong_type.vm", ctx);
            fail();
        } catch (TemplateException e) {
            assertThat(
                    e,
                    exception(ParseErrorException.class,
                            "Error rendering Velocity template: /escape/test_escape_wrong_type.vm",
                            "Invalid escape type: unknown at /escape/test_escape_wrong_type.vm",
                            "Available escape types: [noescape, java, javascript, html, xml, url, sql]"));
        }
    }

    @Test
    public void escape_directive() throws Exception {
        getEngine("with_escape", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("count", new Counter());
        ctx.put("object", new MyRenderable("<world name=\"中国\" />"));

        String content = templateService.getText("escape/test_escape.vm", ctx);

        // original
        assertThat(content, containsString("1. <world name=\"中国\" />"));

        // html
        assertThat(content, containsString("2. &lt;world name=&quot;中国&quot; /&gt;"));

        // javascript
        assertThat(content, containsString("3. <world name=\\\"中国\\\" \\/>"));

        // html (restored)
        assertThat(content, containsString("4. &lt;world name=&quot;中国&quot; /&gt;"));

        // noescape
        assertThat(content, containsString("5. <world name=\"中国\" />"));

        // url encoding
        assertThat(content, containsString("6. %3Cworld+name%3D%22%D6%D0%B9%FA%22+%2F%3E"));

        // noescape (restored)
        assertThat(content, containsString("7. <world name=\"中国\" />"));

        // html (restored)
        assertThat(content, containsString("8. &lt;world name=&quot;中国&quot; /&gt;"));

        // original (restored)
        assertThat(content, containsString("9. <world name=\"中国\" />"));

        assertFalse(ctx.containsKey("_ESCAPE_SUPPORT_TYPE_"));
    }

    @Test
    public void escape_directive_default() throws Exception {
        getEngine("with_defaultEscape", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("count", new Counter());
        ctx.put("object", new MyRenderable("<world name=\"中国\" />"));

        String content = templateService.getText("escape/test_escape.vm", ctx);

        // original - default html
        assertThat(content, containsString("1. &lt;world name=&quot;中国&quot; /&gt;"));

        // html
        assertThat(content, containsString("2. &lt;world name=&quot;中国&quot; /&gt;"));

        // javascript
        assertThat(content, containsString("3. <world name=\\\"中国\\\" \\/>"));

        // html (restored)
        assertThat(content, containsString("4. &lt;world name=&quot;中国&quot; /&gt;"));

        // noescape
        assertThat(content, containsString("5. <world name=\"中国\" />"));

        // url encoding
        assertThat(content, containsString("6. %3Cworld+name%3D%22%D6%D0%B9%FA%22+%2F%3E"));

        // noescape (restored)
        assertThat(content, containsString("7. <world name=\"中国\" />"));

        // html (restored)
        assertThat(content, containsString("8. &lt;world name=&quot;中国&quot; /&gt;"));

        // original (restored) - default html
        assertThat(content, containsString("9. &lt;world name=&quot;中国&quot; /&gt;"));

        assertFalse(ctx.containsKey("_ESCAPE_SUPPORT_TYPE_"));
    }

    @Test
    public void escape_directive_default_interplate() throws Exception {
        getEngine("with_defaultEscape", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("count", new Counter());
        ctx.put("object", new MyRenderable("<world name=\"中国\" />"));

        String content = templateService.getText("escape/test_escape_interpolation.vm", ctx);

        assertThat(content, containsString("1. &lt;world name=&quot;中国&quot; /&gt;"));
    }

    @Test
    public void escape_directive_parse() throws Exception {
        getEngine("with_escape", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("count", new Counter());
        ctx.put("object", new MyRenderable("<world name=\"中国\" />"));

        String content = templateService.getText("escape/test_escape_parse.vm", ctx);

        assertThat(content, containsString("1. &lt;world name=&quot;中国&quot; /&gt;"));

        // escape将会影响parse的结果：这未必是我们想要的结果，但目前没有办法。
        assertThat(content, containsString("2. &lt;world name=&quot;中国&quot; /&gt;"));
    }

    @Test
    public void escape_directive_all_types() throws Exception {
        getEngine("with_escape", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("count", new Counter());
        ctx.put("object", new MyRenderable("<world name=\"'中国'\" />"));

        String content = templateService.getText("escape/test_escape_all_types.vm", ctx);

        // noescape
        assertThat(content, containsString("1. <world name=\"'中国'\" />"));

        // java
        assertThat(content, containsString("2. <world name=\\\"'中国'\\\" />"));

        // javascript
        assertThat(content, containsString("3. <world name=\\\"\\'中国\\'\\\" \\/>"));

        // html
        assertThat(content, containsString("4. &lt;world name=&quot;&#39;中国&#39;&quot; /&gt;"));

        // xml
        assertThat(content, containsString("5. &lt;world name=&quot;&apos;中国&apos;&quot; /&gt;"));

        // url
        assertThat(content, containsString("6. %3Cworld+name%3D%22'%D6%D0%B9%FA'%22+%2F%3E"));

        // sql
        assertThat(content, containsString("7. <world name=\"''中国''\" />"));

        assertFalse(ctx.containsKey("_ESCAPE_SUPPORT_TYPE_"));
    }

    @Test
    public void escape_directive_macros() throws Exception {
        getEngine("with_escape", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("count", new Counter());
        ctx.put("object", new MyRenderable("<world name=\"'中国'\" />"));

        String content = templateService.getText("escape/test_escape_macros.vm", ctx);

        // noescape
        assertThat(content, containsString("1. <world name=\"'中国'\" />"));

        // java
        assertThat(content, containsString("2. <world name=\\\"'中国'\\\" />"));

        // javascript/js
        assertThat(content, containsString("3. <world name=\\\"\\'中国\\'\\\" \\/>"));
        assertThat(content, containsString("4. <world name=\\\"\\'中国\\'\\\" \\/>"));

        // html
        assertThat(content, containsString("5. &lt;world name=&quot;&#39;中国&#39;&quot; /&gt;"));

        // xml
        assertThat(content, containsString("6. &lt;world name=&quot;&apos;中国&apos;&quot; /&gt;"));

        // url
        assertThat(content, containsString("7. %3Cworld+name%3D%22'%D6%D0%B9%FA'%22+%2F%3E"));

        // sql
        assertThat(content, containsString("8. <world name=\"''中国''\" />"));

        assertFalse(ctx.containsKey("_ESCAPE_SUPPORT_TYPE_"));
    }

    @Test
    public void escape_with_rules() throws Exception {
        escape_with_rules_internal(factory);

        assertFalse(velocityEngine.getConfiguration().isProductionMode());

        EscapeSupport escapeSupport = (EscapeSupport) getFieldValue(velocityEngine.getConfiguration(), "plugins",
                Object[].class)[0];

        assertFalse(getFieldValue(escapeSupport, "cacheReferences", Boolean.class));

        Map<String, EscapeType> cacheReferences = getFieldValue(escapeSupport, "referenceCache", null);

        assertTrue(cacheReferences.isEmpty());
    }

    @Test
    public void escape_with_rules_productionMode() throws Exception {
        ApplicationContext factory;

        try {
            System.setProperty("productionMode", "true");
            factory = createFactory("services_escape.xml");
            escape_with_rules_internal(factory);
        } finally {
            System.clearProperty("productionMode");
        }

        assertTrue(velocityEngine.getConfiguration().isProductionMode());

        EscapeSupport escapeSupport = (EscapeSupport) getFieldValue(velocityEngine.getConfiguration(), "plugins",
                Object[].class)[0];

        assertTrue(getFieldValue(escapeSupport, "cacheReferences", Boolean.class));

        Map<String, EscapeType> cacheReferences = getFieldValue(escapeSupport, "referenceCache", null);

        assertFalse(cacheReferences.isEmpty());
    }

    private void escape_with_rules_internal(ApplicationContext factory) throws Exception {
        getEngine("with_rules", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("value", "<world name=\"'中国'\" />");
        ctx.put("jsValue", "<world name=\"'中国'\" />");
        ctx.put("control", new MyControl());
        ctx.put("Screen_Placeholder", "<world name=\"'中国'\" />");
        ctx.put("stringescapeutil", new StringEscapeUtil());

        String content = templateService.getText("escape/test_escape_rules.vm", ctx);

        // default = html
        assertThat(content, containsString("1. &lt;world name=&quot;&#39;中国&#39;&quot; /&gt;"));

        // js* = javascript
        assertThat(content, containsString("2. <world name=\\\"\\'中国\\'\\\" \\/>"));

        // control* = noescape
        assertThat(content, containsString("3. <hello />"));

        // screen_placeholder* = noescape, case insensitive
        assertThat(content, containsString("4. <world name=\"'中国'\" />"));

        // stringescapeutil.escape* = noescape, case insensitive
        assertThat(content, containsString("5. %3Chello+%2F%3E"));

        // 指定escape = html
        assertThat(content, containsString("6. &lt;world name=&quot;&#39;中国&#39;&quot; /&gt;"));

        // 指定noescape
        assertThat(content, containsString("7. <world name=\"'中国'\" />"));
    }

    public static class MyControl {
        private String value;

        public MyControl setValue(String value) {
            this.value = value;
            return this;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
