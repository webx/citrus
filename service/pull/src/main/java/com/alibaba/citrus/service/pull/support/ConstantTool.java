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
package com.alibaba.citrus.service.pull.support;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 在模板中使用constant的tool。
 * <p>
 * 例如：<code>$myconstant.MY_CONSTANT</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class ConstantTool implements ToolFactory {
    private static final Logger log = LoggerFactory.getLogger(ConstantTool.class);
    private Class<?> constantClass;
    protected Map<String, Object> constants = createHashMap();

    public Class<?> getConstantClass() {
        return constantClass;
    }

    public void setConstantClass(Class<?> constantClass) {
        this.constantClass = assertNotNull(constantClass, "constantClass");

        log.trace("Introspecting constants in class {}", constantClass.getName());

        Field[] fields = constantClass.getFields();
        Map<String, Field> fieldsMap = createHashMap();

        for (Field field : fields) {
            int modifier = field.getModifiers();

            // 取得public static final的常量。
            if (Modifier.isPublic(modifier) && Modifier.isFinal(modifier) && Modifier.isStatic(modifier)) {
                // 处理名称冲突，确保子类覆盖父类的常量。
                if (fieldsMap.containsKey(field.getName())) {
                    Field existField = fieldsMap.get(field.getName());

                    if (existField.getDeclaringClass().isAssignableFrom(field.getDeclaringClass())) {
                        fieldsMap.put(field.getName(), field);
                    }
                } else {
                    fieldsMap.put(field.getName(), field);
                }
            }
        }

        for (Field field : fieldsMap.values()) {
            try {
                Object value = field.get(null);

                constants.put(field.getName(), field.get(null));

                if (log.isTraceEnabled()) {
                    log.trace("Found constant: {}.{} = {}",
                            new Object[] { constantClass.getSimpleName(), field.getName(), ObjectUtil.toString(value) });
                }
            } catch (Exception e) {
                unexpectedException(e, "failed to get constant: %s", field.getName());
            }
        }
    }

    public boolean isSingleton() {
        return true;
    }

    public Object createTool() {
        return this;
    }

    public Object get(String name) {
        return constants.get(name);
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("Constant[")
                .append(constantClass == null ? "" : constantClass.getSimpleName()).append("]").append(constants)
                .toString();
    }
}
