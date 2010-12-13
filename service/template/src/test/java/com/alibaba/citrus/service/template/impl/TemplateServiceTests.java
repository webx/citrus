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
package com.alibaba.citrus.service.template.impl;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.util.i18n.LocaleUtil;
import com.alibaba.citrus.util.io.ByteArrayOutputStream;

public class TemplateServiceTests {
    private static ApplicationContext factory;
    private TemplateServiceImpl templateService;

    @BeforeClass
    public static void initContext() {
        factory = createContext("template.xml");
    }

    @After
    public void destroy() {
        System.clearProperty("productionMode");
        LocaleUtil.resetContext();
    }

    @Test
    public void createServiceDirectly_uninited() throws Exception {
        templateService = new TemplateServiceImpl();

        // 未初始化，不能执行exists等方法
        try {
            templateService.exists("test.vm");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Bean instance", "has not been initialized yet"));
        }

        try {
            templateService.getText("test.vm", null);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("Bean instance", "has not been initialized yet"));
        }
    }

    @Test
    public void createServiceDirectly_getExts() throws Exception {
        templateService = new TemplateServiceImpl();
        templateService.afterPropertiesSet();

        assertArrayEquals(new String[0], templateService.getSupportedExtensions());
    }

    @Test
    public void createServiceDirectly_invalidExts() throws Exception {
        for (String ext : new String[] { null, "", ".test" }) {
            Map<String, String> mapping = createHashMap();
            mapping.put(ext, "test");

            templateService = new TemplateServiceImpl();
            templateService.setEngineNameMappings(mapping);

            try {
                templateService.afterPropertiesSet();
                fail();
            } catch (IllegalArgumentException e) {
                assertThat(e, exception("Invalid extension: " + ext));
            }
        }
    }

    @Test
    public void bean_definition_parser_errors() {
        // 缺少extension
        try {
            createContext("template-noExtension.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "extension"));
        }

        // 缺少engineName
        try {
            createContext("template-noEngineName.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "engine"));
        }

        // extension重复
        try {
            createContext("template-dupExtensions.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "duplicated extension: test"));
        }
    }

    @Test
    public void productionMode_default() {
        assertTrue(new TemplateServiceImpl().isProductionMode());
    }

    @Test
    public void cacheEnabled_productionMode() {
        System.setProperty("productionMode", "true");
        ApplicationContext factory = createContext("template-cache.xml");

        templateService = (TemplateServiceImpl) factory.getBean("default");
        assertTrue(templateService.isProductionMode());
        assertTrue(templateService.isCacheEnabled());

        templateService = (TemplateServiceImpl) factory.getBean("cacheEnabled");
        assertTrue(templateService.isProductionMode());
        assertTrue(templateService.isCacheEnabled());

        templateService = (TemplateServiceImpl) factory.getBean("cacheDisabled");
        assertTrue(templateService.isProductionMode());
        assertFalse(templateService.isCacheEnabled());
    }

    @Test
    public void cacheEnabled_devMode() {
        ApplicationContext factory = createContext("template-cache.xml");

        templateService = (TemplateServiceImpl) factory.getBean("default");
        assertFalse(templateService.isProductionMode());
        assertFalse(templateService.isCacheEnabled());

        templateService = (TemplateServiceImpl) factory.getBean("cacheEnabled");
        assertFalse(templateService.isProductionMode());
        assertTrue(templateService.isCacheEnabled());

        templateService = (TemplateServiceImpl) factory.getBean("cacheDisabled");
        assertFalse(templateService.isProductionMode());
        assertFalse(templateService.isCacheEnabled());
    }

    @Test
    public void empty_getExts() {
        templateService = (TemplateServiceImpl) factory.getBean("empty");
        assertArrayEquals(new String[0], templateService.getSupportedExtensions());
    }

    @Test
    public void engineNotDefined() {
        try {
            factory.getBean("engineNotDefined");
            fail();
        } catch (FatalBeanException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class,
                            "TemplateEngine \"notDefinied\" not defined.  Defined names: [myEngine]"));
        }
    }

    @Test
    public void extensions() {
        templateService = (TemplateServiceImpl) factory.getBean("exts");

        TemplateEngine engine1 = templateService.getEngineOfName("myEngine1");
        TemplateEngine engine2 = templateService.getEngineOfName("myEngine2");

        assertArrayEquals(new String[] { "jhtml", "jsp", "vm1" }, templateService.getSupportedExtensions());

        // specificly mapped extensions override default ones
        assertEquals(null, templateService.getTemplateEngine("vm"));
        assertSame(engine1, templateService.getTemplateEngine("vm1"));

        // default extensions
        assertSame(engine2, templateService.getTemplateEngine("jsp"));
        assertSame(engine2, templateService.getTemplateEngine("jhtml"));
    }

    @Test
    public void search_ByDefaultExt() throws Exception {
        templateService = (TemplateServiceImpl) factory.getBean("defaultExt");

        // use default ext
        assertTemplate("/dir1/template1.vm", "template1");

        // use specified ext
        assertTemplate("/dir2/template1.jsp", "template1.jsp");
    }

    @Test
    public void search_AllExts() throws Exception {
        templateService = (TemplateServiceImpl) factory.getBean("searchExts");

        // sort by ext: jsp before vm
        assertTemplate("/dir2/template1.jsp", "template1"); // no ext
        assertTemplate("/dir2/template1.jsp", "template1.jhtml"); // ext specified

        // try specified ext first
        assertTemplate("/dir1/template1.vm", "template1.vm");
    }

    @Test
    public void search_Locales() throws Exception {
        templateService = (TemplateServiceImpl) factory.getBean("searchLocales");

        try {
            templateService.getText("template1", null);
            fail();
        } catch (TemplateNotFoundException e) {
            assertThat(e, exception("Could not find template \"/template1\""));
        }

        try {
            templateService.getText("template1.jhtml", null);
            fail();
        } catch (TemplateNotFoundException e) {
            assertThat(e, exception("Could not find template \"/template1.jhtml\""));
        }

        LocaleUtil.setContext(Locale.CHINA);

        assertTemplate("/dir1/template1.vm", "template1.vm");
        assertTemplate("/dir2/template1.jsp", "template1.jsp");
        assertTemplate("/dir1/template2_zh_CN.vm", "template2.vm");
        assertTemplate("/dir2/template3_zh.jhtml", "template3.jhtml");
    }

    @Test
    public void search_combine_strategies() throws Exception {
        templateService = (TemplateServiceImpl) factory.getBean("templateService");

        LocaleUtil.setContext(Locale.CHINA);

        // default ext=vm
        assertTemplate("/dir1/template1.vm", "template1");

        // found directly
        assertTemplate("/dir2/template1.jsp", "template1.jsp");

        // search exts
        assertTemplate("/dir2/template1.jsp", "template1.jhtml");

        // default ext=vm, localized
        assertTemplate("/dir1/template2_zh_CN.vm", "template2");

        // search exts, localized
        assertTemplate("/dir1/template2_zh_CN.vm", "template2.jsp");

        // default ext=vm, search exts, localized
        assertTemplate("/dir2/template3_zh.jhtml", "template3");
    }

    @Test
    public void search_noCache() throws Exception {
        templateService = (TemplateServiceImpl) factory.getBean("templateService");
        assertFalse(templateService.isCacheEnabled());

        LocaleUtil.setContext(Locale.CHINA);
        TemplateMatchResult result1;
        TemplateMatchResult result2;

        // template1
        result1 = templateService.findTemplate("template1");
        assertEquals("/template1.vm", result1.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result1.getEngine().toString());

        result2 = templateService.findTemplate("template1");
        assertEquals("/template1.vm", result2.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result2.getEngine().toString());
        assertNotSame(result1, result2);

        // template2.jsp, search exts, localized
        result1 = templateService.findTemplate("template2.jsp");
        assertEquals("/template2_zh_CN.vm", result1.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result1.getEngine().toString());

        result2 = templateService.findTemplate("template2.jsp");
        assertEquals("/template2_zh_CN.vm", result2.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result2.getEngine().toString());
        assertNotSame(result1, result2);

        // template2.jsp, in English locale
        LocaleUtil.setContext(Locale.US);
        result1 = templateService.findTemplate("template2.jsp");
        assertEquals("/template2.vm", result1.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result1.getEngine().toString());

        result2 = templateService.findTemplate("template2.jsp");
        assertEquals("/template2.vm", result2.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result2.getEngine().toString());
        assertNotSame(result1, result2);
    }

    @Test
    public void search_withCache() throws Exception {
        System.setProperty("productionMode", "true");
        ApplicationContext factory = createContext("template.xml");

        templateService = (TemplateServiceImpl) factory.getBean("templateService");
        assertTrue(templateService.isCacheEnabled());

        LocaleUtil.setContext(Locale.CHINA);
        TemplateMatchResult result1;
        TemplateMatchResult result2;

        // template1
        result1 = templateService.findTemplate("template1");
        assertEquals("/template1.vm", result1.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result1.getEngine().toString());

        result2 = templateService.findTemplate("template1");
        assertSame(result1, result2);

        // template2.jsp, search exts, localized
        result1 = templateService.findTemplate("template2.jsp");
        assertEquals("/template2_zh_CN.vm", result1.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result1.getEngine().toString());

        result2 = templateService.findTemplate("template2.jsp");
        assertSame(result1, result2);

        // template2.jsp, in English locale
        LocaleUtil.setContext(Locale.US);
        result1 = templateService.findTemplate("template2.jsp");
        assertEquals("/template2.vm", result1.getTemplateName());
        assertEquals("SimpleEngine[dir1]", result1.getEngine().toString());

        result2 = templateService.findTemplate("template2.jsp");
        assertSame(result1, result2);
    }

    private void assertTemplate(String result, String templateName) throws Exception {
        // exists
        assertTrue(templateName + " not exist", templateService.exists(templateName));

        // getText
        assertEquals(result, templateService.getText(templateName, null));

        // writeTo stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        templateService.writeTo(templateName, null, baos);

        assertEquals(result, new String(baos.toByteArray().toByteArray()));

        // writeTo writer
        StringWriter sw = new StringWriter();
        templateService.writeTo(templateName, null, sw);

        assertEquals(result, sw.toString());
    }

    private static ApplicationContext createContext(String name) {
        return new XmlApplicationContext(new FileSystemResource(new File(srcdir, name)));
    }
}
