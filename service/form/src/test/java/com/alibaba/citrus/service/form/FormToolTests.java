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

import java.util.Iterator;
import java.util.Set;

import org.apache.ecs.xhtml.input;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.form.support.FormTool;
import com.alibaba.citrus.service.form.support.FormTool.FieldHelper;
import com.alibaba.citrus.service.form.support.FormTool.GroupHelper;
import com.alibaba.citrus.service.form.support.FormTool.GroupInstanceHelper;
import com.alibaba.citrus.service.pull.PullService;

public class FormToolTests extends AbstractFormServiceTests {
    private FormTool tool;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form.xml", true);
    }

    @Before
    public void init() throws Exception {
        getFormService("form1");
        PullService pullService = (PullService) factory.getBean("pullService");
        tool = (FormTool) pullService.getTools().get("form");
        assertNotNull(tool);

        newForm();
    }

    private void newForm() throws Exception {
        invokePost(null);
    }

    private void submitForm() throws Exception {
        invokePost(new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aa" }, // group1.default.field1
                { "_fm.g.1.f", "bb" }, // group1.1.field1
                { "_fm.gr._0.f", "cc" }, // group2.default.field1
                { "_fm.gr._0.fi", "dd" }, // group2.default.field2
        });
    }

    @Test
    public void tool_toString() {
        assertEquals("FormTool[no FormService]", new FormTool().toString());
        assertEquals("form1:FormService {\n" //
                + "  FormConfig[groups: 3]\n" //
                + "}", tool.toString());
    }

    @Test
    public void form_getGroup() throws Exception {
        // new form
        GroupHelper group = tool.get("group1");
        assertEquals("Group[group1]", group.toString());

        GroupInstanceHelper group_0 = group.getDefaultInstance();
        GroupInstanceHelper group_1 = group.getInstance("1");
        GroupInstanceHelper group_2 = group.getInstance("2", false);

        assertNull(group_2);
        group_2 = group.getInstance("2", true);

        assertEquals("Group[name: group1._0, fields: 2, validated: false, valid: true]", group_0.toString());
        assertEquals("Group[name: group1.1, fields: 2, validated: false, valid: true]", group_1.toString());
        assertEquals("Group[name: group1.2, fields: 2, validated: false, valid: true]", group_2.toString());

        // submit form
        submitForm();

        group = tool.get("group1");
        assertEquals("Group[group1]", group.toString());

        group_0 = group.getDefaultInstance();
        group_1 = group.getInstance("1", false);
        assertNotNull(group_1);
        group_1 = group.getInstance("1");

        assertEquals("Group[name: group1._0, fields: 2, validated: true, valid: false]", group_0.toString());
        assertEquals("Group[name: group1.1, fields: 2, validated: true, valid: false]", group_1.toString());

        // not exist group
        group = tool.get("notExist");
        assertEquals("Group[notExist]", group.toString());

        assertNull(group.getDefaultInstance());
        assertNull(group.getInstance("1"));
        assertNull(group.getInstance("2", false));
    }

    @Test
    public void form_getGroups() throws Exception {
        submitForm();

        // getGroups
        Iterator<GroupInstanceHelper> i = tool.getGroups();
        Set<String> results = createHashSet( //
                "Group[name: group1._0, fields: 2, validated: true, valid: false]", //
                "Group[name: group1.1, fields: 2, validated: true, valid: false]", //
                "Group[name: group2._0, fields: 2, validated: true, valid: true]");

        assertNotNull(results.remove(i.next().toString()));
        assertNotNull(results.remove(i.next().toString()));
        assertNotNull(results.remove(i.next().toString()));
        assertFalse(i.hasNext());

        // getGroups(name)
        i = tool.getGroups("group1");
        results = createHashSet( //
                "Group[name: group1._0, fields: 2, validated: true, valid: false]", //
                "Group[name: group1.1, fields: 2, validated: true, valid: false]");

        assertNotNull(results.remove(i.next().toString()));
        assertNotNull(results.remove(i.next().toString()));
        assertFalse(i.hasNext());
    }

    @Test
    public void form_isValid() throws Exception {
        // 初始状态：true
        assertEquals(true, tool.isValid());

        // 验证失败：false
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
        };

        invokePost(args);
        assertEquals(false, tool.isValid());

        // 验证成功：true
        args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        invokePost(args);
        assertEquals(true, tool.isValid());
    }

    @Test
    public void group_getField() throws Exception {
        // new form
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        assertEquals("Field[group: group1._0, name: field1, values: [default1], valid: true]", group.get("field1")
                .toString());
        assertEquals("Field[group: group1._0, name: field2, values: [], valid: true]", group.get("field2").toString());
        assertEquals(null, group.get("notExist"));

        // submit form
        submitForm();

        group = tool.get("group1").getDefaultInstance();
        assertEquals("Field[group: group1._0, name: field1, values: [aa], valid: true]", group.get("field1").toString());
        assertEquals("Field[group: group1._0, name: field2, values: [], valid: false]", group.get("field2").toString());

        group = tool.get("group1").getInstance("1");
        assertEquals("Field[group: group1.1, name: field1, values: [bb], valid: true]", group.get("field1").toString());
        assertEquals("Field[group: group1.1, name: field2, values: [], valid: false]", group.get("field2").toString());

        group = tool.get("group2").getDefaultInstance();
        assertEquals("Field[group: group2._0, name: field1, values: [cc], valid: true]", group.get("field1").toString());
        assertEquals("Field[group: group2._0, name: field2, values: [dd], valid: true]", group.get("field2").toString());
    }

    @Test
    public void group_getFields() throws Exception {
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        Iterator<FieldHelper> i = group.getFields();
        Set<String> results = createHashSet("Field[group: group1._0, name: field1, values: [default1], valid: true]", //
                "Field[group: group1._0, name: field2, values: [], valid: true]");

        assertNotNull(results.remove(i.next().toString()));
        assertNotNull(results.remove(i.next().toString()));
        assertFalse(i.hasNext());
    }

    @Test
    public void group_isValid() throws Exception {
        // new form
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        assertEquals(true, group.isValid());

        // submit form
        submitForm();

        group = tool.get("group1").getDefaultInstance();
        assertEquals(false, group.isValid());

        group = tool.get("group2").getDefaultInstance();
        assertEquals(true, group.isValid());

        group = tool.get("group2").getInstance("2");
        assertEquals(true, group.isValid());
    }

    @Test
    public void group_isValidated() throws Exception {
        // new form
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        assertEquals(false, group.isValidated());

        // submit form
        submitForm();

        group = tool.get("group1").getDefaultInstance();
        assertEquals(true, group.isValidated());

        group = tool.get("group2").getDefaultInstance();
        assertEquals(true, group.isValidated());

        group = tool.get("group2").getInstance("2");
        assertEquals(false, group.isValidated());
    }

    @Test
    public void group_mapTo() throws Exception {
        MyClass obj = new MyClass();
        obj.setProperty1("aaa");
        obj.setField2(456);

        // new form
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        group.mapTo(obj);

        assertEquals("aaa", group.get("field1").getValue());
        assertEquals("456", group.get("field2").getValue());

        // submit form
        submitForm();

        group = tool.get("group1").getDefaultInstance();
        group.mapTo(obj); // 对validated group无效果

        assertEquals("aa", group.get("field1").getValue());
        assertEquals("", group.get("field2").getValue());
    }

    @Test
    public void field_functions() throws Exception {
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        FieldHelper field = group.get("field1");
        setFieldValues(field, new String[] { "<&\">", "hello" });

        assertEquals("我的字段1", field.getDisplayName());
        assertEquals("_fm.g._0.f", field.getKey());
        assertEquals("_fm.g._0.f.~html", field.getHtmlKey());
        assertEquals("_fm.g._0.f.absent", field.getAbsentKey());
        assertEquals("_fm.g._0.f.attach", field.getAttachmentKey());
        assertEquals("<&\">", field.getValue());
        assertEquals("&lt;&amp;&quot;&gt;", field.getEscapedValue());
        assertArrayEquals(new String[] { "<&\">", "hello" }, field.getValues());
        assertArrayEquals(new String[] { "&lt;&amp;&quot;&gt;", "hello" }, field.getEscapedValues());
    }

    @Test
    public void field_absent() throws Exception {
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        FieldHelper field = group.get("field1");

        // absent hidden field
        input hiddenTag = field.getAbsentHiddenField("defaultValue");
        assertThat(hiddenTag.toString(),
                containsAll("<input", "name='_fm.g._0.f.absent'", "type='hidden'", "value='defaultValue'", "/>"));

    }

    @Test
    public void field_attachment() throws Exception {
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        FieldHelper field = group.get("field1");

        // no attachment
        assertEquals(null, field.getAttachment());
        assertEquals("", field.getAttachmentEncoded());
        assertEquals(false, field.hasAttachment());

        input attachmentTag = field.getAttachmentHiddenField();
        assertThat(attachmentTag.toString(),
                containsAll("<input", "name='_fm.g._0.f.attach'", "type='hidden'", "value=''", "/>"));

        // with attachment
        field.setAttachment("attachedObject");

        assertEquals("attachedObject", field.getAttachment());
        assertEquals("eNpb85aBtYSBL7GkJDE5IzXFPykrNbkEAFOhB7Y%3D", field.getAttachmentEncoded());
        assertEquals(true, field.hasAttachment());

        attachmentTag = field.getAttachmentHiddenField();
        assertThat(
                attachmentTag.toString(),
                containsAll("<input", "name='_fm.g._0.f.attach'", "type='hidden'",
                        "value='eNpb85aBtYSBL7GkJDE5IzXFPykrNbkEAFOhB7Y%3D'", "/>"));

        // clear attachment
        field.clearAttachment();
        assertEquals(null, field.getAttachment());
        assertEquals("", field.getAttachmentEncoded());
        assertEquals(false, field.hasAttachment());
    }

    @Test
    public void field_isValid() throws Exception {
        // new form
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        FieldHelper field = group.get("field1");

        assertTrue(field.isValid());

        // submit form
        submitForm();

        group = tool.get("group1").getDefaultInstance();
        field = group.get("field2");

        assertFalse(field.isValid());
    }

    @Test
    public void field_getMessage() throws Exception {
        // new form
        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        FieldHelper field = group.get("field1");

        assertEquals(null, field.getMessage());

        // submit form
        submitForm();

        group = tool.get("group1").getDefaultInstance();
        field = group.get("field2");

        assertEquals("required field2", field.getMessage());
    }

    private void setFieldValues(FieldHelper fieldHelper, String[] value) throws Exception {
        Field field = (Field) getAccessibleField(FieldHelper.class, "field").get(fieldHelper);
        field.setValues(value);
    }

    public static class MyClass {
        private String property1;
        private int field2;

        public String getProperty1() {
            return property1;
        }

        public void setProperty1(String property1) {
            this.property1 = property1;
        }

        public int getField2() {
            return field2;
        }

        public void setField2(int field2) {
            this.field2 = field2;
        }
    }
}
