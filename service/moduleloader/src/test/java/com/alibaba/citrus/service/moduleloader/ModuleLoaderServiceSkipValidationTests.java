package com.alibaba.citrus.service.moduleloader;

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

import com.alibaba.citrus.service.moduleloader.impl.ModuleLoaderServiceImpl;
import com.alibaba.citrus.service.moduleloader.impl.factory.ClassModuleFactory;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

@RunWith(Parameterized.class)
public class ModuleLoaderServiceSkipValidationTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private ModuleLoaderServiceImpl moduleLoaderService;

    public ModuleLoaderServiceSkipValidationTests(boolean skipValidation) {
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
        moduleLoaderService = (ModuleLoaderServiceImpl) factory.getBean("moduleLoaderService");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void defaultAdapters() {
        ModuleAdapterFactory[] adapters = getFieldValue(moduleLoaderService, "adapters", ModuleAdapterFactory[].class);
        assertTrue(adapters.length > 0);
    }

    @Test
    public void classModule() {
        ModuleFactory[] factories = getFieldValue(moduleLoaderService, "factories", ModuleFactory[].class);
        ClassModuleFactory classModuleFactory = (ClassModuleFactory) factories[0];
        assertTrue(classModuleFactory.getModule("screen", "MyScreen") instanceof MyScreen); // 说明include-filter有效
    }
}
