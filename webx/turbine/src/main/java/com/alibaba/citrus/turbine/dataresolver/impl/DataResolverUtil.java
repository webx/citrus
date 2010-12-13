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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.dataresolver.DataResolverException;

public class DataResolverUtil {
    static <A extends Annotation> String getAnnotationNameOrValue(Class<A> annotationType, A annotation,
                                                                  DataResolverContext context, boolean hasOptionalArgs) {
        String name = trimToNull(get(annotationType, annotation, "name"));

        if (name == null) {
            name = trimToNull(get(annotationType, annotation, "value"));

            String typeName = annotationType.getSimpleName();
            assertNotNull(name, "missing @%s's name: %s", typeName, context);

            // 约定：假如有其它可选参数存在，则必须使用name()这种形式。
            // 例如：@Param("name")
            // 和：  @Param(name="name", defaultValue="123")
            assertTrue(!hasOptionalArgs, "use @%s(name=\"%s\") instead of @%s(value=\"%s\"): %s", typeName, name,
                    typeName, name, context);
        }

        return name;
    }

    private static <A extends Annotation> String get(Class<A> annotationType, A annotation, String name) {
        try {
            return (String) annotation.getClass().getMethod(name).invoke(annotation);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("could not get value: @%s.%s()",
                    annotationType.getSimpleName(), name), e);
        }
    }

    static FastConstructor getFastConstructor(Class<?> beanType) {
        int mod = beanType.getModifiers();

        assertTrue(!Modifier.isAbstract(mod) && Modifier.isPublic(mod),
                "Class to set properties should be public and concrete: %s", beanType.getName());

        Constructor<?> constructor;

        try {
            constructor = beanType.getConstructor();
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Class to set properties has no default constructor: %s",
                    beanType.getName()));
        }

        return FastClass.create(beanType).getConstructor(constructor);
    }

    static Object newInstance(FastConstructor fc) {
        try {
            return assertNotNull(fc, "fastConstructor==null").newInstance();
        } catch (InvocationTargetException e) {
            throw new DataResolverException("Failed to create instance of class " + fc.getDeclaringClass().getName(),
                    e.getCause());
        }
    }
}
