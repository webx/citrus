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
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FieldTests extends AbstractFormServiceTests {
    private Form form;
    private Group group;
    private Field field1;
    private Field field2;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form.xml", true);
    }

    @Before
    public void init() throws Exception {
        getFormService("form1");

        invokePost(null);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertEquals("default1", field1.getValue()); // specified default value
        assertEquals(null, field2.getValue()); // default is null
    }

    @Test
    public void getFieldConfig() {
        assertSame(group.getGroupConfig(), field1.getFieldConfig().getGroupConfig());
        assertSame(group.getGroupConfig(), field2.getFieldConfig().getGroupConfig());
    }

    @Test
    public void getGroup() {
        assertSame(group, field1.getGroup());
        assertSame(group, field2.getGroup());
    }

    @Test
    public void getKey() {
        assertEquals("_fm.g._0.f", field1.getKey());
        assertEquals("_fm.g._0.fi", field2.getKey());
    }

    @Test
    public void getAbsentKey() {
        assertEquals("_fm.g._0.f.absent", field1.getAbsentKey());
        assertEquals("_fm.g._0.fi.absent", field2.getAbsentKey());
    }

    @Test
    public void getAttachmentKey() {
        assertEquals("_fm.g._0.f.attach", field1.getAttachmentKey());
        assertEquals("_fm.g._0.fi.attach", field2.getAttachmentKey());
    }

    @Test
    public void getName() {
        assertEquals("field1", field1.getName());
        assertEquals("field2", field2.getName());
    }

    @Test
    public void getDisplayName() {
        assertEquals("我的字段1", field1.getDisplayName());
        assertEquals("field2", field2.getDisplayName()); // same as field name
    }

    @Test
    public void getDefaultValue() {
        assertEquals("default1", field1.getDefaultValue());
        assertEquals(null, field2.getDefaultValue());

        group = form.getGroup("group3");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals("default1", field1.getDefaultValue());
        assertEquals(null, field2.getDefaultValue());
    }

    @Test
    public void getDefaultValues() {
        assertArrayEquals(new String[] { "default1" }, field1.getDefaultValues());
        assertArrayEquals(new String[0], field2.getDefaultValues());

        group = form.getGroup("group3");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertArrayEquals(new String[] { "default1", "default2", "default3" }, field1.getDefaultValues());
        assertArrayEquals(new String[0], field2.getDefaultValues());
    }

    @Test
    public void getValue() throws Exception {
        assertEquals("default1", field1.getValue()); // with default value
        assertEquals(null, field2.getValue()); // no default value

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals("aaa", field1.getValue());
        assertEquals("bbb", field2.getValue());
    }

    @Test
    public void getValues() throws Exception {
        group = form.getGroup("group3");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals("default1", field1.getValue()); // with default value
        assertArrayEquals(new String[] { "default1", "default2", "default3" }, field1.getValues()); // with default value
        assertEquals(null, field2.getValue()); // no default value

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", new String[] { "aaa", "bbb" } }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals("aaa", field1.getValue());
        assertArrayEquals(new String[] { "aaa", "bbb" }, field1.getValues());
        assertEquals("bbb", field2.getValue());
    }

    @Test
    public void getStringValue() {
        assertEquals(null, field2.getValue());
        assertEquals("", field2.getStringValue());

        field2.setValue("hello");
        assertEquals("hello", field2.getValue());
        assertEquals("hello", field2.getStringValue());
    }

    @Test
    public void addValue_trimming() {
        // trimming = true
        field1.setValue(" hello ");
        assertEquals("hello", field1.getValue());

        field1.setValue("  ");
        assertEquals(null, field1.getValue());

        // trimming = false
        group = form.getGroup("group3");
        field1 = group.getField("field1");

        field1.setValue(" hello ");
        assertEquals(" hello ", field1.getValue());

        field1.setValue("  ");
        assertEquals("  ", field1.getValue());
    }

    @Test
    public void getAttachment() throws Exception {
        String code = "eNpb85aBtbiIwTg5P1cvMSczKTEpUS85s6SotFivOLWoLDM5VS8tvyh"
                + "Xzy0zNSclJLW4pFjFt9I%2FKSs1uSTiZmR4h%2Fan%2F0wMTJ4MjJk%2BDI"
                + "zFJQxCPlmJZYn6OYl56frBJUWZeenWFQUMDAzVJQwsJUDtAPRsJsw%3D";

        MyObject obj = new MyObject();
        obj.i = 123;
        obj.s = "test";

        // no attachment
        assertEquals(false, field1.hasAttachment());
        assertEquals(null, field1.getAttachment());
        assertEquals(null, field1.getAttachmentEncoded());

        // with attachment
        field1.setAttachment(obj);
        assertEquals(true, field1.hasAttachment());
        assertSame(obj, field1.getAttachment());
        assertEquals(code, field1.getAttachmentEncoded());

        // clear attachment
        field1.clearAttachment();
        assertEquals(false, field1.hasAttachment());
        assertEquals(null, field1.getAttachment());
        assertEquals(null, field1.getAttachmentEncoded());

        // 通过form携带attachment
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", new String[] { "aaa", "bbb" } }, // group1.field1
                { "_fm.g._0.f.attach", code }, // group1.field1.attach
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(true, field1.hasAttachment());
        assertNotSame(obj, field1.getAttachment());
        assertEquals(obj, field1.getAttachment());
        assertEquals(code, field1.getAttachmentEncoded());
    }

    @Test
    public void getAttachment_failureEncoding() throws Exception {
        String code = "!Failure: java.io.NotSerializableException: java.lang.Object";

        field1.setAttachment(new Object()); // not serializable
        assertEquals(code, field1.getAttachmentEncoded());
        assertNotNull(field1.getAttachment());

        // 解码Failure
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", new String[] { "aaa", "bbb" } }, // group1.field1
                { "_fm.g._0.f.attach", code }, // group1.field1.attach
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, field1.hasAttachment());
        assertEquals(null, field1.getAttachment());
        assertEquals(null, field1.getAttachmentEncoded());
    }

    @Test
    public void getAttachment_failureDecoding() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", new String[] { "aaa", "bbb" } }, // group1.field1
                { "_fm.g._0.f.attach", "wrong format" }, // group1.field1.attach
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, field1.hasAttachment());
        assertEquals(null, field1.getAttachment());
        assertEquals(null, field1.getAttachmentEncoded());
    }

    @Test
    public void isValid() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValid());
        assertEquals(true, field1.isValid());
        assertEquals(false, field2.isValid());
    }

    @Test
    public void getMessage_invalidField() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "" }, // group1.field1
                { "_fm.g._0.fi", "" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(false, group.isValid());

        assertEquals(false, field1.isValid());
        assertEquals("required 我的字段1", field1.getMessage());

        assertEquals(false, field2.isValid());
        assertEquals("required field2", field2.getMessage());
    }

    @Test
    public void getMessage_validField() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(true, group.isValid());

        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    @Test
    public void setMessage() throws Exception {
        initForSetMessage();

        // id not found
        try {
            field2.setMessage("notExistErrorId");
            fail();
        } catch (CustomErrorNotFoundException e) {
            assertThat(e, exception("Specified error ID \"notExistErrorId\" was not found "
                    + "in Field[group: group3._0, name: field2, values: [bbb], valid: true]"));
        }

        // setMessage
        field2.setMessage("err1");
        assertEquals(false, field2.isValid());
        assertEquals(false, group.isValid());
        assertEquals("hello, world", field2.getMessage());

        // setMessage twice
        field2.setMessage("err2");
        assertEquals(false, field2.isValid());
        assertEquals(false, group.isValid());
        assertEquals("hello, world", field2.getMessage()); // 错误信息不覆盖

        // setMessage with params
        initForSetMessage();
        Map<String, String> params = createHashMap();
        params.put("world", "世界");
        field2.setMessage("err3", params);
        assertEquals(false, field2.isValid());
        assertEquals(false, group.isValid());
        assertEquals("hello, 世界", field2.getMessage());

        // param not found
        initForSetMessage();
        field2.setMessage("err3");
        assertEquals(false, field2.isValid());
        assertEquals(false, group.isValid());
        assertEquals("hello, ", field2.getMessage());

        // no message (requiresMessage=false)
        initForSetMessage();
        try {
            field2.setMessage("err4");
            fail();
        } catch (CustomErrorNotFoundException e) {
            assertThat(e, exception("No message specified for error ID \"err4\" "
                    + "in Field[group: group3._0, name: field2, values: [bbb], valid: false]"));
        }
    }

    private void initForSetMessage() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gro._0.f", "aaa" }, // group3.field1
                { "_fm.gro._0.fi", "bbb" }, // group3.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group3");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(true, group.isValid());
        assertEquals(true, field1.isValid());
        assertEquals(true, field2.isValid());
    }

    @Test
    public void validate_withAbsentKey() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f.absent", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(true, group.isValid());
        assertEquals(true, field1.isValid());
        assertEquals(true, field2.isValid());
        assertEquals("aaa", field1.getValue());
    }

    @Test
    public void toString_() {
        assertEquals("Field[group: group1._0, name: field1, values: [default1], valid: true]", field1.toString());
    }

    public static class MyObject implements Serializable {
        private static final long serialVersionUID = 6402246577765479167L;
        private int i;
        private String s;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            MyObject other = (MyObject) obj;

            if (i != other.i) {
                return false;
            }

            if (s == null) {
                if (other.s != null) {
                    return false;
                }
            } else if (!s.equals(other.s)) {
                return false;
            }

            return true;
        }
    }
}
