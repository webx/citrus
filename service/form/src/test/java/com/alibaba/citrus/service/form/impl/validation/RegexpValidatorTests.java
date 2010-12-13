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

import java.util.regex.PatternSyntaxException;

import org.junit.Test;

public class RegexpValidatorTests extends AbstractValidatorTests<RegexpValidator> {
    @Override
    protected String getGroupName() {
        return "i";
    }

    @Override
    protected void initFor_AbstractValidatorTests(RegexpValidator validator) {
        validator.setPattern("test");
    }

    @Test
    public void init_pattern() throws Exception {
        RegexpValidator v = newValidator();

        // empty
        v.setPattern(null);
        assertEquals(null, v.getPattern());

        v.setPattern("  ");
        assertEquals(null, v.getPattern());

        v.setMessage("message");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing regexp pattern"));
        }

        // empty negative pattern
        v = newValidator();
        v.setMessage("message");
        v.setPattern(" ! ");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing regexp pattern"));
        }

        // wrong pattern
        v = newValidator();
        v.setMessage("message");
        v.setPattern(" \\ ");

        try {
            v.afterPropertiesSet();
        } catch (PatternSyntaxException e) {
            assertThat(e, exception("\\"));
        }

        v = newValidator();
        v.setMessage("message");
        v.setPattern(" ! \\ ");

        try {
            v.afterPropertiesSet();
        } catch (PatternSyntaxException e) {
            assertThat(e, exception("\\"));
        }

        // right pattern
        v = newValidator();
        v.setMessage("message");
        v.setPattern(" abc ");
        v.afterPropertiesSet();
        assertEquals("abc", v.getPattern());

        v = newValidator();
        v.setMessage("message");
        v.setPattern(" ! abc ");
        v.afterPropertiesSet();
        assertEquals("! abc", v.getPattern());
    }

    @Test
    public void init_getNot() throws Exception {
        RegexpValidator v = newValidator();
        assertEquals(false, v.getNot());

        v.setPattern(" abc ");
        assertEquals(false, v.getNot());

        v.setPattern(" ! abc ");
        assertEquals(true, v.getNot());
    }

    @Test
    public void validate_partialMatch() throws Exception {
        request("def");
        assertEquals(false, field1.isValid());
        assertEquals("field1 should match abc", field1.getMessage());

        request("defabc");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        request("abcdef");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        request("123abcdef");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        request("abc");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_fullMatch() throws Exception {
        request(null, "def");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should match ^abc$", field2.getMessage());

        request(null, "defabc");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should match ^abc$", field2.getMessage());

        request(null, "abcdef");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should match ^abc$", field2.getMessage());

        request(null, "123abcdef");
        assertEquals(false, field2.isValid());
        assertEquals("field2 should match ^abc$", field2.getMessage());

        request(null, "abc");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    @Test
    public void validate_match_negative() throws Exception {
        request(null, null, "def");
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        request(null, null, "defabc");
        assertEquals(false, field3.isValid());
        assertEquals("field3 should match !abc", field3.getMessage());

        request(null, null, "abcdef");
        assertEquals(false, field3.isValid());
        assertEquals("field3 should match !abc", field3.getMessage());

        request(null, null, "123abcdef");
        assertEquals(false, field3.isValid());
        assertEquals("field3 should match !abc", field3.getMessage());

        request(null, null, "abc");
        assertEquals(false, field3.isValid());
        assertEquals("field3 should match !abc", field3.getMessage());
    }
}
