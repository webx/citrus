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
package com.alibaba.citrus.util.i18n;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.junit.Test;

public class LocaleInfoTests {
    private LocaleInfo localeInfo;

    @Test
    public void createSystemLocaleInfo() {
        localeInfo = new LocaleInfo();

        assertNotNull(localeInfo.getLocale());
        assertNotNull(localeInfo.getCharset());
    }

    @Test
    public void create_noLocale_noCharset() {
        try {
            LocaleUtil.setDefault(Locale.CHINA, "GB18030");

            localeInfo = new LocaleInfo(null);

            assertEquals(Locale.CHINA, localeInfo.getLocale());
            assertEquals("GB18030", localeInfo.getCharset().name());
        } finally {
            LocaleUtil.resetDefault();
        }
    }

    @Test
    public void create_withLocale_noCharset() {
        try {
            LocaleUtil.setDefault(Locale.CHINA, "GB18030");

            localeInfo = new LocaleInfo(Locale.US);

            assertEquals(Locale.US, localeInfo.getLocale());
            assertEquals("UTF-8", localeInfo.getCharset().name()); // È«ÄÜcharset
        } finally {
            LocaleUtil.resetDefault();
        }
    }

    @Test
    public void create_withLocale_withCharset() {
        try {
            LocaleUtil.setDefault(Locale.CHINA, "GB18030");

            localeInfo = new LocaleInfo(Locale.US, "8859_1");

            assertEquals(Locale.US, localeInfo.getLocale());
            assertEquals("ISO-8859-1", localeInfo.getCharset().name());
        } finally {
            LocaleUtil.resetDefault();
        }
    }

    @Test
    public void create_noFallback() {
        try {
            new LocaleInfo(null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("fallbackLocaleInfo"));
        }
    }

    @Test
    public void equalsHashCode() {
        LocaleInfo l1 = new LocaleInfo(Locale.CHINA, "GB18030");
        LocaleInfo l2 = new LocaleInfo(Locale.CHINA, "GB18030");
        LocaleInfo l3 = new LocaleInfo(Locale.US, "8859_1");

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());

        assertThat(l1, not(equalTo(l3)));
        assertThat(l1.hashCode(), not(equalTo(l3.hashCode())));

        assertTrue(l1.equals(l1));
        assertFalse(l1.equals(null));
        assertFalse(l1.equals("not a locale"));
    }

    @Test
    public void clone_() {
        localeInfo = new LocaleInfo(Locale.US, "8859_1");

        assertNotSame(localeInfo, localeInfo.clone());
        assertEquals(localeInfo, localeInfo.clone());
    }

    @Test
    public void toString_() {
        localeInfo = new LocaleInfo(Locale.US, "8859_1");
        assertEquals("en_US:ISO-8859-1", localeInfo.toString());
    }

    @Test
    public void parse() {
        try {
            localeInfo = LocaleInfo.parse(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no locale name"));
        }

        try {
            localeInfo = LocaleInfo.parse("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no locale name"));
        }

        assertEquals("en_US:UTF-8", LocaleInfo.parse(" en_US ").toString());
        assertEquals("en_US:UTF-8", LocaleInfo.parse(" en_US : ").toString());
        assertEquals("en_US:ISO-8859-1", LocaleInfo.parse(" en_US : 8859_1").toString());
    }

    @Test
    public void serialize() throws Exception {
        localeInfo = new LocaleInfo(Locale.US, "8859_1");

        // write
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(localeInfo);
        oos.close();

        // read
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        LocaleInfo copy = (LocaleInfo) ois.readObject();

        assertNotSame(localeInfo, copy);
        assertEquals(localeInfo, copy);
    }
}
