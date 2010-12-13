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
package com.alibaba.citrus.service.form.impl.validation.composite;

import static org.junit.Assert.*;

import org.junit.Test;

public class AllOfValuesValidatorTests extends AbstractSimpleCompositeValidatorTests<AllOfValuesValidator> {
    @Override
    protected String getGroupName() {
        return "n";
    }

    @Test
    public void validate_allTrue() throws Exception {
        request((Object) new String[] { "true", "true", "true" });
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_someTrue() throws Exception {
        request((Object) new String[] { "true", "false", "true" });
        assertEquals(false, field1.isValid());
        assertEquals("field1: [field1[1] validator]", field1.getMessage());
    }

    @Test
    public void validate_allFalse() throws Exception {
        request((Object) new String[] { "false", "false", "false" });
        assertEquals(false, field1.isValid());
        assertEquals("field1: [field1[0] validator]", field1.getMessage());
    }

    /**
     * 检查每个值是否分别被传递到子validator中，特别是：子validator为composite validator的情况。
     */
    @Test
    public void validate_hidden_all_of_validator() throws Exception {
        request(null, new String[] { "true", "false", "true" });
        assertEquals(false, field2.isValid());
        assertEquals("field2: [field2[1] validator]", field2.getMessage());
    }
}
