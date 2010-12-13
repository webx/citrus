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
package com.alibaba.citrus.service.requestcontext;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

import com.alibaba.citrus.service.requestcontext.basic.BasicRequestContext;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.requestcontext.lazycommit.LazyCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.rewrite.RewriteRequestContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;

/**
 * 测试<code>RequestContextChainingService</code>。
 * 
 * @author Michael Zhou
 */
public class RequestContextChainingServiceTests extends AbstractRequestContextsTests<RequestContext> {
    private static ThreadLocal<RequestContextChainingServiceTests> testCaseHolder = new ThreadLocal<RequestContextChainingServiceTests>();
    private List<Integer> prepareOrder;
    private List<Integer> commitOrder;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");

        prepareOrder = createArrayList();
        commitOrder = createArrayList();
        testCaseHolder.set(this);
    }

    @After
    public void destroy() {
        testCaseHolder.set(null);
    }

    @Test
    public void defaultNameAndAlias() {
        RequestContextChainingService requestContexts = (RequestContextChainingService) factory
                .getBean("requestContexts");

        assertNotNull(requestContexts);
        assertSame(requestContexts, factory.getBean("requestContextChainingService"));
    }

    @Test
    public void emptyRequestContext() throws Exception {
        initRequestContext("empty");

        // 在没有任何request context factory的时候，
        //   requestContext.getRequest()就是原来的request
        //   requestContext.getResponse()就是原来的response
        assertSame(request, newRequest);
        assertSame(response, newResponse);

        // 确保可以从request中取得requestContext
        assertSame(requestContext, RequestContextUtil.getRequestContext(request));
    }

    @Test
    public void _toString() throws Exception {
        initRequestContext("order");

        String expectedResult = "";

        expectedResult += "MyContext[4] {\n";
        expectedResult += "  MyContext[3] {\n";
        expectedResult += "    MyContext[2] {\n";
        expectedResult += "      MyContext[1] {\n";
        expectedResult += "        MyContext[0] {\n";
        expectedResult += "          SimpleRequestContext {\n";
        expectedResult += "            request  = " + request + "\n";
        expectedResult += "            response = " + response + "\n";
        expectedResult += "            webapp   = " + config.getServletContext() + "\n";
        expectedResult += "          }\n";
        expectedResult += "        }\n";
        expectedResult += "      }\n";
        expectedResult += "    }\n";
        expectedResult += "  }\n";
        expectedResult += "}";

        assertEquals(expectedResult, requestContext.toString());
    }

    @Test
    public void prepareCommitOrder() throws Exception {
        initRequestContext("order");

        requestContexts.commitRequestContext(requestContext);
        assertArrayEquals(new Object[] { 0, 1, 2, 3, 4 }, prepareOrder.toArray());
        assertArrayEquals(new Object[] { 4, 3, 2, 1, 0 }, commitOrder.toArray());
    }

    @Test
    public void sort() throws Exception {
        initRequestContext("sort");

        requestContexts.commitRequestContext(requestContext);
        assertArrayEquals(new Object[] { -3, -2, 4, 5, 2, 3, 1, 0, -1, -4 }, prepareOrder.toArray());
        assertArrayEquals(new Object[] { -4, -1, 0, 1, 3, 2, 5, 4, -2, -3 }, commitOrder.toArray());
    }

    @Test
    public void sort1() throws Exception {
        initRequestContext("sort1");

        requestContexts.commitRequestContext(requestContext);

        // 顺序虽然与sort()不同，但仍然符合所有条件。
        assertArrayEquals(new Object[] { -3, -2, 5, 4, 3, 2, 1, 0, -1, -4 }, prepareOrder.toArray());
        assertArrayEquals(new Object[] { -4, -1, 0, 1, 2, 3, 4, 5, -2, -3 }, commitOrder.toArray());
    }

    @Test
    public void sort2() throws Exception {
        initRequestContext("all");

        RequestContextInfo<?>[] infos = requestContexts.getRequestContextInfos();

        assertEquals(8, infos.length);
        int i = 0;

        assertEquals(BasicRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(ParserRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(BufferedRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(LazyCommitRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(SessionRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(SetLocaleRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(RewriteRequestContext.class, infos[i++].getRequestContextInterface());
        assertEquals(RunData.class, infos[i++].getRequestContextInterface());
    }

    @Test
    public void missingRequired() throws Exception {
        try {
            initRequestContext("missingRequired");
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(IllegalArgumentException.class, "Missing feature", "f2", "required by", "[1]"));
        }
    }

    @Test
    public void destructionCallbacks() throws Exception {
        initRequestContext("all");

        final String[] data = new String[1];

        RequestContextUtil.registerRequestDestructionCallback("testCallback", new Runnable() {
            public void run() {
                data[0] = "destructed!";
            }
        });

        requestContexts.commitRequestContext(requestContext);

        assertArrayEquals(new String[] { "destructed!" }, data);
    }

    public static class MyRequestContextFactory extends AbstractRequestContextFactory<MyRequestContext> {
        private int index;

        public void setIndex(int index) {
            this.index = index;
        }

        public MyRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
            return new MyRequestContextImpl(wrappedContext, index);
        }

        public String[] getFeatures() {
            return new String[] { "f" + index };
        }

        public FeatureOrder[] featureOrders() {
            switch (index) {
                case -3:
                    return new FeatureOrder[] { new BeforeFeature("f-2") };

                case -2:
                    return new FeatureOrder[] { new BeforeFeature("*") }; // *表示在所有feature之前

                case -1:
                    return new FeatureOrder[] { new AfterFeature("*") }; // *表示在所有feature之后

                case -4:
                    return new FeatureOrder[] { new AfterFeature("f-1") };

                case 0:
                    return new FeatureOrder[] { new AfterFeature("f1") };

                case 1:
                    return new FeatureOrder[] { new RequiresFeature("f2"), new AfterFeature("f3") };

                case 2:
                    return new FeatureOrder[] { new AfterFeature("f4") };

                case 3:
                    return new FeatureOrder[] { new AfterFeature("f4") };

                case 5:
                    return new FeatureOrder[] { new BeforeFeature("f2") };

                case 4:

                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            return "MyRequestContextFactory[" + index + "]";
        }
    }

    public static class MyRequestContextImpl extends AbstractRequestContextWrapper implements MyRequestContext {
        private final int index;

        public MyRequestContextImpl(RequestContext wrappedContext, int index) {
            super(wrappedContext);
            this.index = index;
        }

        @Override
        public void prepare() {
            testCaseHolder.get().prepareOrder.add(index);
        }

        @Override
        public void commit() throws RequestContextException {
            testCaseHolder.get().commitOrder.add(index);
        }

        @Override
        protected String thisToString() {
            return "MyContext[" + index + "]";
        }
    }

    public static interface MyRequestContext extends RequestContext {
    }
}
