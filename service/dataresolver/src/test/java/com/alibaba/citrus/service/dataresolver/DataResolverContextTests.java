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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.dataresolver.data.Param;

public class DataResolverContextTests {
    private Annotation a1;
    private Annotation a2;
    private Annotation a3;

    private Object o1;
    private Object o2;
    private Object o3;

    private DataResolverContext context;

    @Before
    public void init() {
        a1 = createMock(Before.class);
        a2 = createMock(After.class);
        a3 = createMock(Test.class);

        o1 = "test"; // string
        o2 = 10; // integer
        o3 = false; // boolean

        context = new DataResolverContext(HttpServletRequest.class, new Annotation[] { a1, a2, a3 }, new Object[] { o1,
                o2, o3 });
    }

    @Test
    public void new_noType() {
        try {
            new DataResolverContext(null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("type"));
        }
    }

    @Test
    public void new_noAnnotations_noExtraInfo() {
        context = new DataResolverContext(HttpServletRequest.class, null, null);

        assertArrayEquals(new Annotation[0], context.getAnnotations());
        assertArrayEquals(new Object[0], context.getExtraInfo());

        assertNull(context.getAnnotation(Param.class));
        assertNull(context.getExtraObject(String.class));
    }

    @Test
    public void getTypeInfo() {
        assertEquals(HttpServletRequest.class, context.getTypeInfo().getRawType());
    }

    @Test
    public void getAnnotations() {
        assertArrayEquals(new Annotation[] { a1, a2, a3 }, context.getAnnotations());
    }

    @Test
    public void getExtraInfo() {
        assertArrayEquals(new Object[] { o1, o2, o3 }, context.getExtraInfo());
    }

    @Test
    public void getAnnotationByType() {
        assertSame(a1, context.getAnnotation(Before.class));
        assertSame(a3, context.getAnnotation(Test.class));

        assertNull(context.getAnnotation(Param.class));
    }

    @Test
    public void getExtraObjectByType() {
        assertSame(o1, context.getExtraObject(String.class));
        assertSame(o3, context.getExtraObject(Boolean.class));

        assertNull(context.getExtraObject(Class.class));
    }

    @Test
    public void toString_() {
        String str = context.toString();

        assertThat(str, containsString("DataResolverContext {"));
        assertThat(str, containsString("HttpServletRequest"));
        assertThat(str, containsAll("Before", "After", "Test"));
        assertThat(str, containsAll("test", "10", "false"));
        assertThat(str, containsString("}"));
    }
}
