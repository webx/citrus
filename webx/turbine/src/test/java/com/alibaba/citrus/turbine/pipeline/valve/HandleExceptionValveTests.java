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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.requestcontext.lazycommit.LazyCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;

public class HandleExceptionValveTests extends AbstractValveTests {
    @Before
    public void initPipeline() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("handleException");
    }

    @Test
    public void create() {
        try {
            new HandleExceptionValve(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no request"));
        }
    }

    @Test
    public void createExceptionHandler() {
        HandleExceptionValve.ExceptionHandler handler;

        try {
            new HandleExceptionValve.ExceptionHandler(null, -1, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no exception type"));
        }

        handler = new HandleExceptionValve.ExceptionHandler(Exception.class, -10, "  ");

        assertEquals(Exception.class, handler.getExceptionType());
        assertEquals(-1, handler.getStatusCode());
        assertNull(handler.getPage());
    }

    @Test
    public void create_noDefaultPage() {
        try {
            factory.getBean("handleException_noDefaultPage");
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(IllegalArgumentException.class, "no defaultPage"));
        }
    }

    @Test
    public void matchExceptions() throws Exception {
        // 精确匹配
        assertException(IOException.class, "error_io_exception.vm");
        assertException(Exception.class, "error_exception.vm");
        assertException(IllegalArgumentException.class, "error_illegal_argument_exception.vm");
        assertException(RuntimeException.class, "error_runtime_exception.vm");

        // FileNotFoundException未指定，但是从IOException派生
        assertException(FileNotFoundException.class, "error_io_exception.vm");

        // ArithmeticException未指定，但是从RuntimeException派生
        assertException(ArithmeticException.class, "error_runtime_exception.vm");

        // NumberFormatException未指定，但是从IllegalArgumentException派生
        assertException(NumberFormatException.class, "error_illegal_argument_exception.vm");

        // ClassNotFoundException未指定，但是从Exception派生
        assertException(ClassNotFoundException.class, "error_exception.vm");

        // Error未匹配，取默认值
        assertException(Error.class, "error.vm", false);
    }

    private void assertException(Class<? extends Throwable> type, String target) throws Exception {
        assertException(type, target, true);
    }

    private void assertException(Class<? extends Throwable> type, String target, boolean indirect) throws Exception {
        // 直接异常
        ErrorHandlerHelper.getInstance(newRequest).init("app1", type.newInstance(), null);
        pipeline.newInvocation().invoke();

        assertEquals(target, rundata.getTarget());

        // 间接异常
        if (indirect) {
            Throwable t = new Exception(type.newInstance());

            ErrorHandlerHelper.getInstance(newRequest).init("app1", t, null);
            pipeline.newInvocation().invoke();

            assertEquals(target, rundata.getTarget());
        }
    }

    @Test
    public void helper() {
        Exception e = new Exception();

        ErrorHandlerHelper.getInstance(newRequest).init("app1", e, null);
        pipeline.newInvocation().invoke();

        ErrorHandlerHelper helper = (ErrorHandlerHelper) rundata.getContext().get("error");

        assertEquals(e, helper.getException());
    }

    @Test
    public void helper_specifiedName() {
        pipeline = (PipelineImpl) factory.getBean("handleException_helperName");

        Exception e = new Exception();

        ErrorHandlerHelper.getInstance(newRequest).init("app1", e, null);
        pipeline.newInvocation().invoke();

        ErrorHandlerHelper helper = (ErrorHandlerHelper) rundata.getContext().get("error1");

        assertEquals(e, helper.getException());
    }

    @Test
    public void statusCode() {
        ErrorHandlerHelper.getInstance(newRequest).init("app1", new Exception(), null);
        pipeline.newInvocation().invoke();

        assertEquals(501, RequestContextUtil.findRequestContext(newRequest, LazyCommitRequestContext.class).getStatus());

        ErrorHandlerHelper helper = (ErrorHandlerHelper) rundata.getContext().get("error");

        assertEquals(501, helper.getStatusCode());
        assertEquals("NOT_IMPLEMENTED", helper.getMessage());
    }

    @Test
    public void statusCode_default() {
        ErrorHandlerHelper.getInstance(newRequest).init("app1", new IllegalArgumentException(), null);
        pipeline.newInvocation().invoke();

        ErrorHandlerHelper helper = (ErrorHandlerHelper) rundata.getContext().get("error");

        assertEquals(500, helper.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", helper.getMessage());
    }
}
