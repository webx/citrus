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

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.util.i18n.LocaleUtil;

public class StringByteLengthValidatorTests extends AbstractValidatorTests<StringByteLengthValidator> {
    @Override
    protected String getGroupName() {
        return "c";
    }

    @Override
    @Before
    public void init() throws Exception {
        super.init();
        LocaleUtil.setContext(Locale.CHINA, "UTF-8");
    }

    @After
    public void dispose() throws Exception {
        LocaleUtil.resetContext();
    }

    @Test
    public void init_wrongCharset() throws Exception {
        StringByteLengthValidator v = newValidator();
        v.setMessage("message");
        v.setCharset("notExist");
        assertInitError(v, exception(IllegalArgumentException.class, "Invalid charset: notExist"));
    }

    @Test
    public void init_charset() throws Exception {
        // default charset
        assertEquals("UTF-8", newValidator().getCharset());

        // specified charset
        StringByteLengthValidator v = newValidator();
        v.setCharset("GBK");
        assertEquals("GBK", v.getCharset());
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
        request("abc", "中g");

        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        request("abcd", "中国");

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
        request("abcdefghijk", "中华联邦台湾自治洲");

        assertEquals(false, field1.isValid());
        assertEquals("field1 length must be less than 10", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("field2 length must be at least 3 and be less than 10", field2.getMessage());
    }
}
