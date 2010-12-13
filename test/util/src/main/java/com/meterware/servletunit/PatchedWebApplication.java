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
package com.meterware.servletunit;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PatchedWebApplication extends WebApplication {
    private ServletContext servletContext;

    public PatchedWebApplication() {
        super();
    }

    public PatchedWebApplication(Document document, File file, String contextPath) throws MalformedURLException,
            SAXException {
        super(document, file, contextPath);
    }

    public PatchedWebApplication(Document document, String contextPath) throws MalformedURLException, SAXException {
        super(document, contextPath);
    }

    public PatchedWebApplication(Document document) throws MalformedURLException, SAXException {
        super(document);
    }

    @Override
    public ServletContext getServletContext() {
        if (servletContext == null) {
            servletContext = new PatchedServletContext(this);
        }

        return servletContext;
    }
}
