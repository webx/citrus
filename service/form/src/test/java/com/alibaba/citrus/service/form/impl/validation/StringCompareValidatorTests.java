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

import org.junit.Test;

public class StringCompareValidatorTests extends AbstractValidatorTests<StringCompareValidator> {
    @Override
    protected String getGroupName() {
        return "f";
    }

    @Test
    public void init_noFieldName() throws Exception {
        StringCompareValidator v = newValidator();
        assertNoFieldName(v);

        v = newValidator();
        v.setEqualTo(" ");
        assertNoFieldName(v);
    }

    private void assertNoFieldName(StringCompareValidator v) throws Exception {
        v.setMessage("message");
        v.afterPropertiesSet();

        try {
            v.init(getGroupConfig().getFieldConfig("field1"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("One of the following attributes should be set:", "[equalTo, notEqualTo]"));
        }
    }

    @Test
    public void init_fieldNotExists() throws Exception {
        StringCompareValidator v = newValidator();
        v.setEqualTo("notExistField");
        v.setMessage("message");
        v.afterPropertiesSet();

        try {
            v.init(getGroupConfig().getFieldConfig("field1"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Field notExistField not exists"));
        }
    }

    @Test
    public void validate_equalTo() throws Exception {
        requestWithExtra(extra("ABC"), "abc");
        assertEquals(false, field1.isValid());
        assertEquals("field1 must equal to otherField", field1.getMessage());

        requestWithExtra(extra(""), "  ");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        requestWithExtra(extra("abc "), "abc");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_equalTo_ignoreCase() throws Exception {
        requestWithExtra(extra("ABCd"), null, "abc");
        assertEquals(false, field2.isValid());
        assertEquals("field2 must equal to otherField in case insensitive mode", field2.getMessage());

        requestWithExtra(extra("ABC "), null, "abc");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    @Test
    public void validate_notEqualTo() throws Exception {
        requestWithExtra(extra("abc "), null, null, "abc");
        assertEquals(false, field3.isValid());
        assertEquals("field3 must not equal to otherField", field3.getMessage());

        requestWithExtra(extra("ABC"), null, null, "abc");
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());
    }

    @Test
    public void validate_notEqualTo_ignoreCase() throws Exception {
        requestWithExtra(extra("ABc "), null, null, null, "abc");
        assertEquals(false, field4.isValid());
        assertEquals("field4 must not equal to otherField in case insensitive mode", field4.getMessage());

        requestWithExtra(extra("ABCd"), null, null, null, "abc");
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());
    }

    @Test
    public void validate_equalTo_noTrimming() throws Exception {
        requestWithExtra(extra("ABC"), null, null, null, null, "abc");
        assertEquals(false, field5.isValid());
        assertEquals("field5 must equal to otherField", field5.getMessage());

        requestWithExtra(extra("abc"), null, null, null, null, " abc ");
        assertEquals(false, field5.isValid());
        assertEquals("field5 must equal to otherField", field5.getMessage());

        requestWithExtra(extra(""), null, null, null, null, "  ");
        assertEquals(false, field5.isValid());
        assertEquals("field5 must equal to otherField", field5.getMessage());

        requestWithExtra(extra(""), null, null, null, null, "");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());

        requestWithExtra(extra("abc "), null, null, null, null, "abc");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());
    }

    private Object[][] extra(String value) {
        return new Object[][] { { "o", value } };
    }
}
