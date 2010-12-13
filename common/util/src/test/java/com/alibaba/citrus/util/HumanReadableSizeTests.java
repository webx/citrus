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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.beans.PropertyEditor;

import org.hamcrest.Matcher;
import org.junit.Test;

public class HumanReadableSizeTests {
    @Test
    public void parse_empty() {
        assertParseError(null, exception(IllegalArgumentException.class, "human readble size"));
        assertParseError("  ", exception(IllegalArgumentException.class, "human readble size"));
    }

    @Test
    public void parse_wrong_format() {
        assertParseError("abc", exception(IllegalArgumentException.class, "wrong format: ", "abc"));
        assertParseError("abc K", exception(IllegalArgumentException.class, "wrong format: abc K"));
        assertParseError("123 KK", exception(IllegalArgumentException.class, "wrong format: 123 KK"));
        assertParseError(" K", exception(IllegalArgumentException.class, "wrong format: K"));
        assertParseError(" 1.5. K", exception(IllegalArgumentException.class, "wrong format: 1.5. K"));
    }

    private void assertParseError(String input, Matcher<Throwable> matcher) {
        try {
            HumanReadableSize.parse(input);
            fail();
        } catch (Exception e) {
            assertThat(e, matcher);
        }
    }

    @Test
    public void parse() {
        // bytes
        assertEquals(1, HumanReadableSize.parse(" 1"));
        assertEquals(512, HumanReadableSize.parse(" 512"));
        assertEquals(512, HumanReadableSize.parse(" 512 "));
        assertEquals(10, HumanReadableSize.parse(" 10.24"));

        // K
        assertEquals(1024, HumanReadableSize.parse(" 1 k"));
        assertEquals(2048, HumanReadableSize.parse(" 2 K"));
        assertEquals(1536, HumanReadableSize.parse(" 1.5 K"));

        // M
        assertEquals(1048576, HumanReadableSize.parse(" 1 m"));
        assertEquals(2097152, HumanReadableSize.parse(" 2 M"));
        assertEquals(1572864, HumanReadableSize.parse(" 1.5 M"));

        // G
        assertEquals(1073741824L, HumanReadableSize.parse(" 1 g"));
        assertEquals(2147483648L, HumanReadableSize.parse(" 2 G"));
        assertEquals(1610612736L, HumanReadableSize.parse(" 1.5 G"));

        // T
        assertEquals(1099511627776L, HumanReadableSize.parse(" 1 t"));
        assertEquals(2199023255552L, HumanReadableSize.parse(" 2 T"));
        assertEquals(1649267441664L, HumanReadableSize.parse(" 1.5 T"));
        assertEquals(2251799813685248L, HumanReadableSize.parse(" 2048 T"));
    }

    @Test
    public void toHumanReadable() {
        // n/a
        assertEquals("n/a", HumanReadableSize.toHumanReadble(-100));
        assertEquals("n/a", HumanReadableSize.toHumanReadble(-1));

        // bytes
        assertEquals("0", HumanReadableSize.toHumanReadble(0));
        assertEquals("123", HumanReadableSize.toHumanReadble(123));
        assertEquals("1023", HumanReadableSize.toHumanReadble(1023));

        // K
        assertEquals("1K", HumanReadableSize.toHumanReadble(1024));
        assertEquals("1.5K", HumanReadableSize.toHumanReadble(1536));
        assertEquals("1.23K", HumanReadableSize.toHumanReadble(1260));

        // M
        assertEquals("1M", HumanReadableSize.toHumanReadble(1048576));
        assertEquals("1.5M", HumanReadableSize.toHumanReadble(1572864));
        assertEquals("1.23M", HumanReadableSize.toHumanReadble(1289750));

        // G
        assertEquals("1G", HumanReadableSize.toHumanReadble(1073741824L));
        assertEquals("1.5G", HumanReadableSize.toHumanReadble(1610612736L));
        assertEquals("1.23G", HumanReadableSize.toHumanReadble(1320702444));

        // T
        assertEquals("1T", HumanReadableSize.toHumanReadble(1099511627776L));
        assertEquals("1.5T", HumanReadableSize.toHumanReadble(1649267441664L));
        assertEquals("1.23T", HumanReadableSize.toHumanReadble(1352399302165L));
        assertEquals("2048T", HumanReadableSize.toHumanReadble(2251799813685248L));
    }

