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
package com.alibaba.citrus.generictype.introspect.impl;

import java.lang.reflect.Array;

import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.IndexedPropertyInfo;
import com.alibaba.citrus.generictype.introspect.PropertyEvaluationFailureException;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;

/**
 * 分析数组，将数组看作indexed属性来操作。
 * 
 * @author Michael Zhou
 */
public class ArrayPropertiesFinder extends SinglePropertyFinder {
    @Override
    protected PropertyInfo createPropertyInfo(TypeInfo type) {
        if (type.isArray()) {
            return new ArrayPropertyImpl(type);
        }

        return null;
    }

    /**
     * 代表一个数组的属性信息。
     */
    private static class ArrayPropertyImpl extends AbstractPropertyInfo implements IndexedPropertyInfo {
        private ArrayPropertyImpl(TypeInfo declaringType) {
            super(null, declaringType, declaringType.getDirectComponentType(), null, null);
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        public Object getValue(Object object, int index) {
            try {
                return Array.get(object, index);
            } catch (Exception e) {
                throw new PropertyEvaluationFailureException(e);
            }
        }

        @Override
        public void setValue(Object object, int index, Object value) {
        }

        @Override
        protected String getDescription() {
            return "IndexeProperty (Array)";
        }
    }
}
