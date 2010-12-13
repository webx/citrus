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
package com.alibaba.citrus.service.form.configuration;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.form.configuration.GroupConfig.Import;
import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl.ImportImpl;

public class GroupConfigTests {
    private FormConfigImpl formConfig;
    private GroupConfigImpl group;
    private GroupConfigImpl group1;
    private GroupConfigImpl group2;
    private GroupConfigImpl group3;
    private GroupConfigImpl group4;
    private FieldConfigImpl field1;
    private FieldConfigImpl field2;
    private FieldConfigImpl field3;
    private FieldConfigImpl field4;

    @Before
    public void init() throws Exception {
        formConfig = new FormConfigImpl();

        field1 = new FieldConfigImpl();
        field1.setName("fiELd1");

        field2 = new FieldConfigImpl();
        field2.setName("field2");

        field3 = new FieldConfigImpl();
        field3.setName("FIELD3");

        field4 = new FieldConfigImpl();
        field4.setName("field4");

        group = new GroupConfigImpl();
        group.setName("group1");

        group1 = new GroupConfigImpl();
        group1.setName("group1");
        group1.setFieldConfigImplList(createArrayList(field1));

        group2 = new GroupConfigImpl();
        group2.setName("group2");
        group2.setFieldConfigImplList(createArrayList(field2));

        group3 = new GroupConfigImpl();
        group3.setName("group3");
        group3.setFieldConfigImplList(createArrayList(field3));

        group4 = new GroupConfigImpl();
        group4.setName("group4");
        group4.setFieldConfigImplList(createArrayList(field4));
    }

    private void initForm(List<FieldConfigImpl> fieldList) throws Exception {
        group.setFieldConfigImplList(fieldList);
        group.afterPropertiesSet();

        formConfig.setGroupConfigImplList(createArrayList(group));
        formConfig.afterPropertiesSet();
    }

    @Test
    public void getFormConfig() throws Exception {
        // no form
        assertNull(group.getFormConfig());

        // with form
        initForm(createArrayList(field1));
        assertSame(formConfig, group.getFormConfig());
    }

    @Test
    public void getName() {
        GroupConfigImpl groupConfig = new GroupConfigImpl();

        // no name
        assertNull(groupConfig.getName());

        // empty name
        groupConfig.setName("");
        assertNull(groupConfig.getName());

        groupConfig.setName("  ");
        assertNull(groupConfig.getName());

        // normal name
        groupConfig.setName("  hello ");
        assertEquals("hello", groupConfig.getName());
    }

    @Test
    public void getKey() throws Exception {
        // no key
        assertNull(group.getKey());

        // empty key
        group.setKey("");
        assertNull(group.getKey());

        group.setKey("  ");
        assertNull(group.getKey());

        // normal key
        group.setKey("  hello ");
        assertEquals("hello", group.getKey());

        // via form.init
        initForm(createArrayList(field1));
        assertEquals("g", group.getKey());
    }

    @Test
    public void getParentGroup() {
        // no key
        assertNull(group.getParentGroup());

        // empty key
        group.setParentGroup("");
        assertNull(group.getParentGroup());

        group.setParentGroup("  ");
        assertNull(group.getParentGroup());

        // normal key
        group.setParentGroup("  hello ");
        assertEquals("hello", group.getParentGroup());
    }

    @Test
    public void isTrimmingByDefault() {
        // default
        assertTrue(group.isTrimmingByDefault());

        // set value
        group.setTrimmingByDefault(false);
        assertFalse(group.isTrimmingByDefault());
    }

    @Test
    public void isPostOnly() throws Exception {
        // default, no form config
        assertTrue(group.isPostOnly());

        // default, with form config
        initForm(createArrayList(field1));

        formConfig.setPostOnlyByDefault(false);
        assertFalse(group.isPostOnly());

        formConfig.setPostOnlyByDefault(true);
        assertTrue(group.isPostOnly());

        // set value
        group.setPostOnly(false);
        assertFalse(group.isPostOnly());
    }

