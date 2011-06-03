package com.alibaba.citrus.webx.servlet;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.webx.AbstractWebxTests;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxComponents;
import com.meterware.servletunit.PatchedServletRunner;
import com.meterware.servletunit.ServletRunner;

/**
 * 测试从classpath中装载配置文件。
 * 
 * @author Michael Zhou
 */
public class WebxClasspathTests extends AbstractWebxTests {
    private WebxFrameworkFilter filter;
    private WebxComponents components;

    @Before
    public void init() throws Exception {
        File webInf = new File(srcdir, "app3/WEB-INF");
        File webXml = new File(webInf, "web.xml");

        ServletRunner servletRunner = new PatchedServletRunner(webXml, "");
        client = servletRunner.newClient();

        filter = (WebxFrameworkFilter) client.newInvocation("http://www.taobao.com/app1").getFilter();
        assertNotNull(filter);

        components = filter.getWebxComponents();
        assertNotNull(components);
    }

    @Test
    public void illegal_configLocationPattern() throws Exception {
        File webInf = new File(srcdir, "app4/WEB-INF");
        File webXml = new File(webInf, "web.xml");

        try {
            new PatchedServletRunner(webXml, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e,
                    exception("Invalid componentConfigurationLocationPattern: classpath*:META-INF/mycomponent.xml"));
        }
    }

    @Test
    public void components() throws Exception {
        assertArrayEquals(new String[] { "a", "b" }, components.getComponentNames());

        WebxComponent a = components.getComponent("a");
        WebxComponent b = components.getComponent("b");

        Method m = getAccessibleMethod(a.getApplicationContext().getClass(), "getConfigLocations", null);

        assertArrayEquals(new String[] { "classpath:META-INF/mycomponent/a.xml" },
                String[].class.cast(m.invoke(a.getApplicationContext())));

        assertArrayEquals(new String[] { "classpath:META-INF/mycomponent/b.xml" },
                String[].class.cast(m.invoke(b.getApplicationContext())));

        assertEquals("hello", a.getApplicationContext().getBean("s1"));
        assertEquals("world", b.getApplicationContext().getBean("s2"));
    }
}
