package com.alibaba.citrus.service.uribroker;

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

import com.alibaba.citrus.service.uribroker.impl.URIBrokerServiceImpl;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

@RunWith(Parameterized.class)
public class URIBrokerSkipValidationTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private URIBrokerServiceImpl uris;

    public URIBrokerSkipValidationTests(boolean skipValidation) {
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

        factory = new XmlApplicationContext(
                new FileSystemResource(new File(srcdir, "services-uri-skip-validation.xml")));

        uris = (URIBrokerServiceImpl) factory.getBean("uris");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void service() {
        assertEquals(false, uris.isRequestAware());

        assertTrue(uris.getNames().contains("u1"));
        assertFalse(uris.getExposedNames().contains("u1"));

        assertTrue(uris.getNames().contains("u2"));
        assertFalse(uris.getExposedNames().contains("u2"));
    }
}
