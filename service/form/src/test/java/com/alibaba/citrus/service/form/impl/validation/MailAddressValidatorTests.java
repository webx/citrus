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

public class MailAddressValidatorTests extends AbstractValidatorTests<MailAddressValidator> {
    @Override
    protected String getGroupName() {
        return "j";
    }

    @Test
    public void validate_mail() throws Exception {
        request("abc");
        assertEquals(false, field1.isValid());
        assertEquals("field1 is not a valid mail address", field1.getMessage());

        request("abc@.com");
        assertEquals(false, field1.isValid());
        assertEquals("field1 is not a valid mail address", field1.getMessage());

        request("abc @ def.com");
        assertEquals(false, field1.isValid());
        assertEquals("field1 is not a valid mail address", field1.getMessage());

        request("abc@def");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }
}
