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

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.TypeConverter;

import com.alibaba.citrus.service.form.configuration.FormConfig;

/**
 * 测试form类。
 * 
 * @author Michael Zhou
 */
public class FormTests extends AbstractFormServiceTests {
    private Form form;
    private Group group;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form.xml", true);
    }

    @Before
    public void init() {
        getFormService("form1");
    }

    @Test
    public void getFormConfig() throws Exception {
        invokeGet(null);

        form = formService.getForm();
        FormConfig fc = form.getFormConfig();

        assertNotNull(fc);
        assertSame(formService, fc.getFormService());
    }

    @Test
    public void getTypeConverter() throws Exception {
        invokeGet(null);

        form = formService.getForm();
        TypeConverter tc = form.getTypeConverter();

        assertNotNull(tc);
        assertSame(tc, form.getTypeConverter()); // 两次返回相同的

        assertEquals(123, tc.convertIfNecessary("any", Integer.class));
    }

    @Test
    public void isForcePostOnly() throws Exception {
        invokeGet(null);

        form = formService.getForm();
        assertEquals(false, form.isForcePostOnly());

        invokeGet(null);

        form = formService.getForm(true);
        assertEquals(true, form.isForcePostOnly());
    }

    @Test
    public void isValid() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
                { "_fm.gr._0.f", "aaa" }, // group2.field1
                { "_fm.gr._0.fi", "" }, // group2.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(false, form.isValid());
        assertEquals(2, form.getGroups().size());

        group = form.getGroup("group1");
        assertEquals(true, group.isValid());

        group = form.getGroup("group2");
        assertEquals(false, group.isValid());
    }

    @Test
    public void init_noData() throws Exception {
        invokePost(null);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(0, form.getGroups().size());

        // create new group instance
        group = form.getGroup("group1");
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());
    }

    @Test
    public void init_noFormData() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gr._0", "aaa" }, // not a field
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(0, form.getGroups().size());
    }

    @Test
    public void init_undefinedGroup() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.u._0.f", "aaa" }, // ??.field1
                { "_fm.u._0.fi", "bbb" }, // ??.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(0, form.getGroups().size());
    }

    @Test
    public void init_withAbsentKey() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gr._0.f.absent", "aaa" }, // group2.field1 if absent
                { "_fm.gr._0.fi", "bbb" }, // group2.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(1, form.getGroups().size());

        group = form.getGroup("group2");

        assertEquals("aaa", group.getField("field1").getValue());
        assertEquals("bbb", group.getField("field2").getValue());
    }

    @Test
    public void init_withAbsentKey_override() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gr._0.f", "aaaaa" }, // group2.field1
                { "_fm.gr._0.f.absent", "aaa" }, // group2.field1 if absent
                { "_fm.gr._0.fi", "bbb" }, // group2.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(1, form.getGroups().size());

        group = form.getGroup("group2");

        assertEquals("aaaaa", group.getField("field1").getValue());
        assertEquals("bbb", group.getField("field2").getValue());
    }

    @Test
    public void init_postOnly() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gr._0.f", "aaa" }, // group2.field1
                { "_fm.gr._0.fi", "bbb" }, // group2.field2
        };

        invokeGet(args);
        form = formService.getForm();

        assertEquals(false, form.isValid());
        assertEquals(0, form.getGroups().size());
    }

    @Test
    public void init_andValidate() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
                { "_fm.gr._0.f", "aaa" }, // group2.field1
                { "_fm.gr._0.fi", "" }, // group2.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(false, form.isValid());
        assertEquals(2, form.getGroups().size());

        group = form.getGroup("group1");
        assertEquals(true, group.isValidated());
        assertEquals(true, group.isValid());

        group = form.getGroup("group2");
        assertEquals(true, group.isValidated());
        assertEquals(false, group.isValid());

        // re-init
        form.init();
        assertEquals(true, form.isValid());
        assertEquals(0, form.getGroups().size());

        group = form.getGroup("group1");
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        group = form.getGroup("group2");
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        // set data manually
        group = form.getGroup("group1");
        group.getField("field1").setValue("aaa");
        group.getField("field2").setValue("bbb");
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        // re-validate
        form.validate();
        assertEquals(false, form.isValid());
        assertEquals(2, form.getGroups().size());

        group = form.getGroup("group1");
        assertEquals(true, group.isValidated());
        assertEquals(true, group.isValid());

        group = form.getGroup("group2");
        assertEquals(true, group.isValidated());
        assertEquals(false, group.isValid());
    }

    @Test
    public void getKey() throws Exception {
        invokePost(null);
        form = formService.getForm();

        assertEquals("_fm", form.getKey());
    }

    @Test
    public void getGroups() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.default.field1
                { "_fm.g._0.fi", "bbb" }, // group1.default.field2
                { "_fm.gr.111.f", "ccc" }, // group2.111.field1
                { "_fm.gr.111.fi", "ddd" }, // group2.111.field2
                { "_fm.gr.222.f", "eee" }, // group2.222.field1
                { "_fm.gr.222.fi", "" }, // group2.222.field2
        };

        invokePost(args);
        form = formService.getForm();
        assertEquals(false, form.isValid());

        // getGroups
        Collection<Group> groups = form.getGroups();
        assertEquals(3, groups.size());

        Iterator<Group> i = groups.iterator();
        assertEquals("_fm.g._0", i.next().getKey());
        assertEquals("_fm.gr.111", i.next().getKey());
        assertEquals("_fm.gr.222", i.next().getKey());

        // getGroups("group1")
        groups = form.getGroups("group1");
        assertEquals(1, groups.size());

        i = groups.iterator();
        assertEquals("_fm.g._0", i.next().getKey());

        // getGroups("group2")
        groups = form.getGroups("group2");
        assertEquals(2, groups.size());

        i = groups.iterator();
        assertEquals("_fm.gr.111", i.next().getKey());
        assertEquals("_fm.gr.222", i.next().getKey());

        // getGroup: undefined
        group = form.getGroup("undefined");
        assertEquals(null, group);

        // getGroup: group1, defaultInstance
        group = form.getGroup("group1");
        assertEquals(true, group.isValidated());
        assertEquals(true, group.isValid());

        // getGroup: group2, defaultInstance - create new
        group = form.getGroup("group2");
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        assertSame(group, form.getGroup("group2", null)); // null means default instance
        assertSame(group, form.getGroup("group2", "  ")); // blank trimmed to null

        // getGroup: group2, 111
        group = form.getGroup("group2", "111");
        assertEquals(true, group.isValidated());
        assertEquals(true, group.isValid());

        // getGroup: group2, 222, trimming
        group = form.getGroup("group2", " 222 ");
        assertEquals(true, group.isValidated());
        assertEquals(false, group.isValid());

        // getGroup: group2, 333 - create new
        group = form.getGroup("group2", "333");
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());

        // getGroup: group2, 444 - don't create new
        group = form.getGroup("group2", "444", false);
        assertEquals(null, group);

        // getGroup: group2, 444 - create new
        group = form.getGroup("group2", "444", true);
        assertEquals(false, group.isValidated());
        assertEquals(true, group.isValid());
    }

    @Test
    public void toString_() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.default.field1
                { "_fm.g._0.fi", "bbb" }, // group1.default.field2
                { "_fm.gr.111.f", "ccc" }, // group2.111.field1
                { "_fm.gr.111.fi", "ddd" }, // group2.111.field2
                { "_fm.gr.222.f", "eee" }, // group2.222.field1
                { "_fm.gr.222.fi", "" }, // group2.222.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals("Form[groups: 3, group instances: 3, valid: false]", form.toString());
    }
}
