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
package com.alibaba.citrus.logconfig;

import java.io.File;
import java.lang.reflect.Field;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;

import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.meterware.servletunit.ServletUnitServletContext;

public abstract class AbstractLogConfiguratorListenerTests extends AbstractLogConfiguratorTests {
    protected final LogConfiguratorListener listener;
    protected final ServletContext servletContext;
    protected final ServletContextEvent event;

    public AbstractLogConfiguratorListenerTests() throws Exception {
        listener = new LogConfiguratorListener();

        ServletRunner runner = new ServletRunner(new File(webxml()), "");
        ServletUnitClient client = runner.newClient();
        InvocationContext ic = client.newInvocation("http://localhost/servlet");
        servletContext = ic.getServlet().getServletConfig().getServletContext();
        event = new ServletContextEvent(servletContext);
    }

    protected String webxml() {
        return "src/test/config/WEB-INF/web.xml";
    }

    protected final void updateSystemOut() throws Exception {
        if (ServletUnitServletContext.class.equals(servletContext.getClass())) {
            Field out = ServletUnitServletContext.class.getDeclaredField("_logStream");
            out.setAccessible(true);
            out.set(servletContext, System.out);
        }
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = 7960467415594028537L;
    }
}
