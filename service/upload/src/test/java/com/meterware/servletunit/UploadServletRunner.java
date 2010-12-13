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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Dictionary;

import javax.servlet.http.HttpSession;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.WebRequest;

/**
 * 对<code>ServletRunner</code>的hack，支持对request content的过滤。
 * 
 * @author Michael Zhou
 */
public class UploadServletRunner extends ServletRunner {

    public UploadServletRunner() {
        super();
    }

    public UploadServletRunner(File webXml, String contextPath) throws IOException, SAXException {
        super(webXml, contextPath);
    }

    public UploadServletRunner(File webXml) throws IOException, SAXException {
        super(webXml);
    }

    public UploadServletRunner(InputStream webXML, String contextPath) throws IOException, SAXException {
        super(webXML, contextPath);
    }

    public UploadServletRunner(InputStream webXML) throws IOException, SAXException {
        super(webXML);
    }

    public UploadServletRunner(String webXMLFileSpec, EntityResolver resolver) throws IOException, SAXException {
        super(webXMLFileSpec, resolver);
    }

    protected byte[] filter(WebRequest request, byte[] messageBody) {
        return messageBody;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ServletUnitClient newClient() {
        return ServletUnitClient.newClient(new InvocationContextFactory() {
            public InvocationContext newInvocation(ServletUnitClient client, FrameSelector targetFrame,
                                                   WebRequest request, Dictionary clientHeaders, byte[] messageBody)
                    throws IOException, MalformedURLException {
                return new InvocationContextImpl(client, UploadServletRunner.this, targetFrame, request, clientHeaders,
                        filter(request, messageBody));
            }

            public HttpSession getSession(String sessionId, boolean create) {
                return getContext().getValidSession(sessionId, null, create);
            }
        });
    }
}
