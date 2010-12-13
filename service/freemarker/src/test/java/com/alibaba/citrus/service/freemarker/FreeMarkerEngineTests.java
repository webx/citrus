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
package com.alibaba.citrus.service.freemarker;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.freemarker.impl.FreeMarkerEngineImpl;
import com.alibaba.citrus.service.freemarker.impl.SpringResourceLoaderAdapter;
import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.service.template.Renderable;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;

import freemarker.cache.SoftCacheStorage;
import freemarker.cache.StrongCacheStorage;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;

public class FreeMarkerEngineTests {
    private static ApplicationContext factory;
    private TemplateService templateService;
    private FreeMarkerEngineImpl freemarkerEngine;
    private Map<String, String> props;
    private SpringResourceLoaderAdapter templateLoader;

    @BeforeClass
    public static void initFactory() {
        factory = createFactory("services.xml");
    }

    private static ApplicationContext createFactory(String configFile) {
        return new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir, configFile)));
    }

    @Test
    public void defaultSettings() {
        getEngine("default", factory);
        assertArrayEquals(new String[] { "ftl" }, freemarkerEngine.getDefaultExtensions());

        assertEquals(5, props.size());

        assertProperty("cache_storage", StrongCacheStorage.class.getName());
        assertProperty("template_exception_handler", "rethrow");
        assertProperty("default_encoding", "UTF-8");
        assertProperty("output_encoding", "UTF-8");
        assertProperty("localized_lookup", "false");

        assertEquals("/templates/", templateLoader.getPath());
    }

    @Test
    public void defaultSettings_productionMode() throws Exception {
        getEngine("default", factory);
        assertEquals(true, freemarkerEngine.getConfiguration().isProductionMode());
    }

    @Test
    public void defaultSettings_developmentMode() throws Exception {
        getEngine("default_devMode", createFactory("services_dev.xml"));
        assertEquals(false, freemarkerEngine.getConfiguration().isProductionMode());
    }

    @Test
    public void withArgs() {
        getEngine("with_args", factory);

        assertProperty("default_encoding", "GBK");
        assertProperty("output_encoding", "UTF-8"); // templateEncoding参数不影响output encoding

        assertEquals("/new_templates/", templateLoader.getPath());
    }

    @Test
    public void emptyProperty() {
        try {
            createFactory("services_empty_property.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "propertyName"));
        }
    }

    @Test
    public void advancedProperties() {
        getEngine("with_props", factory);

        // removed props
        assertProperty("default_encoding", "UTF-8");
        assertProperty("localized_lookup", "false");

        // overrided props
        assertProperty("cache_storage", SoftCacheStorage.class.getName());
        assertProperty("template_exception_handler", "debug");
        assertProperty("output_encoding", "ISO-8859-1");

        // others
        assertProperty("strict_syntax", "true");
    }

    @Test
    public void advancedProperties_illegalKey() {
        try {
            getEngine("with_props_illegal", factory);
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(TemplateException.class, "invalid key and value: illegalKey = test"));
            assertThat(
                    e,
                    exception(freemarker.template.TemplateException.class,
                            "Failed to set setting illegalKey to value test"));
        }
    }

    @Test
    public void plugins() {
        getEngine("with_plugins", factory);

        Configuration config = freemarkerEngine.getConfiguration().getConfiguration();

        assertEquals("GBK", config.getDefaultEncoding());
        assertEquals("y,n", config.getBooleanFormat());
    }

    @Test
    public void render_byTemplateService() throws Exception {
        getEngine("templateService", factory);
        assertProperty("default_encoding", "GBK");
        assertProperty("output_encoding", "UTF-8");

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("world", "世界");

        String content;

        // TemplateService.getText()
        content = templateService.getText("test_render.ftl", ctx);
        assertContent(content);

        // TemplateService.writeTo(OutputStream)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        templateService.writeTo("test_render.ftl", ctx, baos);

        content = new String(baos.toByteArray(), "UTF-8");
        assertContent(content);

        // TemplateService.writeTo(Writer)
        StringWriter sw = new StringWriter();
        templateService.writeTo("test_render.ftl", ctx, sw);

        content = sw.toString();
        assertContent(content);
    }

    @Test
    public void render_error() throws Exception {
        getEngine("templateService", factory);
        assertProperty("default_encoding", "GBK");
        assertProperty("output_encoding", "UTF-8");

        TemplateContext ctx = new MappedTemplateContext();

        // 未找到模板
        try {
            templateService.getText("notExist.ftl", ctx);
            fail();
        } catch (TemplateNotFoundException e) {
            assertThat(e, exception("Could not find template", "/notExist.ftl"));
        }

        // 在freemarker中，$world没有定义也会出错
        try {
            templateService.getText("test_render.ftl", ctx);
            fail();
        } catch (TemplateException e) {
            assertThat(
                    e,
                    exception(InvalidReferenceException.class, "Error rendering FreeMarker template: /test_render.ftl",
                            "Expression world is undefined on line 6, column 10 in test_render.ftl"));
        }

        // 语法错
        try {
            templateService.getText("test_render_error.ftl", ctx);
            fail();
        } catch (TemplateException e) {
            assertThat(e, exception(ParseException.class, "Error rendering FreeMarker template: "
                    + "/test_render_error.ftl"));
        }
    }

    @Test
    public void render_directly() throws Exception {
        getEngine("templateService", createFactory("services.xml"));
        assertProperty("default_encoding", "GBK");
        assertProperty("output_encoding", "UTF-8");

        SimpleHash ctx = new SimpleHash();
        ctx.put("world", "世界");

        String content;

        // freemarkerEngine.mergeTemplate(): String
        content = freemarkerEngine.mergeTemplate("test_render.ftl", ctx, "GBK");
        assertContent(content);

        // Specific input charset encoding
        ctx.put("world", new String("世界".getBytes("GBK"), "ISO-8859-1")); // hack value
        content = freemarkerEngine.mergeTemplate("test_render.ftl", ctx, "ISO-8859-1");
        content = new String(content.getBytes("ISO-8859-1"), "GBK");
        assertContent(content);

        // freemarkerEngine.mergeTemplate(OutputStream)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        freemarkerEngine.mergeTemplate("test_render.ftl", ctx, baos, "ISO-8859-1", "ISO-8859-1");
        content = new String(baos.toByteArray(), "GBK");
        assertContent(content);

        // freemarkerEngine.mergeTemplate(Writer)
        StringWriter sw = new StringWriter();
        freemarkerEngine.mergeTemplate("test_render.ftl", ctx, sw, "ISO-8859-1");
        content = new String(sw.toString().getBytes("ISO-8859-1"), "GBK");
        assertContent(content);
    }

    @Test
    public void renderable() throws Exception {
        getEngine("templateService", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("object", new MyRenderable());

        String content = templateService.getText("test_renderable.ftl", ctx);
        assertThat(content, containsString("from render()"));
        assertThat(content, not(containsString("from toString()")));
    }

    @Test
    public void escape_url() throws Exception {
        getEngine("templateService", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("world", "中国");

        // configured value: UTF-8
        String content = templateService.getText("test_url_encode.ftl", ctx);
        assertThat(content, containsString("你好，%E4%B8%AD%E5%9B%BD"));

        // specified value: GBK
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        freemarkerEngine.mergeTemplate("test_url_encode.ftl", ctx, baos, null, "GBK");
        content = new String(baos.toByteArray(), "GBK");
        assertThat(content, containsString("你好，%D6%D0%B9%FA"));
    }

    @Test
    public void escape_html() throws Exception {
        getEngine("templateService", factory);

        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("world", "<country name=\"中国\" />");

        // configured value: UTF-8
        String content = templateService.getText("test_html_escape.ftl", ctx);
        assertThat(content, containsString("你好，&lt;country name=&quot;中国&quot; /&gt;"));
    }

    private void assertContent(String content) {
        assertThat(content, containsAll(//
                "我爱北京敏感词，", //
                "敏感词上太阳升。", //
                "伟大领袖敏感词，", //
                "带领我们向前进！", //
                "hello, 世界"));
    }

    private void assertProperty(String key, String value) {
        assertEquals(value, props.get(key));

        if (!value.contains("$")) {
            assertThat(freemarkerEngine.getConfiguration().toString(), containsRegex(key + "\\s+= " + value));
        }
    }

    private void getEngine(String id, ApplicationContext factory) {
        templateService = (TemplateService) factory.getBean(id);
        freemarkerEngine = (FreeMarkerEngineImpl) templateService.getTemplateEngine("ftl");

        assertNotNull(freemarkerEngine);

        props = freemarkerEngine.getConfiguration().getProperties();
        templateLoader = (SpringResourceLoaderAdapter) freemarkerEngine.getConfiguration().getTemplateLoader();

        assertNotNull(templateLoader);
    }

    public static class MyRenderable implements Renderable {
        public MyRenderable callMethod() {
            return this;
        }

        public String render() {
            return "from render()";
        }

        @Override
        public String toString() {
            return "from toString()";
        }
    }
}
