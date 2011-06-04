package com.alibaba.citrus.service.freemarker;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.freemarker.impl.FreeMarkerConfigurationImpl;
import com.alibaba.citrus.service.freemarker.impl.FreeMarkerEngineImpl;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

@RunWith(Parameterized.class)
public class FreeMarkerEngineSkipValidationTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private FreeMarkerEngineImpl freeMarkerEngine;

    public FreeMarkerEngineSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "services-skip-validation.xml")));
        TemplateService templateService = (TemplateService) factory.getBean("templateService");
        freeMarkerEngine = (FreeMarkerEngineImpl) templateService.getTemplateEngine("ftl");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void configuration() {
        FreeMarkerConfigurationImpl config = freeMarkerEngine.getConfiguration();

        assertEquals("/templates", getFieldValue(config, "path", null));
        assertEquals("UTF-8", getFieldValue(config, "charset", null));
    }
}
