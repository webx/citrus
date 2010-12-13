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
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.impl.validation.MyValidator;

public class ChooseValidatorTests extends AbstractCompositeValidatorTests<ChooseValidator> {
    @Override
    protected String getGroupName() {
        return "s";
    }

    @Test
    public void init_validators() throws Exception {
        ChooseValidator v = newValidator();

        // no validators
        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no validators"));
        }

        // wrong validators
        v = newValidator();
        v.setValidators(createArrayList((Validator) new MyValidator()));

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expected <when>"));
        }

        v = newValidator();
        v.setValidators(createArrayList((Validator) new ChooseValidator.When(), new ChooseValidator.Otherwise(),
                new ChooseValidator.When()));

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expected <when>"));
        }

        v = newValidator();
        v.setValidators(createArrayList((Validator) new ChooseValidator.When(), new MyValidator()));

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expected <when> or <otherwise>"));
        }

        // right validators
        v = newValidator();
        v.setValidators(createArrayList((Validator) new ChooseValidator.When(), new ChooseValidator.When(),
                new ChooseValidator.When()));
        v.afterPropertiesSet();

        v = newValidator();
        v.setValidators(createArrayList((Validator) new ChooseValidator.When(), new ChooseValidator.When(),
                new ChooseValidator.Otherwise()));
        v.afterPropertiesSet();
    }

    @Test
    public void init_when() throws Exception {
        ChooseValidator.When v = new ChooseValidator.When();

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no validators"));
        }

        v = new ChooseValidator.When();
        v.setValidators(createArrayList((Validator) new MyValidator()));

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no condition"));
        }
    }

    @Test
    public void init_otherwise() throws Exception {
        ChooseValidator.Otherwise v = new ChooseValidator.Otherwise();

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no validators"));
        }
    }

    @Test
    public void validate_when_when_otherwise() throws Exception {
        // when1 -> validator 1 failed
        requestWithExtra(new Object[][] { { "o", "true" }, { "ot", "true" } }, "false, true, true");
        assertEquals(false, field1.isValid());
        assertEquals("field1 validator 1", field1.getMessage());

        // when1 -> validator 1 success
        requestWithExtra(new Object[][] { { "o", "true" }, { "ot", "true" } }, "true, false, false");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        // when1 passed, when2 -> validator 2 failed
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "true" } }, "false, false, true");
        assertEquals(false, field1.isValid());
        assertEquals("field1 validator 2", field1.getMessage());

        // when1 passed, when2 -> validator 2 success
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "true" } }, "false, true, false");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        // when1, when2 passed, otherwise -> validator 3 failed
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "false" } }, "false, false, false");
        assertEquals(false, field1.isValid());
        assertEquals("field1 validator 3", field1.getMessage());

        // when1, when2 passed, otherwise -> validator 3 success
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "false" } }, "false, false, true");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_noOtherwise() throws Exception {
        // when1 -> validator 1 failed
        requestWithExtra(new Object[][] { { "o", "true" }, { "ot", "true" } }, null, "false, true, true");
        assertEquals(false, field2.isValid());
        assertEquals("field2 validator 1", field2.getMessage());

        // when1 -> validator 1 success
        requestWithExtra(new Object[][] { { "o", "true" }, { "ot", "true" } }, null, "true, false, false");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        // when1 passed, when2 -> validator 2 failed
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "true" } }, null, "false, false, true");
        assertEquals(false, field2.isValid());
        assertEquals("field2 validator 2", field2.getMessage());

        // when1 passed, when2 -> validator 2 success
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "true" } }, null, "false, true, false");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        // when1, when2 passed, no otherwise
        requestWithExtra(new Object[][] { { "o", "false" }, { "ot", "false" } }, null, "false, false, false");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }
}
