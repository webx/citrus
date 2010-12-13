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
package com.alibaba.citrus.service.form.impl.validation;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class DateValidatorTests extends AbstractValidatorTests<DateValidator> {
    @Override
    protected String getGroupName() {
        return "h";
    }

    @Test
    public void normalizeDateString() {
        assertEquals(null, DateValidator.normalizeDateString(null));
        assertEquals(null, DateValidator.normalizeDateString("  "));
        assertEquals("aa 1-2-3 4:58 bb", DateValidator.normalizeDateString(" aa  01-02-003   004:58 bb"));
    }

    @Test
    public void init_format() throws Exception {
        DateValidator v;

        // default value
        v = newValidator();
        v.setMessage("message");
        v.afterPropertiesSet();
        assertEquals("yyyy-MM-dd", v.getFormat());

        // empty 
        v = newValidator();
        v.setMessage("message");
        v.setFormat(null);
        v.afterPropertiesSet();
        assertEquals("yyyy-MM-dd", v.getFormat());

        v = newValidator();
        v.setMessage("message");
        v.setFormat("  ");
        v.afterPropertiesSet();
        assertEquals("yyyy-MM-dd", v.getFormat());

        // with value
        v = newValidator();
        v.setMessage("message");
        v.setFormat(" yyyy-MM-dd hh:mm:ss ");
        v.afterPropertiesSet();
        assertEquals("yyyy-MM-dd hh:mm:ss", v.getFormat());

        // wrong format
        v = newValidator();
        v.setMessage("message");
        v.setFormat("abcdefghijklmnopqrstuvwxyz");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Illegal pattern character"));
        }
    }

    @Test
    public void init_minDate() throws Exception {
        DateValidator v = newValidator();

        // empty
        v.setMinDate(null);
        assertEquals(null, v.getMinDate());

        v.setMinDate("  ");
        assertEquals(null, v.getMinDate());

        // with value
        v.setMinDate(" 1911-10-10 10:10 ");
        assertEquals("1911-10-10 10:10", v.getMinDate());

        // init
        v.setMessage("message");
        v.afterPropertiesSet();
        assertEquals("1911-10-10", v.getMinDate());

        // wrong format
        v = newValidator();
        v.setMessage("message");
        v.setMinDate("1911");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (ParseException e) {
            assertThat(e, exception("1911"));
        }

        // wrong format 2：和指定format格式不一致
        v = newValidator();
        v.setMessage("message");
        v.setFormat("yyyy-MM-dd HH:mm:ss");
        v.setMinDate("1911-10-10");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (ParseException e) {
            assertThat(e, exception("1911-10-10"));
        }

        v = newValidator();
        v.setMessage("message");
        v.setFormat("yyyy-MM-dd HH:mm:ss");
        v.setMinDate("1911-10-10 10:10:10");
        v.afterPropertiesSet();
    }

    @Test
    public void init_maxDate() throws Exception {
        DateValidator v = newValidator();

        // empty
        v.setMaxDate(null);
        assertEquals(null, v.getMaxDate());

        v.setMaxDate("  ");
        assertEquals(null, v.getMaxDate());

        // with value
        v.setMaxDate(" 1911-10-10 10:10 ");
        assertEquals("1911-10-10 10:10", v.getMaxDate());

        // init
        v.setMessage("message");
        v.afterPropertiesSet();
        assertEquals("1911-10-10", v.getMaxDate());

        // wrong value
        v = newValidator();
        v.setMessage("message");
        v.setMaxDate("1911");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (ParseException e) {
            assertThat(e, exception("1911"));
        }

        // wrong format 2：和指定format格式不一致
        v = newValidator();
        v.setMessage("message");
        v.setFormat("yyyy-MM-dd HH:mm:ss");
        v.setMaxDate("1911-10-10");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (ParseException e) {
            assertThat(e, exception("1911-10-10"));
        }

        v = newValidator();
        v.setMessage("message");
        v.setFormat("yyyy-MM-dd HH:mm:ss");
        v.setMaxDate("1911-10-10 10:10:10");
        v.afterPropertiesSet();
    }

    @Test
    public void validate_default() throws Exception {
        // wrong format
        request(" 1911- ");
        assertEquals(false, field1.isValid());
        assertEquals("field1 should be of format yyyy-MM-dd", field1.getMessage());

        // right format
        request(" 1911-10-10  ");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        request(" 1911-2-28  ");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        // redundent info
        request(" 1911-10-10 10:10 ");
        assertEquals(false, field1.isValid());
        assertEquals("field1 should be of format yyyy-MM-dd", field1.getMessage());

        // overflow
        request(" 1911-2-30 ");
        assertEquals(false, field1.isValid());
        assertEquals("field1 should be of format yyyy-MM-dd", field1.getMessage());
    }

    @Test
    public void validate_format() throws Exception {
        // wrong format
        request(null, " 1911-01-01 ");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should be of format yyyy-MM-dd HH:mm:ss", field2.getMessage());

        // right format
        request(null, " 1911-10-10 11:01:02 ");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        request(null, " 1911-2-28 13:01:02 ");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        // redundent info
        request(null, " 1911-10-10 10:10:10.12 ");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should be of format yyyy-MM-dd HH:mm:ss", field2.getMessage());

        // overflow
        request(null, " 1911-2-28 12:70:10");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should be of format yyyy-MM-dd HH:mm:ss", field2.getMessage());
    }

    @Test
    public void validate_minDate() throws Exception {
        request(null, null, " 1911-10-10 ");
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        request(null, null, " 1911-10-11 ");
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        request(null, null, " 1911-10-9 ");
        assertEquals(false, field3.isValid());
        assertEquals("field3 should be after 1911-10-10", field3.getMessage());
    }

    @Test
    public void validate_maxDate() throws Exception {
        request(null, null, null, " 1911-10-10 ");
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());

        request(null, null, null, " 1911-10-11 ");
        assertEquals(false, field4.isValid());
        assertEquals("field4 should be earlier than 1911-10-10", field4.getMessage());

        request(null, null, null, " 1911-10-9 ");
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());
    }

    @Test
    public void validate_minDate_maxDate_format() throws Exception {
        request(null, null, null, null, " 1911-10-10 ");
        assertEquals(false, field5.isValid());
        assertEquals(
                "field5 should be date between 1911-10-10 00:10:10 and 1911-10-10 10:10:10 in format yyyy-MM-dd HH:mm:ss",
                field5.getMessage());

        request(null, null, null, null, " 1911-10-10 0:10:9");
        assertEquals(false, field5.isValid());
        assertEquals(
                "field5 should be date between 1911-10-10 00:10:10 and 1911-10-10 10:10:10 in format yyyy-MM-dd HH:mm:ss",
                field5.getMessage());

        request(null, null, null, null, " 1911-10-10 10:10:11");
        assertEquals(false, field5.isValid());
        assertEquals(
                "field5 should be date between 1911-10-10 00:10:10 and 1911-10-10 10:10:10 in format yyyy-MM-dd HH:mm:ss",
                field5.getMessage());

        request(null, null, null, null, " 1911-10-10 0:10:10");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());

        request(null, null, null, null, " 1911-10-10 10:10:10");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());
    }
}
