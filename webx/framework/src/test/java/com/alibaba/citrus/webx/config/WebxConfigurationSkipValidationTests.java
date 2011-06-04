package com.alibaba.citrus.webx.config;

import static com.alibaba.citrus.test.TestEnvStatic.*;
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

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl;

@RunWith(Parameterized.class)
public class WebxConfigurationSkipValidationTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private WebxConfigurationImpl config;

    public WebxConfigurationSkipValidationTests(boolean skipValidation) {
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

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                "webx-configuration-skip-validation.xml")));
        config = (WebxConfigurationImpl) factory.getBean("webxConfiguration");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void test() {
        assertEquals(true, config.isProductionMode());
        assertEquals("internal", config.getInternalPathPrefix());
        assertSame(factory.getBean("requestContexts"), config.getRequestContexts());
        assertSame(factory.getBean("pipeline"), config.getPipeline());
        assertEquals(null, config.getExceptionPipeline());
        assertEquals("/WEB-INF/webx-*.xml", config.getComponentsConfig().getComponentConfigurationLocationPattern());
    }
}
