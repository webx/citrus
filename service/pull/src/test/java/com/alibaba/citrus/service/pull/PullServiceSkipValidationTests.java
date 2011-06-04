package com.alibaba.citrus.service.pull;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.pull.impl.PullServiceImpl;
import com.alibaba.citrus.service.pull.support.BeanTool;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

@RunWith(Parameterized.class)
public class PullServiceSkipValidationTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private PullServiceImpl pullService;
    private Map<String, Object> tools;
    private Map<String, Object> prePulledTools;

    public PullServiceSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "services-skip-validation.xml")));
        pullService = (PullServiceImpl) factory.getBean("pullService");
        tools = getFieldValue(pullService, "tools", Map.class);
        prePulledTools = getFieldValue(pullService, "prePulledTools", Map.class);
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void beanTool() {
        BeanTool bean = (BeanTool) tools.get("b");
        assertEquals(false, bean.isAutowire());
    }

    @Test
    public void constants() {
        assertTrue(prePulledTools.get("c") instanceof ToolSetFactory); // exposed=true
        assertEquals(HttpServletRequest.BASIC_AUTH, prePulledTools.get("BASIC_AUTH"));
    }
}
