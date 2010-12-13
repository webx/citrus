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
package com.alibaba.citrus.service.jsp.impl;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.jsp.JspEngine;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * Jsp模板引擎的实现。
 * 
 * @author Michael Zhou
 */
public class JspEngineImpl extends AbstractService<JspEngine> implements JspEngine, ResourceLoaderAware,
        InitializingBean {
    private final ServletContext servletContext;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private ResourceLoader resourceLoader;
    private String contextRoot;
    private String path;

    /**
     * 创建jsp引擎。
     * <p>
     * 需要注意的是，用来创建jsp引擎的参数必须是“全局”作用域的，而不是“request”作用域的。这一点可由
     * <code>RequestContextChainingService</code>来保证。
     * </p>
     */
    public JspEngineImpl(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
        this.servletContext = assertNotNull(servletContext, "servletContext");
        this.request = assertProxy(assertNotNull(request, "request"));
        this.response = assertProxy(assertNotNull(response, "response"));
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setPath(String path) {
        this.path = trimToNull(path);
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(resourceLoader, "resourceLoader");

        // 取得搜索路径（相对）。
        if (path == null) {
            path = "/templates";
        }

        // 规格化路径，以"/"结尾。
        path = normalizeAbsolutePath(path + "/");

        // 取得webroot根目录的URL
        URL url = servletContext.getResource("/");

        if (url != null) {
            contextRoot = url.toExternalForm();
        } else {
            // 如果取不到webroot根目录，则试着取得web.xml的URL，以此为基准，计算相对于webroot的URL。
            url = servletContext.getResource("/WEB-INF/web.xml");

            if (url != null) {
                String urlstr = url.toExternalForm();

                if (urlstr.endsWith("/WEB-INF/web.xml")) {
                    contextRoot = urlstr.substring(0, urlstr.length() - "WEB-INF/web.xml".length());
                }
            }
        }

        if (contextRoot == null) {
            throw new IllegalArgumentException("Could not find WEBROOT.  Are you sure you are in webapp?");
        }

        if (!contextRoot.endsWith("/")) {
            contextRoot += "/";
        }

        if (getLogger().isDebugEnabled()) {
            MapBuilder mb = new MapBuilder();

            mb.append("path", path);
            mb.append("contextRoot", contextRoot);

            getLogger().debug(new ToStringBuilder().append("Initialized JSP Template Engine").append(mb).toString());
        }
    }

    /**
     * 取得默认的模板名后缀列表。
     * <p>
     * 当<code>TemplateService</code>没有指定到当前engine的mapping时，将取得本方法所返回的后缀名列表。
     * </p>
     */
    public String[] getDefaultExtensions() {
        return new String[] { "jsp" };
    }

    /**
     * 判定模板是否存在。
     */
    public boolean exists(String templateName) {
        return getPathWithinServletContextInternal(templateName) != null;
    }

    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     * 
     * @param template 模板名
     * @param context template context
     * @return 模板渲然的结果字符串
     * @throws TemplateException 渲染失败
     */
    public String getText(String template, TemplateContext context) throws TemplateException, IOException {
        // 取得JSP相对于webapp的路径。
        String relativeTemplateName = getPathWithinServletContext(template);

        // 取得JSP的RequestDispatcher。
        RequestDispatcher dispatcher = servletContext.getRequestDispatcher(relativeTemplateName);

        if (dispatcher == null) {
            throw new TemplateNotFoundException("Could not dispatch to JSP template " + template);
        }

        try {
            // 将template context适配到request
            HttpServletRequest requestWrapper = new TemplateContextAdapter(request, context);

            // 避免在jsp中修改content type、locale和charset，这应该在模板外部来控制
            HttpServletResponse responseWrapper = new JspResponse(response);

            dispatcher.include(requestWrapper, responseWrapper);
        } catch (ServletException e) {
            throw new TemplateException(e);
        }

        return "";
    }

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, OutputStream ostream) throws TemplateException,
            IOException {
        getText(templateName, context);
    }

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, Writer writer) throws TemplateException,
            IOException {
        getText(templateName, context);
    }

    /**
     * 取得相对于servletContext的模板路径。这个路径可被
     * <code>javax.servlet.RequestDispatcher</code> 使用，以便找到jsp的实例。
     */
    public String getPathWithinServletContext(String templateName) throws TemplateNotFoundException {
        String path = getPathWithinServletContextInternal(templateName);

        if (path == null) {
            throw new TemplateNotFoundException("Template " + templateName + " not found");
        }

        return path;
    }

    private String getPathWithinServletContextInternal(String templateName) {
        assertInitialized();

        String resourceName = path + (templateName.startsWith("/") ? templateName.substring(1) : templateName);
        Resource resource = resourceLoader.getResource(resourceName);
        String path = null;

        if (resource != null && resource.exists()) {
            try {
                String url = resource.getURL().toExternalForm();

                if (url.startsWith(contextRoot)) {
                    path = url.substring(contextRoot.length() - 1); // 保留slash:/
                }
            } catch (IOException e) {
                // ignore
            }
        }

        return path;
    }

    @Override
    public String toString() {
        return "JspEngine[" + path + "]";
    }
}
