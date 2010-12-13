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
package com.alibaba.citrus.generictype.introspect;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.util.internal.StringUtil;

/**
 * 用来读取或设置property的工具。
 * 
 * @author Michael Zhou
 */
public class PropertyUtil {

    /**
     * 取得指定名称的property，支持嵌套的property。
     */
    public static Object getProperty(Object object, String propertyPath, TypeConverter converter) {
        assertNotNull(object, "object");

        PropertyEvaluater eval = new PropertyEvaluater(propertyPath, converter, object);

        PropertyPath.parse(propertyPath, eval);

        return eval.getValue();
    }

    /**
     * 对property取值的对象。
     */
    private static class PropertyEvaluater implements PropertyPath.Visitor {
        private final String propertyPath;
        private final TypeConverter converter;
        private TypeIntrospectionInfo classInfo;
        private Object value;

        public PropertyEvaluater(String propertyPath, TypeConverter converter, Object value) {
            this.propertyPath = propertyPath;
            this.converter = converter;
            setValue(value);
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            if (value != this.value) {
                if (value == null) {
                    this.value = null;
                    this.classInfo = null;
                } else if (this.value == null || !value.getClass().equals(this.value.getClass())) {
                    this.value = value;
                    this.classInfo = Introspector.getClassInfo(value.getClass());
                }
            }
        }

        public void visitSimpleProperty(String propertyName, String displayName, boolean last) {
            SimpleProperty prop = classInfo.findProperty(SimpleProperty.class, propertyName, null, true, false);

            if (prop != null) {
                Object propValue = prop.getValue(value);

                if (propValue == null && !last) {
                    throwNPE(propertyPath, displayName);
                }

                setValue(propValue);

                return;
            }

            throwFailure(propertyPath, displayName);
        }

        public boolean visitIndexedProperty(String propertyName, int index, String displayName, boolean last) {
            IndexedProperty prop = classInfo.findProperty(IndexedProperty.class, propertyName, null, true, false);

            if (prop == null) {
                return false;
            }

            Object propValue = prop.getValue(value, index);

            if (propValue == null && !last) {
                throwNPE(propertyPath, displayName);
            }

            setValue(propValue);

            return true;
        }

        public boolean visitMappedProperty(String propertyName, String key, String displayName, boolean last) {
            MappedProperty prop = classInfo.findProperty(MappedProperty.class, propertyName, null, true, false);

            if (prop == null) {
                return false;
            }

            Object propValue = prop.getValue(value, key);

            if (propValue == null && !last) {
                throwNPE(propertyPath, displayName);
            }

            setValue(propValue);

            return true;
        }

        public void visitIndex(int index, String displayName, boolean last) {
            if (!visitIndexedProperty(null, index, displayName, last)) {
                throwFailure(propertyPath, displayName);
            }
        }

        public void visitKey(String key, String displayName, boolean last) {
            if (!visitMappedProperty(null, key, displayName, last)) {
                throwFailure(propertyPath, displayName);
            }
        }

        private void throwNPE(String propertyPath, String displayName) {
            throw new PropertyEvaluationFailureException(String.format(
                    "Could not evaluate property \"%s\": value of \"%s\" is null", StringUtil.escapeJava(propertyPath),
                    StringUtil.escapeJava(displayName)));
        }

        private void throwFailure(String propertyPath, String displayName) {
            throw new PropertyEvaluationFailureException(String.format("Could not evaluate property \"%s\": \"%s\"",
                    StringUtil.escapeJava(propertyPath), StringUtil.escapeJava(displayName)));
        }
    }
}
