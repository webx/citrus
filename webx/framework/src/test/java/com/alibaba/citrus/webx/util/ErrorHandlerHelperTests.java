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

package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.webx.util.ErrorHandlerHelper.LoggingDetail.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper.ExceptionCodeMapping;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class ErrorHandlerHelperTests {
    private HttpServletRequest request;
    private ErrorHandlerHelper helper;
    private Logger             log;

    @Before
    public void init() throws Exception {
        ServletRunner servletRunner = new ServletRunner();
        servletRunner.registerServlet("/app1/*", HttpServlet.class.getName());
        ServletUnitClient client = servletRunner.newClient();

        request = client.newInvocation("http://localhost/app1").getRequest();
        helper = ErrorHandlerHelper.getInstance(request);
        log = createMock(Logger.class);
    }

    @Test
    public void getInstance() throws Exception {
        ServletRunner servletRunner = new ServletRunner();
        servletRunner.registerServlet("/app1/*", HttpServlet.class.getName());
        ServletUnitClient client = servletRunner.newClient();

        request = client.newInvocation("http://localhost/app1").getRequest();

        assertNull(request.getAttribute("_webx_errorHandlerHelper_"));
        helper = ErrorHandlerHelper.getInstance(request);
        assertSame(helper, ErrorHandlerHelper.getInstance(request));
        assertSame(helper, request.getAttribute("_webx_errorHandlerHelper_"));
    }

    @Test
    public void messages() {
        System.out.println(new ToStringBuilder().append(ErrorHandlerHelper.STATUS_CODE_MESSAGES));

        assertEquals("OK", ErrorHandlerHelper.STATUS_CODE_MESSAGES.get(200));
        assertEquals("BAD_REQUEST", ErrorHandlerHelper.STATUS_CODE_MESSAGES.get(400));
        assertEquals("NOT_FOUND", ErrorHandlerHelper.STATUS_CODE_MESSAGES.get(404));
        assertEquals("INTERNAL_SERVER_ERROR", ErrorHandlerHelper.STATUS_CODE_MESSAGES.get(500));
    }

    @Test
    public void setException() {
        Throwable exception = new IOException();

        helper.setException(exception);
        helper.setServletErrorAttributes();

        assertSame(exception, helper.getException());
        assertSame(exception, request.getAttribute("javax.servlet.error.exception"));

        assertEquals(IOException.class, helper.getExceptionType());
        assertEquals(IOException.class, request.getAttribute("javax.servlet.error.exception_type"));
    }

    @Test
    public void setStatusCode() {
        helper.setStatusCode(500);
        helper.setServletErrorAttributes();

        assertEquals(500, helper.getStatusCode());
        assertEquals(500, request.getAttribute("javax.servlet.error.status_code"));

        assertEquals("INTERNAL_SERVER_ERROR", helper.getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", request.getAttribute("javax.servlet.error.message"));

        helper.setStatusCode(404);
        helper.setServletErrorAttributes();

        assertEquals(404, helper.getStatusCode());
        assertEquals(404, request.getAttribute("javax.servlet.error.status_code"));

        assertEquals("NOT_FOUND", helper.getMessage());
        assertEquals("NOT_FOUND", request.getAttribute("javax.servlet.error.message"));

        helper.setStatusCode(500, "MY_ERROR");
        helper.setServletErrorAttributes();

        assertEquals(500, helper.getStatusCode());
        assertEquals(500, request.getAttribute("javax.servlet.error.status_code"));

        assertEquals("MY_ERROR", helper.getMessage());
        assertEquals("MY_ERROR", request.getAttribute("javax.servlet.error.message"));

        helper.setStatusCode(404, "MY_ERROR");
        helper.setServletErrorAttributes();

        assertEquals(404, helper.getStatusCode());
        assertEquals(404, request.getAttribute("javax.servlet.error.status_code"));

        assertEquals("MY_ERROR", helper.getMessage());
        assertEquals("MY_ERROR", request.getAttribute("javax.servlet.error.message"));
    }

    @Test
    public void setRequestURI() {
        helper.setRequestURI("/aa/bb");
        helper.setServletErrorAttributes();

        assertEquals("/aa/bb", helper.getRequestURI());
        assertEquals("/aa/bb", request.getAttribute("javax.servlet.error.request_uri"));
    }

    @Test
    public void setServletName() {
        helper.setServletName("myName");
        helper.setServletErrorAttributes();

        assertEquals("myName", helper.getServletName());
        assertEquals("myName", request.getAttribute("javax.servlet.error.servlet_name"));
    }

    @Test
    public void setError() {
        ExceptionCodeMapping mapping = new ExceptionCodeMapping() {
            public int getExceptionCode(Throwable exception) {
                if (exception instanceof FileNotFoundException) {
                    return 404;
                } else if (exception instanceof IOException) {
                    return 500;
                }

                return 0;
            }
        };

        IOException e = new IOException();
        helper.init("myServlet", e, mapping);
        helper.setServletErrorAttributes();

        assertEquals(e, helper.getException());
        assertEquals(IOException.class, helper.getExceptionType());
        assertEquals(500, helper.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", helper.getMessage());
        assertEquals("/app1", helper.getRequestURI());
        assertEquals("myServlet", helper.getServletName());

        e = new FileNotFoundException();
        helper.init("myServlet", e, mapping);
        helper.setServletErrorAttributes();

        assertEquals(e, request.getAttribute("javax.servlet.error.exception"));
        assertEquals(FileNotFoundException.class, request.getAttribute("javax.servlet.error.exception_type"));
        assertEquals(404, request.getAttribute("javax.servlet.error.status_code"));
        assertEquals("NOT_FOUND", request.getAttribute("javax.servlet.error.message"));
        assertEquals("/app1", request.getAttribute("javax.servlet.error.request_uri"));
        assertEquals("myServlet", request.getAttribute("javax.servlet.error.servlet_name"));
    }

    @Test
    public void logError() {
        // no exception
        reset(log);
        replay(log);
        helper.logError(log);
        verify(log);

        // with exception
        Exception e = new IOException("ioe");
        helper.init("myServlet", e, null);

        reset(log);
        log.error("Failed to process request /app1, the root cause was IOException: ioe", e);
        replay(log);
        helper.logError(log);
        verify(log);
    }

    @Test
    public void logError2() {
        // no exception
        reset(log);
        replay(log);
        helper.logError(log, null);
        verify(log);

        // with exception, default logging detail
        Exception e = new IOException("ioe");
        e.initCause(new IllegalArgumentException("iae"));
        helper.init("myServlet", e, null);

        reset(log);
        log.error("Failed to process request /app1, the root cause was IllegalArgumentException: iae", e);
        replay(log);
        helper.logError(log, null);
        verify(log);

        // detailed
        reset(log);
        log.error("Failed to process request /app1, the root cause was IllegalArgumentException: iae", e);
        replay(log);
        helper.logError(log, detailed);
        verify(log);

        // brief
        reset(log);
        log.error("IllegalArgumentException: iae");
        replay(log);
        helper.logError(log, brief);
        verify(log);

        // brief - exception without message
        e = new IOException("ioe");
        e.initCause(new IllegalArgumentException());
        helper.init("myServlet", e, null);

        reset(log);
        log.error("IllegalArgumentException");
        replay(log);
        helper.logError(log, brief);
        verify(log);

        // disabled logging
        reset(log);
        replay(log);
        helper.logError(log, disabled);
        verify(log);
    }

    @Test
    public void toString_() {
        assertEquals("500 INTERNAL_SERVER_ERROR", helper.toString());

        helper.setStatusCode(404);
        assertEquals("404 NOT_FOUND", helper.toString());
    }
}
