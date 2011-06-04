package com.alibaba.citrus.service.jsp;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JspEngineSkipValidationTests extends AbstractJspEngineTests {
    private final boolean skipValidation;

    public JspEngineSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() throws Exception {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        initServlet("webapp/WEB-INF/web.xml");
        initFactory("services-skip-validation.xml");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void test() {
        assertEquals("/templates/", getFieldValue(engine, "path", null));
    }
}
