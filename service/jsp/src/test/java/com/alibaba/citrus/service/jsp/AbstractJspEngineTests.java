package com.alibaba.citrus.service.jsp;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.jsp.JspEngineTests.ServletContextWrapper;
import com.alibaba.citrus.service.jsp.impl.JspEngineImpl;
import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractJspEngineTests {
    protected ServletContext servletContext;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletUnitClient client;
    protected InvocationContext ic;
    protected XmlWebApplicationContext factory;
    protected TemplateService templateService;
    protected JspEngineImpl engine;

    protected void initServlet(String webXml) throws Exception {
        ServletRunner runner = new ServletRunner(new File(srcdir, webXml), "");
        client = runner.newClient();
        ic = client.newInvocation("http://localhost:8080/app1");

        servletContext = new ServletContextWrapper(ic.getServlet().getServletConfig().getServletContext());
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
}
