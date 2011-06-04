package com.alibaba.citrus.service.form;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.form.impl.FormServiceImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;

@RunWith(Parameterized.class)
public class FormServiceSkipValidationTests extends AbstractFormServiceTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private FormConfigImpl config;

    public FormServiceSkipValidationTests(boolean skipValidation) {
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

        factory = createContext("services-skip-validation.xml", true);
        formService = (FormServiceImpl) factory.getBean("formService");
        config = (FormConfigImpl) formService.getFormConfig();
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void formConfig() {
        assertEquals(true, config.isConverterQuiet());
        assertEquals(true, config.isPostOnlyByDefault());
        assertEquals("form.", config.getMessageCodePrefix());
    }

    @Test
    public void groupConfig() {
        assertEquals(true, config.getGroupConfig("test").isPostOnly());
        assertEquals(true, config.getGroupConfig("test").isTrimmingByDefault());
    }
}
