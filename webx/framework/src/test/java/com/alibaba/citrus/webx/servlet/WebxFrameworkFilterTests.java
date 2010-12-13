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
package com.alibaba.citrus.webx.servlet;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.util.CollectionUtil;
import com.alibaba.citrus.webx.AbstractWebxTests;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.config.WebxConfiguration;
import com.alibaba.citrus.webx.support.AbstractWebxController;
import com.alibaba.citrus.webx.util.WebxUtil;
import com.meterware.servletunit.PatchedServletRunner;

public class WebxFrameworkFilterTests extends AbstractWebxTests {
    private WebxFrameworkFilter filter;
    private WebxComponents components;

    @Before
    public void init() throws Exception {
        MyController.resetAttributes();

        prepareWebClient(null);

        filter = (WebxFrameworkFilter) client.newInvocation("http://www.taobao.com/app1").getFilter();
        assertNotNull(filter);

        components = filter.getWebxComponents();
        assertNotNull(components);
    }

    @Test
    public void excludes() throws Exception {
        Pattern[] excludes;

        // no excludes - default value
        excludes = getFieldValue(filter, "excludes", Pattern[].class);
        assertNull(excludes);

        // set empty excludes
        filter.setExcludes(" ");
        excludes = getFieldValue(filter, "excludes", Pattern[].class);
        assertNull(excludes);

        filter.setExcludes(" \r\n, ");
        excludes = getFieldValue(filter, "excludes", Pattern[].class);
        assertNull(excludes);

        // with excludes
        filter.setExcludes("/aa , *.jpg");
        excludes = getFieldValue(filter, "excludes", Pattern[].class);
        assertEquals(2, excludes.length);

        filter.setExcludes("/aa  *.jpg");
        excludes = getFieldValue(filter, "excludes", Pattern[].class);
        assertEquals(2, excludes.length);

        filter.setExcludes("/aa\r\n*.jpg");
        excludes = getFieldValue(filter, "excludes", Pattern[].class);
        assertEquals(2, excludes.length);
    }

    @Test
    public void isExcluded() throws Exception {
        filter.setExcludes("/aa , *.jpg");

        assertExcluded(true, "/aa/bb");
        assertExcluded(false, "/cc/aa/bb");

        assertExcluded(true, "/cc/test.jpg");
        assertExcluded(true, "/cc/aa/bb/test.jpg");

        assertExcluded(false, "/cc/aa/bb/test.htm");
    }

    private void assertExcluded(boolean excluded, String requestURI) throws Exception {
        Method isExcluded = getAccessibleMethod(filter.getClass(), "isExcluded",
                new Class<?>[] { HttpServletRequest.class });

        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain filterChain = createMock(FilterChain.class);

        expect(request.getRequestURI()).andReturn(requestURI).anyTimes();

        if (excluded) {
            filterChain.doFilter(request, response);
        }

        replay(request, response, filterChain);

        assertEquals(excluded, isExcluded.invoke(filter, request));

        if (excluded) {
            filter.doFilter(request, response, filterChain); // 对excluded request调用doFilter，应该立即返回
        }

        verify(request, response, filterChain);
    }

    @Test
    public void componentPath_wrong() throws Exception {
        File webInf = new File(srcdir, "app2/WEB-INF");
        File webXml = new File(webInf, "web.xml");

        try {
            new PatchedServletRunner(webXml, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("default component \"app1\" should not have component path \"/my/app1\""));
        }
    }

