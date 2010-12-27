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
package com.alibaba.citrus.service.dataresolver;

import static com.alibaba.citrus.test.TestUtil.exception;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.MethodParameter;

import com.alibaba.citrus.service.dataresolver.data.Action;
import com.alibaba.citrus.service.dataresolver.data.ContextAwareResolver;
import com.alibaba.citrus.service.dataresolver.data.DerivedAction;
import com.alibaba.citrus.service.dataresolver.data.Param;

public class DataResolverServiceTests extends AbstractDataResolverTests {
    private DataResolver resolver;
    private HttpServletRequest request;
    private Param param;

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = createFactory("services.xml");
    }

    @Before
    public void init() {
        resolverServices = getResolvers("dataResolverService");
        assertNotNull(resolverServices);

        request = createMock(HttpServletRequest.class);
        replay(request);

        param = createMock(Param.class);
        expect(param.value()).andReturn("bbb").anyTimes();
        replay(param);
    }

    @Test
    public void beanNames() {
        assertSame(resolverServices, getResolvers("dataResolvers"));
    }

    @Test
    public void getResolver_noType() throws Exception {
        try {
            resolverServices.getDataResolver(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("type"));
        }
    }

    @Test
    public void getResolver_withExtraInfo() throws Exception {
        resolver = resolverServices.getDataResolver(HttpServletRequest.class, null, request);

        assertTrue(resolver.getClass().getName().contains("RequestResolver"));
        assertTrue(resolver.resolve() instanceof HttpServletRequest);
    }

    @Test
    public void getResolver_withAnnotations() throws Exception {
        resolver = resolverServices.getDataResolver(String.class, new Annotation[] { createMock(Test.class), param });

        assertTrue(resolver.getClass().getName().contains("MapResolver"));
        assertEquals("222", resolver.resolve());
    }

    @Test
    public void getResolver_notFound() throws Exception {
        try {
            resolverServices.getDataResolver(String.class, null);
            fail();
        } catch (DataResolverNotFoundException e) {
            assertThat(e, exception("Could not find data resolver for DataResolverContext {", "String", "[]", "}"));
        }
    }

    @Test
    public void getParameterResolvers() throws Exception {
        assertParameterResolvers(Action.class);
        assertParameterResolvers(DerivedAction.class);
    }

    private void assertParameterResolvers(Class<?> actionClass) throws Exception {
        Method method = actionClass.getMethod("execute", HttpServletRequest.class, String.class);
        DataResolver[] resolvers = resolverServices.getParameterResolvers(method, request);

        assertEquals(2, resolvers.length);

        // RequestResolver
        ContextAwareResolver resolver0 = (ContextAwareResolver) resolvers[0];
        ContextAwareResolver resolver1 = (ContextAwareResolver) resolvers[1];

        assertTrue(resolver0.resolve() instanceof HttpServletRequest);
        assertEquals("111", resolver1.resolve());

        // °üº¬extraObject: MethodParameter
        MethodParameter param0 = resolver0.getContext().getExtraObject(MethodParameter.class);
        assertEquals(HttpServletRequest.class, param0.getParameterType());
        assertEquals(0, param0.getParameterIndex());

        String str = "";
        str += "MethodParameter {\n";
        str += "  method     = public void " + actionClass.getName()
                + ".execute(javax.servlet.http.HttpServletRequest,java.lang.String)\n";
        str += "  paramIndex = 0\n";
        str += "}";

        assertEquals(str, param0.toString());

        MethodParameter param1 = resolver1.getContext().getExtraObject(MethodParameter.class);
        assertEquals(String.class, param1.getParameterType());
        assertEquals(1, param1.getParameterIndex());

        str = "";
        str += "MethodParameter {\n";
        str += "  method     = public void " + actionClass.getName()
                + ".execute(javax.servlet.http.HttpServletRequest,java.lang.String)\n";
        str += "  paramIndex = 1\n";
        str += "}";

        assertEquals(str, param1.toString());

    }

    @Test
    public void toString_() {
        String str = resolverServices.toString();

        assertThat(str, containsString("DataResolverService ["));
        assertThat(str, containsString("MapResolverFactory"));
        assertThat(str, containsString("RequestResolverFactory"));
        assertThat(str, containsString("]"));
    }
}
