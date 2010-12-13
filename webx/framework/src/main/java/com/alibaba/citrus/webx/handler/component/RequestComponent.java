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
package com.alibaba.citrus.webx.handler.component;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

public class RequestComponent extends PageComponent {
    private final KeyValuesComponent keyValuesComponent;

    public RequestComponent(PageComponentRegistry registry, String componentPath, KeyValuesComponent keyValuesComponent) {
        super(registry, componentPath);
        this.keyValuesComponent = keyValuesComponent;
    }

    public void visitTemplate(RequestHandlerContext context) {
        getTemplate().accept(new RequestVisitor(context));
    }

    @SuppressWarnings("unused")
    private class RequestVisitor extends AbstractVisitor {
        private final HttpServletRequest request;
        private final ServletContext servletContext;

        public RequestVisitor(RequestHandlerContext context) {
            super(context, RequestComponent.this);
            this.request = context.getRequest();
            this.servletContext = context.getServletContext();
        }

        public void visitBasicInfo() {
            Map<String, Object> keyValues = createLinkedHashMap();

            keyValues.put("Method", request.getMethod());
            keyValues.put("Protocol", request.getProtocol());
            keyValues.put("Request URL", request.getRequestURL().toString());
            keyValues.put("Query String", request.getQueryString());
            keyValues.put("Scheme", request.getScheme());
            keyValues.put("Server Name", request.getServerName());
            keyValues.put("Server Port", String.valueOf(request.getServerPort()));
            keyValues.put("Context Path", request.getContextPath());
            keyValues.put("Servlet Path", request.getServletPath());
            keyValues.put("Path Info", request.getPathInfo());
            keyValues.put("Path Translated", request.getPathTranslated());

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitConnectionInfo() {
            Map<String, Object> keyValues = createLinkedHashMap();

            keyValues.put("Local Name", request.getLocalName());
            keyValues.put("Local Address", request.getLocalAddr());
            keyValues.put("Local Port", String.valueOf(request.getLocalPort()));
            keyValues.put("Remote Host", request.getRemoteHost());
            keyValues.put("Remote Address", request.getRemoteAddr());
            keyValues.put("Remote Port", String.valueOf(request.getRemotePort()));

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitAuthInfo() {
            Map<String, Object> keyValues = createLinkedHashMap();

            keyValues.put("Remote User", request.getRemoteUser());
            keyValues.put("Auth Type", request.getAuthType());
            keyValues.put("User Principal", String.valueOf(request.getUserPrincipal()));

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitMisc() {
            Map<String, Object> keyValues = createLinkedHashMap();

            keyValues.put("Character Encoding", request.getCharacterEncoding());
            keyValues.put("Content Length", String.valueOf(request.getContentLength()));
            keyValues.put("Content Type", request.getContentType());
            keyValues.put("Preferred Locale", String.valueOf(request.getLocale()));
            keyValues.put("Supported Locales", enumToList(request.getLocales()));

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitParameters() {
            Map<String, Object> keyValues = createTreeMap();

            for (String name : enumToList(request.getParameterNames())) {
                keyValues.put(name, createArrayList(request.getParameterValues(name)));
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitHeaders() {
            Map<String, Object> keyValues = createTreeMap();

            for (String name : enumToList(request.getHeaderNames())) {
                keyValues.put(name, enumToList(request.getHeaders(name)));
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitCookies() {
            Map<String, Object> keyValues = createTreeMap();
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    keyValues.put(cookie.getName(), cookie.getValue());
                }
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitAttributes() {
            Map<String, Object> keyValues = createTreeMap();

            for (String name : enumToList(request.getAttributeNames())) {
                keyValues.put(name, request.getAttribute(name));
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        public void visitContextAttributes() {
            Map<String, Object> keyValues = createTreeMap();

            for (String name : enumToList(servletContext.getAttributeNames())) {
                keyValues.put(name, String.valueOf(servletContext.getAttribute(name)));
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        private List<String> enumToList(Enumeration<?> i) {
            List<String> list = createArrayList();

            if (i != null) {
                while (i.hasMoreElements()) {
                    list.add(String.valueOf(i.nextElement()));
                }
            }

            return list;
        }
    }
}