    @Test
    public void newInstance_error() {
        assertNewInstanceError(null, exception(IllegalArgumentException.class, "human readble size"));
        assertNewInstanceError("  ", exception(IllegalArgumentException.class, "human readble size"));

        assertNewInstanceError("abc", exception(IllegalArgumentException.class, "wrong format: ", "abc"));
        assertNewInstanceError("abc K", exception(IllegalArgumentException.class, "wrong format: abc K"));
        assertNewInstanceError("123 KK", exception(IllegalArgumentException.class, "wrong format: 123 KK"));
        assertNewInstanceError(" K", exception(IllegalArgumentException.class, "wrong format: K"));
        assertNewInstanceError(" 1.5. K", exception(IllegalArgumentException.class, "wrong format: 1.5. K"));

        assertNewInstanceError(" -100", exception(IllegalArgumentException.class, "wrong format: -100"));
    }

    private void assertNewInstanceError(String input, Matcher<Throwable> matcher) {
        try {
            HumanReadableSize.parse(input);
            fail();
        } catch (Exception e) {
            assertThat(e, matcher);
        }
    }

    @Test
    public void newInstance_humanReadable() {
        assertHumanReadable(" -1 ", -1, "n/a");
        assertHumanReadable("n/A", -1, "n/a");

        assertHumanReadable("1024", 1024, "1K");
        assertHumanReadable("1536", 1536, "1.5K");
        assertHumanReadable("1537", 1537, "1.5K");
        assertHumanReadable("1073741824", 1073741824L, "1G");
    }

    private void assertHumanReadable(String input, long value, String normalized) {
        HumanReadableSize hrs = new HumanReadableSize(input);
        assertEquals(value, hrs.getValue());
        assertEquals(normalized, hrs.getHumanReadable());
        assertEquals(normalized, hrs.toString());
    }

    @Test
    public void newInstance_value() {
        assertValue(-1, -1, "n/a");
        assertValue(-100, -1, "n/a");

        assertValue(1024, 1024, "1K");
        assertValue(1536, 1536, "1.5K");
        assertValue(1537, 1537, "1.5K");
        assertValue(1073741824L, 1073741824L, "1G");
    }

    private void assertValue(long input, long value, String normalized) {
        HumanReadableSize hrs = new HumanReadableSize(input);
        assertEquals(value, hrs.getValue());
        assertEquals(normalized, hrs.getHumanReadable());
        assertEquals(normalized, hrs.toString());
    }

    @Test
    public void equalsHashCode() {
        assertEqualsHashCode("-1", "n/A", true);

        assertEqualsHashCode("123", " 123 ", true);
        assertEqualsHashCode("123", " 124 ", false);
        assertEqualsHashCode("1024", " 1025 ", false);
    }

    private void assertEqualsHashCode(String v1, String v2, boolean equals) {
        HumanReadableSize hrs1 = new HumanReadableSize(v1);
        HumanReadableSize hrs2 = new HumanReadableSize(v2);

        if (equals) {
            assertEquals(hrs1, hrs2);
            assertEquals(hrs1.hashCode(), hrs2.hashCode());
            assertTrue(hrs1.hashCode() > 0);
        } else {
            assertTrue(!hrs1.equals(hrs2));
            assertTrue(hrs1.hashCode() != hrs2.hashCode());
            assertTrue(hrs1.hashCode() > 0);
            assertTrue(hrs2.hashCode() > 0);
        }
    }

    @Test
    public void editor() {
        PropertyEditor editor = new HumanReadableSizeEditor();

        editor.setAsText("1024");
        assertEquals(new HumanReadableSize("1024"), editor.getValue());
        assertEquals("1K", editor.getAsText());
    }
}
