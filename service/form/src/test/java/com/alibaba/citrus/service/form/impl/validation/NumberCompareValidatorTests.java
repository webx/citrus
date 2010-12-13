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

public class NumberCompareValidatorTests extends AbstractNumberValidatorTests<NumberCompareValidator> {
    @Override
    protected String getGroupName() {
        return "e";
    }

    @Test
    public void init_noFieldName() throws Exception {
        NumberCompareValidator v = newValidator();
        assertNoFieldName(v);

        v = newValidator();
        v.setEqualTo(" ");
        assertNoFieldName(v);
    }

    private void assertNoFieldName(NumberCompareValidator v) throws Exception {
        v.setMessage("message");
        v.afterPropertiesSet();

        try {
            v.init(getGroupConfig().getFieldConfig("field1"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(
                    e,
                    exception("One of the following attributes should be set:",
                            "[equalTo, notEqualTo, lessThan, greaterThan, lessThanOrEqualTo, greaterThanOrEqualTo]"));
        }
    }

    @Test
    public void init_fieldNotExists() throws Exception {
        NumberCompareValidator v = newValidator();
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
    public void validate_thisField_notANumber() throws Exception {
        requestWithExtra(extra("123"), "abc");
        assertEquals(false, field1.isValid());
        assertEquals("field1 must equal to otherField", field1.getMessage());
    }

    @Test
    public void validate_otherField_notANumber() throws Exception {
        requestWithExtra(extra("abc"), "123");
        assertEquals(false, field1.isValid());
        assertEquals("field1 must equal to otherField", field1.getMessage());
    }

    @Test
    public void validate_equalTo() throws Exception {
        requestWithExtra(extra("1234"), "123");
        assertEquals(false, field1.isValid());
        assertEquals("field1 must equal to otherField", field1.getMessage());

        requestWithExtra(extra("123"), "123");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_notEqualTo() throws Exception {
        requestWithExtra(extra("123"), null, "123");
        assertEquals(false, field2.isValid());
        assertEquals("field2 must not equal to otherField", field2.getMessage());

        requestWithExtra(extra("1234"), null, "123");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    @Test
    public void validate_lessThan() throws Exception {
        requestWithExtra(extra("123"), null, null, "123");
        assertEquals(false, field3.isValid());
        assertEquals("field3 must be less than otherField", field3.getMessage());

        requestWithExtra(extra("123"), null, null, "234");
        assertEquals(false, field3.isValid());
        assertEquals("field3 must be less than otherField", field3.getMessage());

        requestWithExtra(extra("123"), null, null, " 1 ");
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());
    }

    @Test
    public void validate_greaterThan() throws Exception {
        requestWithExtra(extra("123"), null, null, null, "123");
        assertEquals(false, field4.isValid());
        assertEquals("field4 must be greater than otherField", field4.getMessage());

        requestWithExtra(extra("123"), null, null, null, "1");
        assertEquals(false, field4.isValid());
        assertEquals("field4 must be greater than otherField", field4.getMessage());

        requestWithExtra(extra("123"), null, null, null, " 234 ");
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());
    }

    @Test
    public void validate_lessThanOrEqualTo() throws Exception {
        requestWithExtra(extra("123"), null, null, null, null, "234");
        assertEquals(false, field5.isValid());
        assertEquals("field5 must be less than or equal to otherField", field5.getMessage());

        requestWithExtra(extra("123"), null, null, null, null, "123");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());

        requestWithExtra(extra("123"), null, null, null, null, " 1 ");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());
    }

    @Test
    public void validate_greaterThanOrEqualTo() throws Exception {
        requestWithExtra(extra("123"), null, null, null, null, null, "1");
        assertEquals(false, field6.isValid());
        assertEquals("field6 must be greater than or equal to otherField", field6.getMessage());

        requestWithExtra(extra("123"), null, null, null, null, null, "123");
        assertEquals(true, field6.isValid());
        assertEquals(null, field6.getMessage());

        requestWithExtra(extra("123"), null, null, null, null, null, " 234 ");
        assertEquals(true, field6.isValid());
        assertEquals(null, field6.getMessage());
    }

    private Object[][] extra(String value) {
        return new Object[][] { { "o", value } };
    }
}
