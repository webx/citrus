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

package com.alibaba.citrus.test.util;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.util.internal.Servlet3Util;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.PatchedServletRunner;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.w3c.dom.Element;

/**
 * 创建用于测试的servlet容器，支持request contexts机制。
 *
 * @author Michael Zhou
 */
public class ServletTestContainer {
    protected final ServletRunner     servletRunner;
    protected final ServletUnitClient client;

    private InvocationContext   invocationContext;
    private HttpServletRequest  rawRequest;
    private HttpServletResponse rawResponse;
    private ServletContext      servletContext;

    private RequestContextChainingService requestContexts;
    private RequestContext                requestContext;

    private WebResponse clientResponse;

    static {
        Servlet3Util.setDisableServlet3Features(true); // 禁用servlet3，因为httpunit还不支持
    }

    public ServletTestContainer() {
        servletRunner = new PatchedServletRunner();

        servletRunner.registerServlet("/noop/*", NoopServlet.class.getName());
        init(servletRunner);

        client = servletRunner.newClient();
    }

    protected void init(ServletRunner runner) {
    }

    public void setMappings(Map<String, String> mappings) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String pattern = entry.getKey();
            String servletClass = defaultIfEmpty(entry.getValue(), NoopServlet.class.getName());

            servletRunner.registerServlet(pattern, servletClass);
        }
    }

    public InvocationContext getInvocationContext() {
        return invocationContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public HttpServletRequest getRawRequest() {
        return rawRequest;
    }

    public HttpServletResponse getRawResponse() {
        return rawResponse;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public HttpServletRequest getRequest() {
        return requestContext == null ? rawRequest : requestContext.getRequest();
    }

    public HttpServletResponse getResponse() {
        return requestContext == null ? rawResponse : requestContext.getResponse();
    }

    public WebResponse getClientResponse() {
        return clientResponse;
    }

    @Autowired(required = false)
    public void setRequestContexts(RequestContextChainingService requestContexts) {
        this.requestContexts = requestContexts;
    }

    public void request(String uri) {
        if (uri != null && uri.startsWith("http")) {
            uri = URI.create(uri).normalize().toString(); // full uri
        } else {
            uri = URI.create("http://www.test.com/" + trimToEmpty(uri)).normalize().toString(); // partial uri
        }

        request(new GetMethodWebRequest(uri));
    }

    public void request(WebRequest webRequest) {
        try {
            invocationContext = client.newInvocation(webRequest);
            rawRequest = new MyHttpRequest(invocationContext.getRequest(), webRequest.getURL().toExternalForm());
            rawResponse = new MyHttpResponse(invocationContext.getResponse());
            servletContext = invocationContext.getServlet().getServletConfig().getServletContext();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (requestContexts != null) {
            requestContext = requestContexts.getRequestContext(servletContext, rawRequest, rawResponse);
        }
    }

    /** 将服务端response提交到client。 */
    public void commit() {
        try {
            if (requestContexts != null && requestContext != null) {
                requestContexts.commitRequestContext(requestContext);
            }

            clientResponse = client.getResponse(invocationContext);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanup() {
        RequestContextHolder.resetRequestAttributes();

        invocationContext = null;
        rawRequest = null;
        rawResponse = null;
        servletContext = null;
        requestContext = null;
    }

    /** 不做任何事的servlet。 */
    public static class NoopServlet extends HttpServlet {
        private static final long serialVersionUID = 3034658026956449398L;

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                                IOException {
        }
    }

    /**
     * 由于httpunit目前未实现setCharacterEncoding方法，getQueryString()也实现得有问题，
     * 所以只能将request包装一下。
     */
    public static class MyHttpRequest extends HttpServletRequestWrapper {
        private String charset;
        private String overrideQueryString;
        private String server = "www.test.com";
        private int    port   = 80;
        private boolean sessionCreated;

        public MyHttpRequest(HttpServletRequest request, String uri) {
            super(request);

            if (uri != null) {
                int index = uri.indexOf("?");

                if (index >= 0) {
                    this.overrideQueryString = uri.substring(index + 1);
                }
            }
        }

        @Override
        public String getQueryString() {
            if (overrideQueryString == null) {
                return super.getQueryString();
            } else {
                return overrideQueryString;
            }
        }

        @Override
        public String getCharacterEncoding() {
            return charset;
        }

        @Override
        public void setCharacterEncoding(String charset) throws UnsupportedEncodingException {
            this.charset = charset;
        }

        /** 默认实现总是返回localhost，只好覆盖此方法。 */
        @Override
        public String getServerName() {
            return server;
        }

        public void setServerName(String server) {
            this.server = server;
        }

        /** 默认实现总是返回0，只好覆盖此方法。 */
        @Override
        public int getServerPort() {
            return port;
        }

        public void setServerPort(int port) {
            this.port = port;
        }

        /** 监视getSession方法的调用。 */
        public boolean isSessionCreated() {
            return sessionCreated;
        }

        @Override
        public HttpSession getSession() {
            sessionCreated = true;
            return super.getSession();
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (create) {
                sessionCreated = true;
            }

            return super.getSession(create);
        }
    }

    /** 由于httpunit目前未实现commit以后抛IllegalStateException，所以只能将response包装一下。 */
    public static class MyHttpResponse extends HttpServletResponseWrapper {
        private boolean committed;

        public MyHttpResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public boolean isCommitted() {
            return super.isCommitted() || committed;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            ensureNotCommited();
            super.sendError(sc, msg);
            committed = true;
        }

        @Override
        public void sendError(int sc) throws IOException {
            ensureNotCommited();
            super.sendError(sc);
            committed = true;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            ensureNotCommited();
            super.sendRedirect(location);
            committed = true;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            ensureNotCommited();
            return super.getOutputStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            ensureNotCommited();
            return super.getWriter();
        }

        @Override
        public void reset() {
            ensureNotCommited();
            super.reset();
        }

        @Override
        public void resetBuffer() {
            ensureNotCommited();
            super.resetBuffer();
        }

        @Override
        public void setLocale(Locale locale) {
            // 防止unsupported operation exception
        }

        @Override
        public void setContentType(String type) {
            if (type == null || type.indexOf("charset=") == -1) {
                setCharacterEncoding(null);
            }

            super.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            ensureNotCommited();
            super.setBufferSize(size);
        }

        private void ensureNotCommited() {
            if (isCommitted()) {
                throw new IllegalStateException();
            }
        }
    }

    public static class DefinitionParser extends AbstractNamedBeanDefinitionParser<ServletTestContainer> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Map<Object, Object> mappings = createManagedMap(element, parserContext);

            for (Element mappingElement : subElements(element, and(sameNs(element), name("mapping")))) {
                String pattern = assertNotNull(trimToNull(mappingElement.getAttribute("pattern")), "pattern");
                String servletClass = trimToNull(mappingElement.getAttribute("servletClass"));

                mappings.put(pattern, servletClass);
            }

            if (!mappings.isEmpty()) {
                builder.addPropertyValue("mappings", mappings);
            }
        }

        @Override
        protected String getDefaultName() {
            return "servletTestContainer";
        }
    }
}
