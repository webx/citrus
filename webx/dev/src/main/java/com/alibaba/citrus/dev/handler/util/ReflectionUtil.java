package com.alibaba.citrus.dev.handler.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Field getAccessibleField(Class<?> targetType, String fieldName) throws Exception {
        Field field = null;

        for (Class<?> c = targetType; c != null && field == null; c = c.getSuperclass()) {
            try {
                field = c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }

        field.setAccessible(true);

        return field;
    }

    public static Method getAccessibleMethod(Class<?> targetType, String methodName, Class<?>[] argTypes)
            throws Exception {
        Method method = null;

        for (Class<?> c = targetType; c != null && method == null; c = c.getSuperclass()) {
            try {
                method = c.getDeclaredMethod(methodName, argTypes);
            } catch (NoSuchMethodException e) {
            }
        }

        method.setAccessible(true);

        return method;
    }
}
