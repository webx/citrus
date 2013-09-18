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

package com.alibaba.citrus.async;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.async.pipeline.valve.PerformRunnableAsyncValve;
import com.alibaba.citrus.async.support.FakeAsyncExecutor;
import com.alibaba.citrus.async.support.GetScreenResult;
import com.alibaba.citrus.async.support.SetScreenResult;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.util.internal.InterfaceImplementorBuilder;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@RunWith(Parameterized.class)
public class PerformRunnableAsyncValveTests extends AbstractAsyncTests {
    private FakeAsyncExecutor             executor1;
    private PerformRunnableAsyncValve     valve;
    private PipelineImpl                  pipeline;
    private RequestContextChainingService rccs;

    private HttpServletRequest requestMock;

    private HttpServletRequest  request;
    private HttpServletResponse response;
    private ServletContext      servletContext;
    private RequestContext      requestContext;
    private AsyncContext        asyncContext;

    private Map<String, Object> requestAttrs = createHashMap();

    private boolean doExecuteTask;

    public PerformRunnableAsyncValveTests(boolean doExecuteTask) {
        this.doExecuteTask = doExecuteTask;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { true }, { false } });
    }

    @BeforeClass
    public static void initClass() {
        defaultFactory = createApplicationContext("performRunnableAsyncValve.xml");
    }

    @Before
    public void init() {
        executor1 = (FakeAsyncExecutor) factory.getBean("executor1");
        pipeline = (PipelineImpl) factory.getBean("pipeline1");
        valve = getValve("pipeline1", 1, PerformRunnableAsyncValve.class);
        rccs = (RequestContextChainingService) factory.getBean("requestContexts");

        assertNotNull(executor1);
        assertNotNull(pipeline);
        assertNotNull(valve);
        assertNotNull(rccs);

        requestMock = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);
        servletContext = createMock(ServletContext.class);
        asyncContext = createMock(AsyncContext.class);

        expect(requestMock.getDispatcherType()).andReturn(DispatcherType.ASYNC).anyTimes();
        expect(requestMock.getLocale()).andReturn(Locale.CHINA).anyTimes();
        expect(requestMock.getSession(false)).andReturn(null).anyTimes();

        // 以下是为setLoggingContext准备的参数
        expect(requestMock.getMethod()).andReturn("GET").once();
        expect(requestMock.getRequestURI()).andReturn("http://localhost:8080/test").once();
        expect(requestMock.getRequestURL()).andReturn(new StringBuffer("http://localhost:8080/test")).once();
        expect(requestMock.getQueryString()).andReturn("x=1").once();
        expect(requestMock.getRemoteHost()).andReturn("localhost").once();
        expect(requestMock.getRemoteAddr()).andReturn("127.0.0.1").once();
        expect(requestMock.getHeader("User-Agent")).andReturn("Safari").once();
        expect(requestMock.getHeader("Referer")).andReturn("http://localhost:8080/").once();
        expect(requestMock.getCookies()).andReturn(new Cookie[0]).once();

        replay(requestMock, response, servletContext);

        request = (HttpServletRequest) new InterfaceImplementorBuilder().addInterface(HttpServletRequest.class).toObject(new Object() {
            public Object getAttribute(String name) {
                return requestAttrs.get(name);
            }

            public void setAttribute(String name, Object o) {
                requestAttrs.put(name, o);
            }

            public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
                    throws IllegalStateException {
                return asyncContext;
            }
        }, requestMock);

        requestContext = rccs.getRequestContext(servletContext, request, response);

        rccs.bind(request);
    }

    @After
    public void destroy() {
        rccs.unbind(request);
        requestAttrs.clear();
    }

    @Test
    public void invoke_resultIsNotRunnable() {
        SetScreenResult.set("notARunnable");
        pipeline.newInvocation().invoke();
    }

    private boolean runnableCalled;

    @Test
    public void invoke_resultIsRunnable() throws Exception {
        invokeWithResult(new Runnable() {
            public void run() {
                runnableCalled = true;
            }
        }, null, 0);
    }

    @Test
    public void invoke_resultIsCallable() throws Exception {
        invokeWithResult(new Callable<Object>() {
            public Object call() {
                runnableCalled = true;
                return "myResultObject";
            }
        }, "myResultObject", 0);
    }

    @Test
    public void invoke_resultIsCallable_withTimeout() throws Exception {
        class MyCallable implements Callable<Object>, AsyncCallback {
            public long getTimeout() {
                return 1000L;
            }

            public long getCancelingTimeout() {
                return 500L;
            }

            public Object call() {
                runnableCalled = true;
                return "myResultObject";
            }
        }

        invokeWithResult(new MyCallable(), "myResultObject", 1000L);
    }

    @Test
    public void invoke_resultIsCallable_withDefaultTimeout() throws Exception {
        class MyCallable implements Callable<Object>, AsyncCallback {
            public long getTimeout() {
                return -1L; // 使用默认值
            }

            public long getCancelingTimeout() {
                return -1L; // 使用默认值
            }

            public Object call() {
                runnableCalled = true;
                return "myResultObject";
            }
        }

        invokeWithResult(new MyCallable(), "myResultObject", 0L);
    }

    private void invokeWithResult(Object result, Object newResult, long timeout) throws Exception {
        SetScreenResult.set(result);

        asyncContext.setTimeout(timeout);
        expectLastCall().once();

        Capture<AsyncListener> listenerCap = new Capture<AsyncListener>();
        asyncContext.addListener(capture(listenerCap));
        expectLastCall().once();

        asyncContext.complete(); // 当runnable被执行完时，asyncContext.complete会被调用。
        expectLastCall().andThrow(new IllegalStateException()).once(); // 即使complete抛出异常也没有关系。

        replay(asyncContext);

        pipeline.newInvocation().invoke();

        AsyncListener listener = listenerCap.getValue(); // addListener被调用

        Callable<?> callable = executor1.getCallable(); // executor.submit(callable)被调用
        assertNotNull(callable);

        // doExecuteTask控制是否执行runnable
        if (doExecuteTask) {
            // 在另一个线程中执行runnable，确保request被绑定到线程中（否则RequestProxyTester会报错）
            runnableCalled = false;
            assertNull(new SimpleAsyncTaskExecutor().submit(callable).get()); // callable总是返回null
            assertTrue(runnableCalled); // runnable或callable被执行
            assertEquals(newResult, GetScreenResult.get()); // doPerformRunnable以后，值被保存到result中

            verify(requestMock, asyncContext);
        }

        // onTimeout
        AsyncEvent event = createMock(AsyncEvent.class);
        reset(asyncContext);

        // 如果runnable已经被执行，那么event.getAsyncContext().complete()将不被执行。
        if (!doExecuteTask) {
            expect(event.getAsyncContext()).andReturn(asyncContext).once();

            asyncContext.complete();
            expectLastCall().andThrow(new IllegalStateException()).once(); // 即使complete抛出异常也没有关系。
        }

        replay(event, asyncContext);

        listener.onTimeout(event); // 当timeout时，asyncContext.complete被调用

        verify(event, asyncContext);
    }
}
