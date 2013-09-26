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

package com.alibaba.citrus.service.requestcontext.impl;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextException;
import com.alibaba.citrus.service.requestcontext.RequestContextFactory;
import com.alibaba.citrus.service.requestcontext.TwoPhaseCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.util.internal.InterfaceImplementorBuilder;
import com.alibaba.citrus.util.internal.Servlet3Util.Servlet3OutputStream;
import org.junit.Before;
import org.junit.Test;

public class RequestContextCommitTests {
    private RequestContextChainingServiceImpl service;
    private ServletContext                    servletContext;
    private HttpServletRequest                request;
    private HttpServletResponse               response;
    private RequestContext                    requestContext1;
    private TwoPhaseCommitRequestContext      requestContext2;
    private Object                            overrider1;
    private Object                            overrider2;
    private RequestContext                    requestContext;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() throws Exception {
        // servletContext/request/response
        servletContext = createNiceMock(ServletContext.class);
        request = createNiceMock(HttpServletRequest.class);
        response = createNiceMock(HttpServletResponse.class);

        expect(response.getWriter()).andReturn(new PrintWriter(System.out)).anyTimes();
        expect(response.getOutputStream()).andReturn(new Servlet3OutputStream(null) {
            @Override
            public void write(int b) throws IOException {
            }
        }).anyTimes();

        replay(servletContext, request, response);

        // requestContexts
        requestContext1 = createMock(RequestContext.class);
        requestContext2 = createMock(TwoPhaseCommitRequestContext.class);

        List<RequestContextFactory<?>> factories = createArrayList(new RequestContextFactory<?>[] {
                new MyRequestContextFactory(requestContext1, overrider1),
                new MyRequestContextFactory(requestContext2, overrider2),
        });

        service = new RequestContextChainingServiceImpl();
        service.setFactories(factories);
        service.afterPropertiesSet();
    }

    @Test
    public void commit() {
        // 接下来的提交，会触发RequestContext.commit，
        // 以及TwoPhaseCommitRequestContext.commitHeaders()。
        requestContext1.commit();
        expectLastCall().once();

        requestContext2.commitHeaders();
        expectLastCall().once();

        requestContext2.commit();
        expectLastCall().once();

        requestContext = service.getRequestContext(servletContext, request, response);

        service.commitRequestContext(requestContext);
        service.commitRequestContext(requestContext); // 不会重复提交

        verify(requestContext1, requestContext2);
    }

    @Test
    public void commitHeaders() {
        // 接下来的提交，不会触发RequestContext.commit，
        // 但会触发TwoPhaseCommitRequestContext.commitHeaders()。
        requestContext2.commitHeaders();
        expectLastCall().once();

        requestContext = service.getRequestContext(servletContext, request, response);

        service.commitHeaders(requestContext);
        service.commitHeaders(requestContext); // 不会重复提交

        verify(requestContext1, requestContext2);
    }

    @Test
    public void commitHeaders_thenCommitAll() {
        // 接下来的提交，会触发RequestContext.commit，
        // 以及TwoPhaseCommitRequestContext.commitHeaders()。
        requestContext1.commit();
        expectLastCall().once();

        requestContext2.commitHeaders();
        expectLastCall().once();

        requestContext2.commit();
        expectLastCall().once();

        requestContext = service.getRequestContext(servletContext, request, response);

        service.commitHeaders(requestContext);
        service.commitRequestContext(requestContext);

        verify(requestContext1, requestContext2);
    }

    @Test
    public void autoCommitHeaders_writer() throws Exception {
        // 接下来对writer或stream的操作，将会自动触发TwoPhaseCommitRequestContext.commitHeaders()。
        requestContext2.commitHeaders();
        expectLastCall().once();

        requestContext = service.getRequestContext(servletContext, request, response);
        PrintWriter out = requestContext.getResponse().getWriter();

        out.println("hi");
        out.flush();

        verify(requestContext1, requestContext2);
    }

    @Test
    public void autoCommitHeaders_stream() throws Exception {
        // 接下来对writer或stream的操作，将会自动触发TwoPhaseCommitRequestContext.commitHeaders()。
        requestContext2.commitHeaders();
        expectLastCall().once();

        requestContext = service.getRequestContext(servletContext, request, response);
        ServletOutputStream out = requestContext.getResponse().getOutputStream();

        out.println("hi");
        out.flush();

        verify(requestContext1, requestContext2);
    }

    private int count;

