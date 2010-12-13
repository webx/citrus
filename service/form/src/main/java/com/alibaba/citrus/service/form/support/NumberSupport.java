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
package com.alibaba.citrus.service.form.support;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.alibaba.citrus.util.StringUtil;

/**
 * 用来方便各种类型数字的比较操作。
 * 
 * @author Michael Zhou
 */
public class NumberSupport implements Comparable<NumberSupport> {
    private String stringValue;
    private Number numberValue;
    private Type numberType;

    public NumberSupport() {
    }

    public NumberSupport(Type numberType, String stringValue) {
        setNumberType(numberType);
        setStringValue(stringValue);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = trimToNull(stringValue);
    }

    public Type getNumberType() {
        return numberType;
    }

    public void setNumberType(Type numberType) {
        this.numberType = numberType;
    }

    public Number getValue() throws NumberFormatException, IllegalArgumentException {
        assertNotNull(numberType, "no number type specified");
        assertNotNull(stringValue, "no value set");

        if (numberValue == null) {
            numberValue = numberType.parse(stringValue);
        }

        return numberValue;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(NumberSupport n) {
        Number v1 = getValue();
        Number v2 = n.getValue();
        assertTrue(v1 instanceof Comparable<?>, "not comparable number: %s", v1);
        return ((Comparable) v1).compareTo(v2);
    }

    @Override
    public int hashCode() {
        return 31 + getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (obj instanceof NumberSupport && getNumberType() == ((NumberSupport) obj).getNumberType()) {
            return compareTo((NumberSupport) obj) == 0;
        }

        return false;
    }

    @Override
    public String toString() {
        return numberValue == null ? stringValue : numberValue.toString();
    }

    /**
     * 数字的类型。
     */
    public static enum Type {
        INT {
            @Override
            public Number parse(String value) {
                return Integer.parseInt(value);
            }
        },
        LONG {
            @Override
            public Number parse(String value) {
                return Long.parseLong(value);
            }
        },
        FLOAT {
            @Override
            public Number parse(String value) {
                return Float.parseFloat(value);
            }
        },
        DOUBLE {
            @Override
            public Number parse(String value) {
                return Double.parseDouble(value);
            }
        },
        BIG_DECIMAL {
            @Override
            public Number parse(String value) {
                if (value == null || !numberPattern.matcher(value).matches()) {
                    throw new NumberFormatException(value);
                }

                return new BigDecimal(value);
            }
        };

        private final static Pattern numberPattern = Pattern.compile("(\\+|-)?[\\d\\.]+");

        public static Type byName(String name) {
            name = defaultIfNull(trimToNull(name), "INT");
            String typeName = StringUtil.toUpperCaseWithUnderscores(name);

            try {
                return Type.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("wrong number type: " + name);
            }
        }

        public abstract Number parse(String value) throws NumberFormatException;
    }

    public static class TypeEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(Type.byName(text));
        }
    }
}
