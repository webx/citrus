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
package com.alibaba.citrus.turbine.dataresolver.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import net.sf.cglib.reflect.FastConstructor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.dataresolver.DataResolverException;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.turbine.dataresolver.Params;

@RunWith(Parameterized.class)
public class DataResolverUtilTests {
    private final Class<Annotation> annotationType;
    private final int index;
    private final boolean hasOptionalArgs;
    private final Object result;
    private DataResolverContext context;

    @SuppressWarnings("unchecked")
    public DataResolverUtilTests(Class<? extends Annotation> annotationType, int index, boolean hasOptionalArgs,
                                 Object result) {
        this.annotationType = (Class<Annotation>) annotationType;
        this.index = index;
        this.hasOptionalArgs = hasOptionalArgs;
        this.result = result;
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = createArrayList();

        // no name() method
        add(data, Params.class, 0, false, new IllegalArgumentException("could not get value: @Params.name()"));

        // no name
        add(data, Param.class, 0, false, new IllegalArgumentException("missing @Param's name: DataResolverContext"));
        add(data, FormGroup.class, 0, false, new IllegalArgumentException(
                "missing @FormGroup's name: DataResolverContext"));

        // noOptionalArgs
        add(data, Param.class, 1, false, "myname");
        add(data, Param.class, 2, false, "myname");
        add(data, Param.class, 3, false, "myname");

        add(data, FormGroup.class, 1, false, "mygroupname");
        add(data, FormGroup.class, 2, false, "mygroupname");
        add(data, FormGroup.class, 3, false, "mygroupname");

        // hasOptionalArgs
        add(data, Param.class, 1, true, new IllegalArgumentException(
                "use @Param(name=\"myname\") instead of @Param(value=\"myname\"): DataResolverContext"));

        add(data, Param.class, 2, true, new IllegalArgumentException(
                "use @Param(name=\"myname\") instead of @Param(value=\"myname\"): DataResolverContext"));

        add(data, Param.class, 3, true, "myname");

        add(data, FormGroup.class, 1, true, new IllegalArgumentException(
                "use @FormGroup(name=\"mygroupname\") instead of @FormGroup(value=\"mygroupname\"): "
                        + "DataResolverContext"));

        add(data, FormGroup.class, 2, true, new IllegalArgumentException(
                "use @FormGroup(name=\"mygroupname\") instead of @FormGroup(value=\"mygroupname\"): "
                        + "DataResolverContext"));

        add(data, FormGroup.class, 3, true, "mygroupname");

        return data;
    }

    private static void add(List<Object[]> data, Object... values) {
        data.add(values);
    }

    @Before
    public void init() throws Exception {
        context = new DataResolverContext(DataResolverUtilTests.class, null, null);
    }

    @Test
    public void getAnnotationNameOrValue() {
        Annotation a = getAnnotation(annotationType, index);

        if (result instanceof Exception) {
            try {
                DataResolverUtil.getAnnotationNameOrValue(annotationType, a, context, hasOptionalArgs);
                fail();
            } catch (Exception e) {
                assertThat(e, exception(((Exception) result).getClass(), ((Exception) result).getMessage()));
            }
        } else {
            assertEquals(result, DataResolverUtil.getAnnotationNameOrValue(annotationType, a, context, hasOptionalArgs));
        }
    }

    private <A extends Annotation> A getAnnotation(Class<A> annotationType, int index) {
        String methodName = annotationType.getSimpleName().toLowerCase();
        Method method = null;

        for (Method m : DataResolverUtilTests.class.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
            }
        }

        return annotationType.cast(method.getParameterAnnotations()[index][0]);
    }

    @SuppressWarnings("unused")
    private static void param(@Param String s1, @Param("myname") String s2, @Param(value = "myname") String s3,
                              @Param(name = "myname") String s4) {
    }

    @SuppressWarnings("unused")
    private static void params(@Params String s1) {
    }

    @SuppressWarnings("unused")
    private static void formgroup(@FormGroup String s1, @FormGroup("mygroupname") String s2,
                                  @FormGroup(value = "mygroupname") String s3,
                                  @FormGroup(name = "mygroupname") String s4) {
    }

    @Test
    public void getFastConstructor_and_newInstance() throws Exception {
        FastConstructor fc = DataResolverUtil.getFastConstructor(MyData.class);
        assertTrue(DataResolverUtil.newInstance(fc) instanceof MyData);
    }

    @Test
    public void getFastConstructor_abstract() throws Exception {
        try {
            DataResolverUtil.getFastConstructor(MyDataAbstract.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Class to set properties should be public and concrete: "));
        }
    }

    @Test
    public void getFastConstructor_private() throws Exception {
        try {
            DataResolverUtil.getFastConstructor(MyDataPrivate.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Class to set properties should be public and concrete: "));
        }
    }

    @Test
    public void getFastConstructor_noDefaultConstructor() throws Exception {
        try {
            DataResolverUtil.getFastConstructor(MyDataNoDefaultConstructor.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Class to set properties has no default constructor:"));
        }
    }

    @Test
    public void newInstance_null() throws Exception {
        try {
            DataResolverUtil.newInstance(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("fastConstructor==null"));
        }
    }

    @Test
    public void newInstance_failed() throws Exception {
        try {
            DataResolverUtil.newInstance(DataResolverUtil.getFastConstructor(MyDataFailedConstructor.class));
            fail();
        } catch (DataResolverException e) {
            assertThat(
                    e,
                    exception(IOException.class,
                            "Failed to create instance of class " + MyDataFailedConstructor.class.getName()));
        }
    }

    public static class MyData {
    }

    public static class MyDataFailedConstructor {
        public MyDataFailedConstructor() throws Exception {
            throw new IOException();
        }
    }

    public static abstract class MyDataAbstract {
    }

    public static class MyDataNoDefaultConstructor {
        MyDataNoDefaultConstructor(String name) {
        }
    }

    private static class MyDataPrivate {
    }
}
