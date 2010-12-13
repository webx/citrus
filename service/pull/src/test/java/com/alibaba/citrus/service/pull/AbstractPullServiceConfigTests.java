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
package com.alibaba.citrus.service.pull;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.pull.tool.BaseFactory;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.test.runner.TestNameAware;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

@RunWith(TestNameAware.class)
public abstract class AbstractPullServiceConfigTests {
    protected static ApplicationContext staticFactory;
    protected ApplicationContext factory;
    protected PullService pullService;
    protected RequestContextChainingService requestContexts;
    protected RequestContext rc;

    @BeforeClass
    public static final void initStaticFactory() {
        staticFactory = createContext("pull/services-pull.xml");
    }

    @Before
    public final void initFactory() {
        factory = staticFactory;
    }

    @After
    public final void destroy() {
        cleanupWebEnvironment();
    }

    protected static ApplicationContext createContext(String name) {
        return createContext(name, null);
    }

    protected static ApplicationContext createContext(String name, ApplicationContext parentContext) {
        return new XmlApplicationContext(new FileSystemResource(new File(srcdir, name)), parentContext);
    }

    protected void prepareWebEnvironment(String url) throws IOException, MalformedURLException, ServletException {
        if (isEmpty(url)) {
            url = "http://www.taobao.com/app1";
        } else {
            url = "http://www.taobao.com/app1/" + url;
        }

        requestContexts = (RequestContextChainingService) factory.getBean("requestContexts");

        ServletRunner servletRunner = new ServletRunner();
        servletRunner.registerServlet("/app1/*", MyServlet.class.getName());
        ServletUnitClient client = servletRunner.newClient();
        InvocationContext ic = client.newInvocation(url);
        MyServlet servlet = (MyServlet) ic.getServlet();

        rc = requestContexts.getRequestContext(servlet.getServletContext(), ic.getRequest(), ic.getResponse());
    }

    protected void cleanupWebEnvironment() {
        if (requestContexts != null) {
            requestContexts.commitRequestContext(rc);
        }
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = -7910017457239423199L;
    }

    public static class InnerTool extends BaseFactory implements ToolFactory {
    }

    public static class InnerToolDefinitionParser extends AbstractSingleBeanDefinitionParser<InnerTool> {
    }

    public static class InnerBean extends BeanSupport {
        @Autowired
        private HttpServletRequest request;

        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public boolean isInitialized() {
            return super.isInitialized();
        }
    }
}