    /** 测试当在commit时间接触发了commitHeaders时，commitHeaders不会被执行。 */
    @Test
    public void preventCommitHeaders_whenDoCommitting() throws Exception {
        // 用来测试执行顺序。
        count = 0;

        overrider1 = new Object() {
            public void commit() {
                try {
                    // 在commit时，触发commitHeaders
                    requestContext1.getResponse().getWriter().flush();
                } catch (IOException e) {
                    throw new RequestContextException(e);
                }

                assertEquals(0, count++);
            }
        };
        overrider2 = new Object() {
            private RequestContext thisObject;

            public void setThisProxy(Object o) {
                thisObject = (RequestContext) o;
            }

            public void commitHeaders() {
                assertEquals(1, count++);

                CommitMonitor monitor = findRequestContext(thisObject, SimpleRequestContext.class);
                assertFalse(getFieldValue(monitor, "headersCommitted", Boolean.class));
                assertTrue(getFieldValue(monitor, "committed", Boolean.class));

                assertTrue(monitor.isCommitted());
                assertTrue(monitor.isHeadersCommitted());
            }

            public void commit() {
                assertEquals(2, count++);
            }
        };

        List<RequestContextFactory<?>> factories = createArrayList(new RequestContextFactory<?>[] {
                new MyRequestContextFactory(requestContext2, overrider2),
                new MyRequestContextFactory(requestContext1, overrider1), // 先提交requestContext1，再提交requestContext2
        });

        service = new RequestContextChainingServiceImpl();
        service.setFactories(factories);
        service.afterPropertiesSet();

        requestContext = service.getRequestContext(servletContext, request, response);
        service.commitRequestContext(requestContext);

        verify(requestContext1, requestContext2);
    }

    private CountDownLatch latch;

    /** 测试多线程不会同时执行commit和commitHeaders。 */
    @Test
    public void synchronization() throws Exception {
        // 用来测试执行顺序。
        count = 0;

        latch = new CountDownLatch(1);

        overrider1 = new Object() {
            public void commit() throws Exception {
                latch.await();
                assertEquals(2, count++);
            }
        };
        overrider2 = new Object() {
            public void commitHeaders() {
                assertEquals(3, count++);
            }

            public void commit() {
                assertEquals(4, count++);
            }
        };

        List<RequestContextFactory<?>> factories = createArrayList(new RequestContextFactory<?>[] {
                new MyRequestContextFactory(requestContext2, overrider2),
                new MyRequestContextFactory(requestContext1, overrider1), // 先提交requestContext1，再提交requestContext2
        });

        service = new RequestContextChainingServiceImpl();
        service.setFactories(factories);
        service.afterPropertiesSet();

        requestContext = service.getRequestContext(servletContext, request, response);

        Thread t1 = new Thread(new Runnable() {
            public void run() {
                assertEquals(0, count++);
                service.commitRequestContext(requestContext);
                assertEquals(0, count++);
            }
        });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                assertEquals(1, count++);
                service.commitHeaders(requestContext);
                assertEquals(5, count++);
            }
        });

        t1.start();

        // 确保thread1进入等待latch状态
        TimeUnit.MILLISECONDS.sleep(10);
        assertEquals(1, count);

        t2.start();

        // 确保thread2进入等待monitor的状态
        TimeUnit.MILLISECONDS.sleep(10);
        assertEquals(2, count);

        latch.countDown();

        t1.join();
        t2.join();
    }

    private class MyRequestContextFactory extends AbstractRequestContextFactory<RequestContext> {
        private final RequestContext requestContext;
        private final Object         overrider;

        private MyRequestContextFactory(RequestContext requestContext, Object overrider) {
            this.requestContext = requestContext;
            this.overrider = overrider;
        }

        @Override
        public RequestContext getRequestContextWrapper(RequestContext wrappedContext) {
            expect(requestContext.getServletContext()).andReturn(wrappedContext.getServletContext()).anyTimes();
            expect(requestContext.getRequest()).andReturn(wrappedContext.getRequest()).anyTimes();
            expect(requestContext.getResponse()).andReturn(wrappedContext.getResponse()).anyTimes();
            expect(requestContext.getWrappedRequestContext()).andReturn(wrappedContext).anyTimes();
            requestContext.prepare();
            expectLastCall().once();
            replay(requestContext);

            if (overrider != null) {
                return (RequestContext) new InterfaceImplementorBuilder()
                        .addInterface(requestContext.getClass().getInterfaces())
                        .toObject(overrider, requestContext);
            } else {
                return requestContext;
            }
        }

        @Override
        public String[] getFeatures() {
            return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public FeatureOrder[] featureOrders() {
            return new FeatureOrder[0];  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
