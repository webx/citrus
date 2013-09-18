/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.jsp;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.jsp.impl.JspEngineImpl;
import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;
import com.alibaba.citrus.util.internal.InterfaceImplementorBuilder;
import com.alibaba.citrus.util.internal.Servlet3Util;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractJspEngineTests {
    private   ServletContext           originalServletContext;
    protected ServletContext           servletContext;
    protected HttpServletRequest       request;
    protected HttpServletResponse      response;
    protected ServletUnitClient        client;
    protected InvocationContext        ic;
    protected XmlWebApplicationContext factory;
    protected TemplateService          templateService;
    protected JspEngineImpl            engine;

    static {
        Servlet3Util.setDisableServlet3Features(true); // 禁用servlet3，因为httpunit还不支持
    }

    protected void initServlet(String webXml) throws Exception {
        ServletRunner runner = new ServletRunner(new File(srcdir, webXml), "");
        client = runner.newClient();
        ic = client.newInvocation("http://localhost:8080/app1");

        originalServletContext = ic.getServlet().getServletConfig().getServletContext();
        servletContext = createServletContextWrapper(true);
        request = ic.getRequest();
        response = ic.getResponse();
    }

    protected void initFactory() {
        initFactory("services.xml");
    }

    protected void initFactory(String configFile) {
        factory = new XmlWebApplicationContext();

        factory.setConfigLocation(configFile);
        factory.setServletContext(servletContext);
        factory.setResourceLoadingExtender(new ResourceLoadingSupport(factory));
        factory.refresh();

        templateService = (TemplateService) factory.getBean("templateService");
        engine = (JspEngineImpl) templateService.getTemplateEngine("jsp");

        assertNotNull(engine);
    }

    protected ServletContext createServletContextWrapper(final boolean supportGetResourceOfRoot) {
        return (ServletContext) new InterfaceImplementorBuilder().addInterface(ServletContext.class).toObject(new Object() {
            /** 判断当resource不存在时，返回null。 */
            public URL getResource(String path) throws MalformedURLException {
                if (("/".equals(path) || isEmpty(path)) && !supportGetResourceOfRoot) {
                    return null;
                }

                URL url = originalServletContext.getResource(path);

                if (url.getProtocol().equals("file")) {
                    try {
                        if (!new File(url.toURI()).exists()) {
                            return null;
                        }
                    } catch (URISyntaxException e) {
                        return url;
                    }
                }

                // 除去末尾的/，配合测试
                String urlstr = url.toExternalForm();

                if (urlstr.endsWith("/")) {
                    urlstr = urlstr.substring(0, urlstr.length() - 1);
                }

                return new URL(urlstr);
            }
        }, originalServletContext);
    }
}
