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

public class RequiredValidatorTests extends AbstractValidatorTests<RequiredValidator> {
    @Override
    protected String getGroupName() {
        return "a";
    }

    @Test
    public void validate_null() throws Exception {
        request((String) null);
        assertEquals(false, field1.isValid());
        assertEquals("field1 is required", field1.getMessage());
    }

    @Test
    public void validate_empty() throws Exception {
        request("");
        assertEquals(false, field1.isValid());
        assertEquals("field1 is required", field1.getMessage());
    }

    @Test
    public void validate_blank() throws Exception {
        request(" ");
        assertEquals(false, field1.isValid());
        assertEquals("field1 is required", field1.getMessage());
    }

    @Test
    public void validate_notEmpty() throws Exception {
        request(" hello ");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_empty_noTrimming() throws Exception {
        request(null, null);
        assertEquals(false, field2.isValid());
        assertEquals("field2 is required", field2.getMessage());

        request(null, "");
        assertEquals(false, field2.isValid());
        assertEquals("field2 is required", field2.getMessage());
    }

    @Test
    public void validate_blank_noTrimming() throws Exception {
        request(null, " ");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }
}