    @Test
    public void getComponent() throws Exception {
        assertNull(components.getComponent("notexist"));

        // root component
        WebxComponent rootComponent = components.getComponent(null);

        assertNull(rootComponent.getName());
        assertEquals("", rootComponent.getComponentPath());
        assertSame(components.getParentWebxConfiguration(), rootComponent.getWebxConfiguration());
        assertSame(components.getParentApplicationContext(), rootComponent.getApplicationContext());
        assertSame(components, rootComponent.getWebxComponents());
        assertEquals(components.toString(), rootComponent.toString());

        try {
            rootComponent.getWebxController();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("RootComponent.getWebxController()"));
        }
    }

    @Test
    public void findComponent() throws Exception {
        WebxComponent app1 = components.getComponent("app1");
        WebxComponent app4 = components.getComponent("app4");
        WebxComponent app5 = components.getComponent("app5");

        assertEquals("/app1", app1.getComponentPath());
        assertEquals("", app4.getComponentPath());
        assertEquals("/my/app5", app5.getComponentPath());

        assertSame(app1, components.findMatchedComponent("app1"));
        assertSame(app1, components.findMatchedComponent("app1/"));
        assertSame(app1, components.findMatchedComponent("/app1"));
        assertSame(app1, components.findMatchedComponent("/app1/test"));

        // default
        assertSame(app4, components.findMatchedComponent("/app1_test"));

        // specified component path
        assertSame(app4, components.findMatchedComponent("/app5"));
        assertSame(app5, components.findMatchedComponent("/my/app5"));
        assertSame(app5, components.findMatchedComponent("/my/app5/test"));
    }

    @Test
    public void readResource_app4_defaultComponent() throws Exception {
        invokeServlet("/plaintext.txt");

        assertEquals(200, clientResponseCode);
        assertThat(clientResponseContent, containsAll("hello, app4"));

        assertEquals("yes", MyController.getAttribute("onRefresh"));
        assertEquals("yes", MyController.getAttribute("postProcessWebApplicationContext"));
        assertEquals(components.getComponent("app4"), MyController.getAttribute("handleRequest"));
    }

    @Test
    public void readResource_app5() throws Exception {
        invokeServlet("/my/app5/plaintext.txt");

        assertEquals(200, clientResponseCode);
        assertThat(clientResponseContent, containsAll("hello, app5"));

        assertEquals("yes", MyController.getAttribute("onRefresh"));
        assertEquals("yes", MyController.getAttribute("postProcessWebApplicationContext"));
        assertEquals(components.getComponent("app5"), MyController.getAttribute("handleRequest"));
    }

    @Test
    public void autowireComponentObject() {
        ComponentsAware componentsAware;

        // parent context
        componentsAware = getComponentsAware(components.getParentApplicationContext());
        assertSame(components, componentsAware.components);
        assertNull(componentsAware.component);

        // component context
        componentsAware = getComponentsAware(components.getComponent("app1").getApplicationContext());
        assertSame(components, componentsAware.components);
        assertSame(components.getComponent("app1"), componentsAware.component);

        componentsAware = getComponentsAware(components.getComponent("app2").getApplicationContext());
        assertSame(components, componentsAware.components);
        assertSame(components.getComponent("app2"), componentsAware.component);

        componentsAware = getComponentsAware(components.getComponent("app3").getApplicationContext());
        assertSame(components, componentsAware.components);
        assertSame(components.getComponent("app3"), componentsAware.component);

        componentsAware = getComponentsAware(components.getComponent("app4").getApplicationContext());
        assertSame(components, componentsAware.components);
        assertSame(components.getComponent("app4"), componentsAware.component);

        componentsAware = getComponentsAware(components.getComponent("app5").getApplicationContext());
        assertSame(components, componentsAware.components);
        assertSame(components.getComponent("app5"), componentsAware.component);
    }

    @Test
    public void getConfiguration_fromComponent() {
        // parent configuration
        WebxConfiguration parentConfiguration = (WebxConfiguration) components.getParentApplicationContext().getBean(
                "webxConfiguration");
        assertNotNull(parentConfiguration);

        // component's configureation
        WebxConfiguration configuration = components.getComponent("app1").getWebxConfiguration();
        assertNotSame(parentConfiguration, configuration);
        assertSame(parentConfiguration, getFieldValue(configuration, "parent", WebxConfiguration.class));
    }

    @Test
    public void autowireController() {
        MyController controller = (MyController) components.getComponent("app4").getWebxController();

        // get component
        assertEquals("app4", controller.getComponent().getName());

        // get others
        assertEquals(controller.getComponent(), controller.componentsAware.component);
    }

    private ComponentsAware getComponentsAware(WebApplicationContext context) {
        return (ComponentsAware) context.getBean("componentsAware");
    }

    public static class ComponentsAware {
        @Autowired(required = false)
        private WebxComponent component;

        @Autowired
        private WebxComponents components;
    }

    public static class MyController extends AbstractWebxController {
        private static ThreadLocal<Map<String, Object>> log = new ThreadLocal<Map<String, Object>>();

        @Autowired
        private ComponentsAware componentsAware;

        public static void resetAttributes() {
            log.set(CollectionUtil.<String, Object> createHashMap());
        }

        public static Object getAttribute(String key) {
            return log.get().get(key);
        }

        public static void setAttribute(String key, Object value) {
            Map<String, Object> attrs = log.get();

            if (attrs != null) {
                attrs.put(key, value);
            }
        }

        @Override
        public void onRefreshContext() throws BeansException {
            super.onRefreshContext();
            setAttribute("onRefresh", "yes");
        }

        @Override
        public void onFinishedProcessContext() {
            super.onFinishedProcessContext();
            setAttribute("postProcessWebApplicationContext", "yes");
        }

        public boolean service(RequestContext requestContext) throws Exception {
            setAttribute("handleRequest", getComponent());

            // 测试component是否被置入request attrs
            assertSame(getComponent(), WebxUtil.getCurrentComponent(requestContext.getRequest()));

            return false;
        }
    }
}
