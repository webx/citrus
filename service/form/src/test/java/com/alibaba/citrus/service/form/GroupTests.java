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

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.form.configuration.GroupConfig;

@RunWith(Parameterized.class)
public class GroupTests extends AbstractFormServiceTests {
    private final ApplicationContext factory;
    private final boolean withParserRequestContext;
    private Form form;
    private Group group;
    private Field field1;
    private Field field2;

    public GroupTests(ApplicationContext factory, boolean withParserRequestContext) {
        this.factory = factory;
        this.withParserRequestContext = withParserRequestContext;
    }

    @Parameters
    public static Collection<Object[]> data() {
        // 分别测试两组：1. 有parser request context  2. 无parser request context，纯HttpServletRequest
        ApplicationContext withParserRequestContextAndUpload = createContext("services-form.xml", true);
        ApplicationContext noParserRequestContextOrUpload = createContext("services-form.xml", false);

        return Arrays.asList(new Object[][] { { withParserRequestContextAndUpload, true },
                { noParserRequestContextOrUpload, false } });
    }

    @Before
    public void init() throws Exception {
        getFormService("form1", factory);

        invokePost(factory, null);
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
    public void getGroupConfig() throws Exception {
        GroupConfig gc = group.getGroupConfig();
        assertNotNull(gc);
        assertSame(form.getFormConfig(), gc.getFormConfig());
    }

    @Test
    public void getForm() throws Exception {
        assertSame(form, group.getForm());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("group1", group.getName());
    }

    @Test
    public void getKey() throws Exception {
        assertEquals("_fm.g._0", group.getKey());

        group = form.getGroup("group1", "111");
        assertEquals("_fm.g.111", group.getKey());
    }

    @Test
    public void getInstanceKey() throws Exception {
        assertEquals("_0", group.getInstanceKey());

        group = form.getGroup("group1", "111");
        assertEquals("111", group.getInstanceKey());
    }

    /**
     * 确保validator可以读到后面的field值。
     */
    @Test
    public void validate_multiFields() throws Exception {
        getFormService("form2", factory);

        // field1 failed to validate
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        assertEquals(false, form.isValid());

        group = form.getGroup("group1");
        assertEquals(false, group.isValid());

        assertEquals(false, group.getField("field1").isValid());
        assertEquals(true, group.getField("field2").isValid());

        // succeeded in validating
        args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "value1" }, // group1.field1
                { "_fm.g._0.fi", "value2" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        assertEquals(true, form.isValid());

        group = form.getGroup("group1");
        assertEquals(true, group.isValid());

        assertEquals(true, group.getField("field1").isValid());
        assertEquals(true, group.getField("field2").isValid());
    }

    @Test
    public void isValidated() throws Exception {
        // no data
        assertEquals(false, group.isValidated());

        // with data
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");
        assertEquals(true, group.isValidated());

        // re-init
        form.init();
        group = form.getGroup("group1");
        assertEquals(false, group.isValidated());
    }

    @Test
    public void getFields() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
        };

        invokePost(factory, args);
        form = formService.getForm();
        assertEquals(false, form.isValid());

        group = form.getGroup("group1");
        assertEquals(false, group.isValid());

        // getFields
        Collection<Field> fields = group.getFields();
        Iterator<Field> i = fields.iterator();

        assertEquals(2, fields.size());
        assertEquals(true, i.next().isValid());
        assertEquals(false, i.next().isValid());

        // getField
        Field field1 = group.getField("field1");
        Field field2 = group.getField("field2");

