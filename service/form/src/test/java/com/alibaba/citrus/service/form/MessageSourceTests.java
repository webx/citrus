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
package com.alibaba.citrus.service.form;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

import com.alibaba.citrus.util.i18n.LocaleUtil;

public class MessageSourceTests extends AbstractFormServiceTests {
    private Form form;
    private Group group;
    private Field field1;
    private Field field2;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form-with-message-source.xml", false);
    }

    private void init(String formId) throws Exception {
        getFormService(formId);

        invokePost(null);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());
    }

    @After
    public void destroy() {
        LocaleUtil.resetContext();
    }

    @Test
    public void getMessage_fromMessageSource() throws Exception {
        init("form1");

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "" }, // group1.field1
                { "_fm.g._0.fi", "" }, // group1.field2
        };

        // zh_CN
        LocaleUtil.setContext(Locale.CHINA);

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValid());

        assertEquals(false, field1.isValid());
        assertEquals("缺少我的字段1", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("缺少field2", field2.getMessage());

        // zh_TW
        LocaleUtil.setContext(Locale.TAIWAN);

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValid());

        assertEquals(false, field1.isValid());
        assertEquals("缺失我的字段1", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("缺失field2", field2.getMessage());
    }

    @Test
    public void getMessage_noMessage_or_noId() throws Exception {
        init("form2");

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "" }, // group1.field1
                { "_fm.g._0.fi", "" }, // group1.field2
        };

        // zh_CN
        LocaleUtil.setContext(Locale.CHINA);

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValid());

        assertEquals(false, field1.isValid());
        assertEquals("myform - 缺少我的字段1", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("required field2", field2.getMessage()); // from message

        // zh_TW
        LocaleUtil.setContext(Locale.TAIWAN);

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValid());

        assertEquals(false, field1.isValid());
        assertEquals("myform - 缺失我的字段1", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("required field2", field2.getMessage()); // from message
    }

    @Test
    public void init_noMessage_noMessageSource() throws Exception {
        try {
            getFormService("form3");
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(IllegalArgumentException.class, "no message"));
        }
    }
}
