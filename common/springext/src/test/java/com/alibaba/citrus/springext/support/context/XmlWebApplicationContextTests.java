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
import static com.alibaba.citrus.util.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;

import com.meterware.servletunit.ServletRunner;

public class XmlWebApplicationContextTests extends AbstractBeanFactoryTests {
    private static XmlWebApplicationContext factory;

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = new XmlWebApplicationContext();
        factory.setConfigLocation("beans.xml");
        factory.setServletContext(new ServletRunner(new File(srcdir, "WEB-INF/web.xml"), "").newClient()
                .newInvocation("http://localhost/servlet").getServlet().getServletConfig().getServletContext());
        factory.refresh();
    }

    @Override
    protected BeanFactory getFactory() {
        return factory;
    }

    @Test
    public void autwireAnnotations() {
        MyClass myClass = (MyClass) getFactory().getBean("myClass");

        assertNotNull(myClass.getContainer());
    }

    public static class NoopServlet extends HttpServlet {
        private static final long serialVersionUID = 3034658026956449398L;

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            doGet(request, response);
        }
    }

}