    @Test
    public void getFieldConfigList() throws Exception {
        // default, empty
        assertTrue(group.getFieldConfigList().isEmpty());

        // field name is null
        try {
            initForm(createArrayList(new FieldConfigImpl()));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("name"));
        }

        // set fields
        initForm(createArrayList(field1, field2, field3));

        assertEquals(3, group.getFieldConfigList().size());
        assertArrayEquals(new Object[] { field1, field2, field3 }, group.getFieldConfigList().toArray());

        // set null
        initForm((List<FieldConfigImpl>) null);
        assertEquals(3, group.getFieldConfigList().size());

        // set empty
        initForm(new ArrayList<FieldConfigImpl>());
        assertEquals(0, group.getFieldConfigList().size());

        // unmodifiable
        try {
            group.getFieldConfigList().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        // set duplicated fields
        try {
            initForm(createArrayList(field1, field2, field1));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Duplicated field name: \"group1.fiELd1\""));
        }
    }

    @Test
    public void getFieldConfig() throws Exception {
        // not init
        assertNull(group.getFieldConfig("test"));

        // init
        initForm(createArrayList(field1, field2, field3));

        // case insensitive
        assertSame(field1, group.getFieldConfig("FIELD1"));
        assertSame(field2, group.getFieldConfig("field2"));
        assertSame(field3, group.getFieldConfig("fiELD3"));
    }

    @Test
    public void getFieldConfigByKey() throws Exception {
        // not init
        try {
            group.getFieldConfigByKey("test");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("fieldsByKey not inited"));
        }

        // init
        initForm(createArrayList(field1, field2, field3));

        // case insensitive
        assertSame(field1, group.getFieldConfigByKey("f"));
        assertSame(field2, group.getFieldConfigByKey("fi"));
        assertSame(field3, group.getFieldConfigByKey("fie"));
    }

    @Test
    public void getImports() throws Exception {
        // default is empty
        assertTrue(group.getImports().isEmpty());

        // setImports
        List<Import> imports = createArrayList((Import) new ImportImpl("groupName", "fieldName"));
        group.setImports(imports);

        assertEquals(1, group.getImports().size());
        assertSame(imports.get(0), group.getImports().get(0));

        // unmodifiable
        try {
            group.getImports().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void initFieldConfig() throws Exception {
        // no groups
        try {
            initForm((List<FieldConfigImpl>) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no fields"));
        }

        initForm(createArrayList(field1, field2, field3));

        // field.getGroupConfig()
        assertSame(group, field1.getGroupConfig());
        assertSame(group, field2.getGroupConfig());
        assertSame(group, field3.getGroupConfig());

        // field keys
        assertEquals("f", field1.getKey());
        assertEquals("fi", field2.getKey());
        assertEquals("fie", field3.getKey());
    }

    @Test
    public void extend_trimByDefault() throws Exception {
        // default value
        assertTrimByDefault(null, null, true);

        // derived from parent
        assertTrimByDefault(true, null, true);
        assertTrimByDefault(false, null, false);

        // override parent
        assertTrimByDefault(null, false, false);
        assertTrimByDefault(null, true, true);
        assertTrimByDefault(true, false, false);
        assertTrimByDefault(false, true, true);
    }

    private void assertTrimByDefault(Boolean parentValue, Boolean overrideValue, boolean expectedValue)
            throws Exception {
        init();

        if (parentValue != null) {
            group1.setTrimmingByDefault(parentValue);
        }

        group1.afterPropertiesSet();

        if (overrideValue != null) {
            group2.setTrimmingByDefault(overrideValue);
        }

        group2.setParentGroup("group1");
        group2.afterPropertiesSet();

        formConfig.setGroupConfigImplList(createArrayList(group1, group2));
        formConfig.afterPropertiesSet();

        assertEquals(expectedValue, group2.isTrimmingByDefault());
    }

    @Test
    public void extend_postOnly() throws Exception {
        // default value
        assertPostOnly(null, null, true);

        // derived from parent
        assertPostOnly(true, null, true);
        assertPostOnly(false, null, false);

        // override parent
        assertPostOnly(null, false, false);
        assertPostOnly(null, true, true);
        assertPostOnly(true, false, false);
        assertPostOnly(false, true, true);
    }

    private void assertPostOnly(Boolean parentValue, Boolean overrideValue, boolean expectedValue) throws Exception {
        init();

        if (parentValue != null) {
            group1.setPostOnly(parentValue);
        }

        group1.afterPropertiesSet();

        if (overrideValue != null) {
            group2.setPostOnly(overrideValue);
        }

        group2.setParentGroup("group1");
        group2.afterPropertiesSet();

        formConfig.setGroupConfigImplList(createArrayList(group1, group2));
        formConfig.afterPropertiesSet();

        assertEquals(expectedValue, group2.isPostOnly());
    }

    @Test
    public void import_fieldNotFound() throws Exception {
        group2.setImports(createArrayList((Import) new ImportImpl("group1", "notExistField")));

        formConfig.setGroupConfigImplList(createArrayList(group1, group2));

        try {
            formConfig.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Field \"group1.notExistField\" not found"));
        }
    }

    @Test
    public void import_dupFields() throws Exception {
        import_dupFields(false);
        import_dupFields(true);
    }

    private void import_dupFields(boolean importAll) throws Exception {
        init();

        List<Import> imports;
        field3.setName("field1"); // group2.field1 duped with group1.field1

        group1 = new GroupConfigImpl();
        group1.setName("group1");
        group1.setFieldConfigImplList(createArrayList(field1));
        group1.afterPropertiesSet();

        group2 = new GroupConfigImpl();
        group2.setName("group2");
        group2.setFieldConfigImplList(createArrayList(field2, field3));

        if (importAll) {
            imports = createArrayList((Import) new ImportImpl("group1", null));
        } else {
            imports = createArrayList((Import) new ImportImpl("group1", "field1"));
        }

        group2.setImports(imports);
        group2.afterPropertiesSet();

        formConfig.setGroupConfigImplList(createArrayList(group1, group2));

        try {
            formConfig.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Duplicated field name: \"group2.field1\""));
        }
    }

    @Test
    public void extend_overrideField() throws Exception {
        field3.setName("field1"); // group2.field1 overrides group1.field1

        group1 = new GroupConfigImpl();
        group1.setName("group1");
        group1.setFieldConfigImplList(createArrayList(field1));
        group1.afterPropertiesSet();

        group2 = new GroupConfigImpl();
        group2.setName("group2");
        group2.setFieldConfigImplList(createArrayList(field2, field3));
        group2.setParentGroup("group1");
        group2.afterPropertiesSet();

        formConfig.setGroupConfigImplList(createArrayList(group1, group2));
        formConfig.afterPropertiesSet();

        assertEquals(1, group1.getFieldConfigList().size());
        assertSame(field1, group1.getFieldConfig("field1"));

        assertEquals(2, group2.getFieldConfigList().size());
        assertSame(field3, group2.getFieldConfig("field1"));
        assertSame(field2, group2.getFieldConfig("field2"));
    }

    @Test
    public void toString_() throws Exception {
        // empty
        assertEquals("GroupConfig[name: group1, fields: 0]", group.toString());

        // with fields
        initForm(createArrayList(field1, field2, field3));
        assertEquals("GroupConfig[name: group1, fields: 3]", group.toString());
    }

    @Test
    public void toString_import() {
        assertEquals("group1", new ImportImpl("group1", null).toString());
        assertEquals("group1", new ImportImpl(" group1 ", "  ").toString());
        assertEquals("group1.field1", new ImportImpl("group1", " field1 ").toString());
    }
}
