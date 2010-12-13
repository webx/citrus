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

import static org.junit.Assert.*;

import org.junit.Test;

public class StringLengthValidatorTests extends AbstractValidatorTests<StringLengthValidator> {
    @Override
    protected String getGroupName() {
        return "b";
    }

    @Test
    public void validate_lessThan3() throws Exception {
        request("a", "中");

        assertEquals(false, field1.isValid());
        assertEquals("field1 length must be at least 3", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("field2 length must be at least 3 and be less than 10", field2.getMessage());
    }

    @Test
    public void validate_between3and10() throws Exception {
        request("abc", "中国人");

        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        request("abcd", "中国人民");

        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        request("abcdefghij", "hi中华民国");

        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    @Test
    public void validate_moreThan10() throws Exception {
        request("abcdefghijk", "中华人民共和国中央人民政府");

        assertEquals(false, field1.isValid());
        assertEquals("field1 length must be less than 10", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("field2 length must be at least 3 and be less than 10", field2.getMessage());
    }
}
