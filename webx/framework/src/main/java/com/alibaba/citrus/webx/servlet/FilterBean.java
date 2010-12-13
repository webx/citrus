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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.servlet.HttpServletBean;

/**
 * 支持注入参数的filter基类。
 * <p>
 * 将init-params注入到filter中，将request和response转换成 <code>HttpServletRequest</code>和
 * <code>HttpServletResponse</code>。
 * </p>
 * 
 * @author Michael Zhou
 * @see HttpServletBean
 */
public abstract class FilterBean implements Filter {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Set<String> requiredProperties = createHashSet();
    private FilterConfig filterConfig;

    /**
     * 添加一个必选配置项。
     */
    protected final void addRequiredProperty(String name) {
        this.requiredProperties.add(name);
    }

    /**
     * 初始化filter。
     */
    public final void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        logInBothServletAndLoggingSystem("Initializing filter: " + getFilterName());

        try {
            PropertyValues pvs = new FilterConfigPropertyValues(getFilterConfig(), requiredProperties);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
            bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
            initBeanWrapper(bw);
            bw.setPropertyValues(pvs, true);
        } catch (Exception e) {
            throw new ServletException("Failed to set bean properties on filter: " + getFilterName(), e);
        }

        try {
            init();
        } catch (Exception e) {
            throw new ServletException("Failed to init filter: " + getFilterName(), e);
        }

        logInBothServletAndLoggingSystem(getClass().getSimpleName() + " - " + getFilterName()
                + ": initialization completed");
    }

    protected final void logInBothServletAndLoggingSystem(String msg) {
        getServletContext().log(msg);
        log.info(msg);
    }

    /**
     * 初始化<code>BeanWrapper</code>。
     */
    protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
    }

    /**
     * 初始化Filter。
     */
    protected void init() throws Exception {
    }

    /**
     * 清理filter。
     */
    public void destroy() {
    }

    /**
     * 取得filter的配置。
     */
    public final FilterConfig getFilterConfig() {
        return filterConfig;
    }

    /**
     * 取得定义在web.xml中的filter名字。
     */
    public final String getFilterName() {
        return filterConfig == null ? null : filterConfig.getFilterName();
    }

    /**
     * 取得当前webapp的context。
     */
    public final ServletContext getServletContext() {
        return filterConfig == null ? null : filterConfig.getServletContext();
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String method = httpRequest.getMethod();

            if ("HEAD".equalsIgnoreCase(method)) {
                httpResponse = new NoBodyResponse(httpResponse);
            }

            try {
                doFilter(httpRequest, httpResponse, chain);
            } finally {
                if (httpResponse instanceof NoBodyResponse) {
                    ((NoBodyResponse) httpResponse).setContentLength();
                }
            }
        } else {
            log.debug("Skipped filtering due to the unknown request/response types: {}, {}", request.getClass()
                    .getName(), response.getClass().getName());

            chain.doFilter(request, response);
        }
    }

    protected abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    /**
     * 从filter config中取得所有的init-params。
     */
    private static class FilterConfigPropertyValues extends MutablePropertyValues {
        private static final long serialVersionUID = -5359131251714023794L;

        public FilterConfigPropertyValues(FilterConfig config, Set<String> requiredProperties) throws ServletException {
            Set<String> missingProps = createTreeSet(requiredProperties);

            for (Enumeration<?> e = config.getInitParameterNames(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String value = config.getInitParameter(key);

                addPropertyValue(new PropertyValue(key, value));
                missingProps.remove(key);
            }

            assertTrue(missingProps.isEmpty(), "Initialization for filter %s failed.  "
                    + "The following required properties were missing: %s", config.getFilterName(), missingProps);
        }
    }

    /**
     * 不返回response body的response实现。
     */
    private static class NoBodyResponse extends HttpServletResponseWrapper {
        private NoBodyOutputStream noBody = new NoBodyOutputStream();
        private PrintWriter writer;
        private boolean didSetContentLength;

        public NoBodyResponse(HttpServletResponse response) {
            super(response);
        }

        public void setContentLength() {
            if (!didSetContentLength) {
                super.setContentLength(noBody.getContentLength());
            }
        }

        @Override
        public void setContentLength(int len) {
            super.setContentLength(len);
            didSetContentLength = true;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return noBody;
        }

        @Override
        public PrintWriter getWriter() throws UnsupportedEncodingException {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(noBody, getCharacterEncoding()));
            }

            return writer;
        }
    }

    /**
     * 不返回response body的servlet output stream实现。
     */
    private static class NoBodyOutputStream extends ServletOutputStream {
        private int contentLength;

        public NoBodyOutputStream() {
            contentLength = 0;
        }

        public int getContentLength() {
            return contentLength;
        }

        @Override
        public void write(int b) {
            contentLength++;
        }

        @Override
        public void write(byte[] buf, int offset, int len) throws IOException {
            if (len >= 0) {
                contentLength += len;
            } else {
                throw new IOException("negative length");
            }
        }
    }
}
