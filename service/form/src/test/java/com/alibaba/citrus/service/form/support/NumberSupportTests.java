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

import static com.alibaba.citrus.service.form.support.NumberSupport.Type.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.alibaba.citrus.service.form.support.NumberSupport.Type;

public class NumberSupportTests {
    @Test
    public void init_noType() {
        try {
            new NumberSupport().getValue();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no number type specified"));
        }
    }

    @Test
    public void init_noValue() {
        try {
            new NumberSupport(INT, null).getValue();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no value set"));
        }
    }

    @Test
    public void getNumberType_byName() {
        // no numberType
        assertEquals(INT, Type.byName(null));

        // wrong numberType
        try {
            Type.byName("wrong");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, "wrong number type: wrong"));
        }

        // types
        assertEquals(INT, Type.byName("int"));
        assertEquals(LONG, Type.byName("long"));
        assertEquals(FLOAT, Type.byName("float"));
        assertEquals(DOUBLE, Type.byName("double"));
        assertEquals(BIG_DECIMAL, Type.byName("bigDecimal"));
    }

    @Test
    public void getStringValue() {
        assertEquals("123", new NumberSupport(INT, "123").getStringValue());
    }

    @Test
    public void getNumberType() {
        assertEquals(INT, new NumberSupport(INT, "123").getNumberType());
    }

    @Test
    public void parseNumber() {
        // wrong format
        assertParse(INT, "123.4", null);
        assertParse(LONG, "123.4", null);
        assertParse(FLOAT, "abc", null);
        assertParse(DOUBLE, "abc", null);
        assertParse(BIG_DECIMAL, "abc", null);

        // succcess
        assertParse(INT, "123", 123);
        assertParse(LONG, "123456789123456789", 123456789123456789L);
        assertParse(FLOAT, "123.456", 123.456F);
        assertParse(DOUBLE, "123456789123456789.123456789", 123456789123456789.123456789D);
        assertParse(BIG_DECIMAL, "123456789123456789123456789123456789123456789.123456789123456789123456789",
                new BigDecimal("123456789123456789123456789123456789123456789.123456789123456789123456789"));
    }

    private void assertParse(Type type, String value, Number result) {
        NumberSupport number = new NumberSupport(type, value);

        if (result != null) {
            assertEquals(result, number.getValue());
        } else {
            try {
                number.getValue();
                fail();
            } catch (NumberFormatException e) {
                assertThat(e, exception(value)); // For input string: value
            }
        }
    }

    @Test
    public void compareTo() {
        // int
        assertCompareTo(-1, new NumberSupport(INT, "123"), new NumberSupport(INT, "234"));
        assertCompareTo(0, new NumberSupport(INT, "123"), new NumberSupport(INT, "123"));
        assertCompareTo(1, new NumberSupport(INT, "123"), new NumberSupport(INT, "012"));

        // long
        assertCompareTo(-1, new NumberSupport(LONG, "123"), new NumberSupport(LONG, "234"));
        assertCompareTo(0, new NumberSupport(LONG, "123"), new NumberSupport(LONG, "123"));
        assertCompareTo(1, new NumberSupport(LONG, "123"), new NumberSupport(LONG, "012"));

        // float
        assertCompareTo(-1, new NumberSupport(FLOAT, "123"), new NumberSupport(FLOAT, "234"));
        assertCompareTo(0, new NumberSupport(FLOAT, "123"), new NumberSupport(FLOAT, "123"));
        assertCompareTo(1, new NumberSupport(FLOAT, "123"), new NumberSupport(FLOAT, "012"));

        // double
        assertCompareTo(-1, new NumberSupport(DOUBLE, "123"), new NumberSupport(DOUBLE, "234"));
        assertCompareTo(0, new NumberSupport(DOUBLE, "123"), new NumberSupport(DOUBLE, "123"));
        assertCompareTo(1, new NumberSupport(DOUBLE, "123"), new NumberSupport(DOUBLE, "012"));

        // bigDecimal
        assertCompareTo(-1, new NumberSupport(BIG_DECIMAL, "123"), new NumberSupport(BIG_DECIMAL, "234"));
        assertCompareTo(0, new NumberSupport(BIG_DECIMAL, "123"), new NumberSupport(BIG_DECIMAL, "123"));
        assertCompareTo(1, new NumberSupport(BIG_DECIMAL, "123"), new NumberSupport(BIG_DECIMAL, "012"));

        // hybrid
        try {
            new NumberSupport(INT, "123").compareTo(new NumberSupport(DOUBLE, "234"));
            fail();
        } catch (ClassCastException e) {
            assertThat(e, exception("java.lang.Double"));
        }
    }

    private void assertCompareTo(int result, NumberSupport n1, NumberSupport n2) {
        if (result < 0) {
            assertTrue(n1.compareTo(n2) < 0);
            assertFalse(n1.equals(n2));
        } else if (result == 0) {
            assertTrue(n1.compareTo(n2) == 0);
            assertTrue(n1.equals(n2));
        } else {
            assertTrue(n1.compareTo(n2) > 0);
            assertFalse(n1.equals(n2));
        }
    }

    @Test
    public void equalsHashCode() {
        NumberSupport n1 = new NumberSupport(INT, "123");
        NumberSupport n2 = new NumberSupport(INT, "234");
        NumberSupport n3 = new NumberSupport(DOUBLE, "123");
        NumberSupport n4 = new NumberSupport(INT, "123");

        assertEqualsHashCode(false, n1, n2);
        assertEqualsHashCode(false, n1, n3);
        assertEqualsHashCode(true, n1, n4);
        assertEqualsHashCode(false, n3, n4);
    }

    private void assertEqualsHashCode(boolean equals, NumberSupport n1, NumberSupport n2) {
        assertEquals(equals, n1.equals(n2));
        assertEquals(equals, n1.hashCode() == n2.hashCode());
    }

    @Test
    public void toString_() {
        assertEquals("123", new NumberSupport(INT, "123").toString());
        assertEquals("234", new NumberSupport(INT, "234").toString());
        assertEquals("123", new NumberSupport(DOUBLE, "123").toString());
        assertEquals("123", new NumberSupport(INT, "123").toString());

        assertEquals(null, new NumberSupport().toString());
    }
}
