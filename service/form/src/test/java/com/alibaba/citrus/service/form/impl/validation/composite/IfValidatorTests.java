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

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.form.Condition;
import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.impl.validation.MyValidator;

public class IfValidatorTests extends AbstractSimpleCompositeValidatorTests<IfValidator> {
    @Override
    protected String getGroupName() {
        return "r";
    }

    @Override
    protected void initFor_AbstractSimpleCompositeValidatorTests(IfValidator v) {
        v.setCondition(createMock(Condition.class));
    }

    @Test
    public void init_noCondition() throws Exception {
        IfValidator v = newValidator();
        v.setValidators(createArrayList((Validator) new MyValidator()));

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no condition"));
        }
    }

    @Test
    public void validate_testAttribute() throws Exception {
        // condition satisfied, inner validator 1 failed
        requestWithExtra(new Object[][] { { "o", "true" } }, "false, true");
        assertEquals(false, field1.isValid());
        assertEquals("field1 validator 1", field1.getMessage());

        // condition not satisfied, validator 2 passed
        requestWithExtra(new Object[][] { { "o", "false" } }, "false, true");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        // condition not satisfied, validator 2 passed
        requestWithExtra(new Object[][] { { "o", "false" } }, "false, false");
        assertEquals(false, field1.isValid());
        assertEquals("field1 validator 2", field1.getMessage());
    }

    @Test
    public void validate_condition() throws Exception {
        // condition satisfied, inner validator 1 failed
        requestWithExtra(new Object[][] { { "o", "hello" } }, "", "false, true");
        assertEquals(false, field2.isValid());
        assertEquals("field2 validator 1", field2.getMessage());

        // condition not satisfied, validator 2 passed
        requestWithExtra(new Object[][] { { "o", "world" } }, "", "false, true");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        // condition not satisfied, validator 2 failed
        requestWithExtra(new Object[][] { { "o", "world" } }, "", "false, false");
        assertEquals(false, field2.isValid());
        assertEquals("field2 validator 2", field2.getMessage());
    }
}
