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

import static com.alibaba.citrus.test.TestUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Dictionary;

import javax.servlet.http.HttpSession;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebRequest;

/**
 * 解决如下问题：
 * <ul>
 * <li>http unit servlet context不支持getResourcePaths方法的问题。</li>
 * <li>不支持httpOnly cookie的问题。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class PatchedServletRunner extends ServletRunner {
    public PatchedServletRunner() {
        super();
        patchInvocationContextFactory();
    }

    public PatchedServletRunner(File webXml, String contextPath) throws IOException, SAXException {
        try {
            Field _application = getAccessibleField(getClass(), "_application");
            _application.set(this, new PatchedWebApplication(HttpUnitUtils.newParser().parse(webXml), webXml
                    .getParentFile().getParentFile(), contextPath));

            Method completeInitialization = getAccessibleMethod(getClass(), "completeInitialization",
                    new Class<?>[] { String.class });
            completeInitialization.invoke(this, contextPath);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        patchInvocationContextFactory();
    }

    public PatchedServletRunner(File webXml) throws IOException, SAXException {
        super(webXml);
        patchInvocationContextFactory();
    }

    public PatchedServletRunner(InputStream webXML, String contextPath) throws IOException, SAXException {
        super(webXML, contextPath);
        patchInvocationContextFactory();
    }

    public PatchedServletRunner(InputStream webXML) throws IOException, SAXException {
        super(webXML);
        patchInvocationContextFactory();
    }

    public PatchedServletRunner(String webXMLFileSpec, EntityResolver resolver) throws IOException, SAXException {
        super(webXMLFileSpec, resolver);
        patchInvocationContextFactory();
    }

    public void setContextDir(File contextRoot) {
        Object _application = getFieldValue(this, "_application", null);
        Field _contextDir = getAccessibleField(_application.getClass(), "_contextDir");

        try {
            _contextDir.set(_application, contextRoot);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void patchInvocationContextFactory() {
        Field _factory = getAccessibleField(getClass(), "_factory");

        try {
            _factory.set(this, new InvocationContextFactory() {
                @SuppressWarnings("rawtypes")
                public InvocationContext newInvocation(ServletUnitClient client, FrameSelector targetFrame,
                                                       WebRequest request, Dictionary clientHeaders, byte[] messageBody)
                        throws IOException, MalformedURLException {
                    return new PatchedInvocationContextImpl(client, PatchedServletRunner.this, targetFrame, request,
                            clientHeaders, messageBody);
                }

                public HttpSession getSession(String sessionId, boolean create) {
                    ServletUnitContext _context = getFieldValue(PatchedServletRunner.this, "_context",
                            ServletUnitContext.class);

                    return _context.getValidSession(sessionId, null, create);
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