        assertEquals(true, field1.isValid());
        assertEquals(false, field2.isValid());
    }

    @Test
    public void mapTo_null() throws Exception {
        group.mapTo(null);

        // 状态不变
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertEquals("default1", field1.getValue());
        assertEquals(null, field2.getValue());
    }

    /**
     * isValidated == true，不允许执行mapTo。
     */
    @Test
    public void mapTo_isValidated() throws Exception {
        group.validate();
        assertEquals(true, group.isValidated());
        assertEquals(false, group.isValid());

        group.mapTo(new MyIntObject(234));

        // 原状态不受影响
        assertEquals(true, group.isValidated());
        assertEquals(false, group.isValid());

        assertEquals(true, field1.isValid());
        assertEquals("default1", field1.getValue()); // 不是234

        assertEquals(false, field2.isValid());
        assertEquals(null, field2.getValue());
    }

    @Test
    public void mapTo_simpleValue() throws Exception {
        group.mapTo(new MyIntObject(234));

        // field1(property1)存在，field2不存在
        // mapTo不影响isValidated和isValid状态
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertEquals(true, field1.isValid());
        assertEquals("234", field1.getValue());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getValue());
    }

    @Test
    public void mapTo_stringArray() throws Exception {
        group.mapTo(new MyStringArrayObject("aaa", "bbb"));

        // field1(property1)不存在，field2存在
        // mapTo不影响isValidated和isValid状态
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertEquals(true, field1.isValid());
        assertEquals("default1", field1.getValue());

        assertEquals(true, field2.isValid());
        assertEquals("aaa", field2.getValue());
        assertArrayEquals(new Object[] { "aaa", "bbb" }, field2.getValues());
    }

    @Test
    public void mapTo_customType() throws Exception {
        group.mapTo(new MyCustomObject(new SimpleValue("aaa")));

        // field1(property1)不存在，field2存在
        // mapTo不影响isValidated和isValid状态
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertEquals(true, field1.isValid());
        assertEquals("default1", field1.getValue());

        assertEquals(true, field2.isValid());
        assertEquals("aaa", field2.getValue());
    }

    @Test
    public void mapTo_date() throws Exception {
        group.mapTo(new MyDateObject("1989-06-04"));

        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertEquals(true, field1.isValid());
        assertEquals("1989-06-04", field1.getValue());

        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getValue());
    }

    @Test
    public void setProperties_null() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        group.setProperties(null);

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void setProperties_unSet() throws Exception {
        invokePost(factory, null);
        form = formService.getForm();
        group = form.getGroup("group1");
        group.getField("field1").setValue("234");

        assertEquals(true, group.isValid());
        assertEquals(false, group.isValidated());

        MyIntObject obj = new MyIntObject();
        group.setProperties(obj);

        assertEquals(true, group.isValid());
        assertEquals(false, group.isValidated());
        assertEquals(-1, obj.getProperty1()); // not set
    }

    @Test
    public void setProperties_invalid() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(false, group.isValid());
        assertEquals(true, group.isValidated());

        MyIntObject obj = new MyIntObject();

        try {
            group.setProperties(obj);
            fail();
        } catch (InvalidGroupStateException e) {
            assertThat(e, exception("Attempted to call setProperties from an invalid input"));
        }

        assertEquals(false, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void setProperties_simpleValue() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "234" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        // field1(property1)存在，field2不存在
        MyIntObject obj = new MyIntObject();
        group.setProperties(obj);

        assertEquals(123, obj.getProperty1()); // 由于custom editor的存在，值变成123

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void setProperties_quietMode() throws Exception {
        getFormService("form3", factory);

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "illegal" }, // group1.field1
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        // 转换失败，但是quiet=true，使用默认值。
        MyIntObject obj = new MyIntObject();
        group.setProperties(obj);

        assertEquals(0, obj.getProperty1());
    }

    @Test
    public void setProperties_noisyMode() throws Exception {
        getFormService("form4", factory);

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "illegal" }, // group1.field1
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        // 转换失败，但是quiet=true，使用默认值。
        try {
            group.setProperties(new MyIntObject());
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal"));
        }
    }

    @Test
    public void setProperties_stringArray() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", new String[] { "bbb", "ccc", "ddd" } }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        // field1(property1)不存在，field2存在
        MyStringArrayObject obj = new MyStringArrayObject();
        group.setProperties(obj);

        assertArrayEquals(new String[] { "bbb", "ccc", "ddd" }, obj.getField2());

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void setProperties_customType() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        // field1(property1)不存在，field2存在
        MyCustomObject obj = new MyCustomObject();
        group.setProperties(obj);

        assertEquals("bbb", obj.getField2().value);

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void setProperties_noValues() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gro._0.f", "aaa" }, // group3.field1
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group3");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        // field1(property1)不存在，field2存在
        MyCustomObject obj = new MyCustomObject();
        group.setProperties(obj);

        assertEquals(null, obj.getField2());

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void setProperties_fileItem() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", new File(srcdir, "data/file1.txt") }, // group1.field2
        };

        invokePostMime(factory, args);

        if (!withParserRequestContext) {
            assertTrue(newRequest.getParameterMap().isEmpty());
            return;
        }

        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(true, group.isValid());
        assertThat(field2.getValue(), instanceOf(FileItem.class));

        MyFileItemObject fio = new MyFileItemObject();
        group.setProperties(fio);

        assertEquals(1, fio.getField2().length);
        assertThat(fio.getField2()[0], instanceOf(FileItem.class));
    }

    @Test
    public void setProperties_fileItems() throws Exception {
        Object[][] args = new Object[][] {
                { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", new File[] { new File(srcdir, "data/file1.txt"), new File(srcdir, "data/file2.txt") } }, // group1.field2
        };

        invokePostMime(factory, args);

        if (!withParserRequestContext) {
            assertTrue(newRequest.getParameterMap().isEmpty());
            return;
        }

        form = formService.getForm();
        group = form.getGroup("group1");
        field1 = group.getField("field1");
        field2 = group.getField("field2");

        assertEquals(true, group.isValid());
        assertThat(field2.getValue(), instanceOf(FileItem.class));

        // fileItems
        MyFileItemObject fio = new MyFileItemObject();
        group.setProperties(fio);

        assertEquals(2, fio.getField2().length);
        assertThat(fio.getField2()[0], instanceOf(FileItem.class));
        assertThat(fio.getField2()[1], instanceOf(FileItem.class));
    }

    @Test
    public void setProperties_date() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "1989-6-4" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(factory, args);
        form = formService.getForm();
        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());

        MyDateObject obj = new MyDateObject();
        group.setProperties(obj);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals("1989-06-04", sdf.format(obj.getProperty1()));

        assertEquals(true, group.isValid());
        assertEquals(true, group.isValidated());
    }

    @Test
    public void toString_() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.default.field1
                { "_fm.g.11.f", "aaa" }, // group1.11.field1
        };

        invokePost(factory, args);
        form = formService.getForm();

        group = form.getGroup("group1");
        assertEquals("Group[name: group1._0, fields: 2, validated: true, valid: false]", group.toString());

        group = form.getGroup("group1", "11");
        assertEquals("Group[name: group1.11, fields: 2, validated: true, valid: false]", group.toString());
    }

    public static class MyIntObject {
        private int property1;

        public MyIntObject() {
            this(-1);
        }

        public MyIntObject(int property1) {
            this.property1 = property1;
        }

        public int getProperty1() {
            return property1;
        }

        public void setProperty1(int property1) {
            this.property1 = property1;
        }
    }

    public static class MyStringArrayObject {
        private String[] field2;

        public MyStringArrayObject(String... field2) {
            this.field2 = field2;
        }

        public String[] getField2() {
            return field2;
        }

        public void setField2(String[] field2) {
            this.field2 = field2;
        }
    }

    public static class MyCustomObject {
        private SimpleValue field2;

        public MyCustomObject() {
        }

        public MyCustomObject(SimpleValue field2) {
            this.field2 = field2;
        }

        public SimpleValue getField2() {
            return field2;
        }

        public void setField2(SimpleValue field2) {
            this.field2 = field2;
        }
    }

    public static class MyFileItemObject {
        private FileItem[] field2;

        public FileItem[] getField2() {
            return field2;
        }

        public void setField2(FileItem[] field2) {
            this.field2 = field2;
        }
    }

    public static class MyDateObject {
        private Date property1;

        public MyDateObject() {
        }

        public MyDateObject(String property1) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                this.property1 = sdf.parse(property1);
            } catch (ParseException e) {
                fail(e.getMessage());
            }
        }

        public Date getProperty1() {
            return property1;
        }

        public void setProperty1(Date property1) {
            this.property1 = property1;
        }
    }

    public static class SimpleValue {
        private String value;

        public SimpleValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class SimpleValueEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(new SimpleValue(text));
        }
    }
}
