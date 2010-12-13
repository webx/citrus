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
package com.alibaba.citrus.springext.support.context;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.meterware.servletunit.ServletRunner;

/**
 * 确保singleton proxy可以工作。设计以下情形：
 * <ul>
 * <li>parent context中被置入了resolvableDependencies。</li>
 * <li>this context中被置入了同名的dependencies。</li>
 * <li>利用autowire注入对象，应该被注入parent中的对象。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class InheritableBeanFactoryTests {
    private XmlWebApplicationContext parentContext;
    private XmlWebApplicationContext thisContext;

    private void initContext(boolean withMockRequest) throws Exception {
        ServletContext servletContext = new ServletRunner(new File(srcdir, "WEB-INF/web.xml"), "").newClient()
                .newInvocation("http://localhost/servlet").getServlet().getServletConfig().getServletContext();

        // parent context，如果withMockRequest，则注册并覆盖原有的request
        parentContext = new XmlWebApplicationContext();
        parentContext.setConfigLocation(withMockRequest ? "beans-autowire-parent.xml" : "beans.xml");
        parentContext.setServletContext(servletContext);
        parentContext.refresh();

        // this context将会重新注册request，但是由于parent中已经注册了，不会被覆盖
        thisContext = new XmlWebApplicationContext();
        thisContext.setConfigLocation("beans-autowire.xml");
        thisContext.setServletContext(servletContext);
        thisContext.setParent(parentContext);
        thisContext.refresh();
    }

    /**
     * 如果parent context中被置入mock request，那么取得并注入到autowire对象中。
     */
    @Test
    public void request1() throws Exception {
        initContext(true);
        MyObject obj = (MyObject) thisContext.getBean("autowiredObject");
        assertEquals("mock_uri", obj.request.getRequestURI());
    }

    /**
     * 如果parent context中没有置入mock request，那么autowire试图取得request失败。
     */
    @Test
    public void request2() throws Exception {
        try {
            initContext(false);
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(IllegalStateException.class, "No thread-bound request found"));
        }
    }

    public static class RequestPostProcessor implements BeanFactoryPostProcessor {
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
            expect(mockRequest.getRequestURI()).andReturn("mock_uri").anyTimes();
            replay(mockRequest);

            beanFactory.registerResolvableDependency(ServletRequest.class, mockRequest);
        }
    }

    public static class MyObject {
        @Autowired(required = false)
        private HttpServletRequest request;
    }
}
