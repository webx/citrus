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
package com.alibaba.citrus.service.velocity.impl.parser;

import static com.alibaba.citrus.service.velocity.support.InterpolationUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;

public class ASTStringLiteralEnhanced extends ASTStringLiteral {
    private static final Field[] fields;
    private boolean interpolate;

    static {
        List<Field> fieldList = createLinkedList();

        for (Class<?> c = ASTStringLiteral.class; c != null && c != Object.class; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                fieldList.add(field);
            }
        }

        fields = fieldList.toArray(new Field[fieldList.size()]);
    }

    public ASTStringLiteralEnhanced(ASTStringLiteral src) {
        super(-1);

        for (Field field : fields) {
            try {
                Object value = field.get(src);

                if ("interpolate".equals(field.getName()) && value instanceof Boolean) {
                    interpolate = (Boolean) value;
                }

                field.set(this, value);
            } catch (Exception e) {
                throw new RuntimeException("Could not copy ASTStringLiteral", e);
            }
        }
    }

    @Override
    public Object value(InternalContextAdapter context) {
        if (interpolate) {
            Object savedInterpolate = context.localPut(INTERPOLATE_KEY, Boolean.TRUE);

            try {
                return super.value(context);
            } finally {
                if (savedInterpolate == null) {
                    context.remove(INTERPOLATE_KEY);
                } else {
                    context.localPut(INTERPOLATE_KEY, savedInterpolate);
                }
            }
        }

        return super.value(context);
    }
}
