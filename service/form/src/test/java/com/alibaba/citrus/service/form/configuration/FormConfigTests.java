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
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.SimpleTypeConverter;

import com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport;
import com.alibaba.citrus.service.form.FormService;
import com.alibaba.citrus.service.form.configuration.GroupConfig.Import;
import com.alibaba.citrus.service.form.impl.MyPropertyEditorRegistrar;
import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl.ImportImpl;

public class FormConfigTests {
    private FormConfigImpl formConfig;
    private GroupConfigImpl group1;
    private GroupConfigImpl group2;
    private GroupConfigImpl group3;
    private GroupConfigImpl group4;

    @Before
    public void init() {
        formConfig = new FormConfigImpl();

        group1 = new GroupConfigImpl();
        group1.setName("grOUp1");
        group1.setFieldConfigImplList(createArrayList(createField("field1")));

        group2 = new GroupConfigImpl();
        group2.setName("group2");
        group2.setFieldConfigImplList(createArrayList(createField("field2")));

        group3 = new GroupConfigImpl();
        group3.setName("GROUP3");
        group3.setFieldConfigImplList(createArrayList(createField("field3")));

        group4 = new GroupConfigImpl();
        group4.setName("group4");
        group4.setFieldConfigImplList(createArrayList(createField("field4"), createField("field4.2")));
    }

    private FieldConfigImpl createField(String fieldName) {
        FieldConfigImpl field = new FieldConfigImpl();
        field.setName(fieldName);
        return field;
    }

    private void initForm(List<GroupConfigImpl> groupList) throws Exception {
        formConfig.setGroupConfigImplList(groupList);
        formConfig.afterPropertiesSet();
    }

    private PropertyEditorRegistrarsSupport getRegisrarsSupport() {
        return (PropertyEditorRegistrarsSupport) formConfig.getPropertyEditorRegistrar();
    }

    @Test
    public void getFormService() {
        // default
        assertNull(formConfig.getFormService());

        // set null
        try {
            formConfig.setFormService(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("formService"));
        }

        // not null
        FormService formService = createMock(FormService.class);
        formConfig.setFormService(formService);
        assertSame(formService, formConfig.getFormService());
    }

    @Test
    public void isPostOnlyByDefault() {
        // default value
        assertTrue(formConfig.isPostOnlyByDefault());

        // set value
        formConfig.setPostOnlyByDefault(false);
        assertFalse(formConfig.isPostOnlyByDefault());
    }

    @Test
    public void getGroupConfigList() throws Exception {
        // default is empty
        assertTrue(formConfig.getGroupConfigList().isEmpty());

        // group name is null
        try {
            initForm(createArrayList(new GroupConfigImpl()));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("name"));
        }

        // set groups
        initForm(createArrayList(group1, group2, group3));

        assertEquals(3, formConfig.getGroupConfigList().size());
        assertArrayEquals(new Object[] { group1, group2, group3 }, formConfig.getGroupConfigList().toArray());

        // set null
        initForm(null);
        assertEquals(3, formConfig.getGroupConfigList().size());

        // unmodifiable
        try {
            formConfig.getGroupConfigList().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        // set duplicated groups
        try {
            initForm(createArrayList(group1, group2, group1));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Duplicated group name: grOUp1"));
        }
    }

    @Test
    public void getGroupConfig() throws Exception {
        // not init
        assertNull(null, formConfig.getGroupConfig("test"));

        // init
        initForm(createArrayList(group1, group2, group3));

        // case insensitive
        assertSame(group1, formConfig.getGroupConfig("GROUP1"));
        assertSame(group2, formConfig.getGroupConfig("group2"));
        assertSame(group3, formConfig.getGroupConfig("grOUP3"));
    }

    @Test
    public void getGroupConfigByKey() throws Exception {
        // not init
        try {
            formConfig.getGroupConfigByKey("test");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("groupsByKey not inited"));
        }

        // init
        initForm(createArrayList(group1, group2, group3));

        // case insensitive
        assertSame(group1, formConfig.getGroupConfigByKey("g"));
        assertSame(group2, formConfig.getGroupConfigByKey("gr"));
        assertSame(group3, formConfig.getGroupConfigByKey("gro"));
    }

    @Test
    public void getPropertyEditorRegistrars() {
        // default is empty
        assertEquals(0, getRegisrarsSupport().size());

        // set
        PropertyEditorRegistrar r1 = createMock(PropertyEditorRegistrar.class);
        PropertyEditorRegistrar r2 = createMock(PropertyEditorRegistrar.class);

        formConfig.setPropertyEditorRegistrars(new PropertyEditorRegistrar[] { r1, r2 });
        assertArrayEquals(new PropertyEditorRegistrar[] { r1, r2 }, getRegisrarsSupport().getPropertyEditorRegistrars());

        // set null/empty
        formConfig.setPropertyEditorRegistrars(null);
        assertArrayEquals(new PropertyEditorRegistrar[0], getRegisrarsSupport().getPropertyEditorRegistrars());

        formConfig.setPropertyEditorRegistrars(new PropertyEditorRegistrar[0]);
        assertArrayEquals(new PropertyEditorRegistrar[0], getRegisrarsSupport().getPropertyEditorRegistrars());
    }

    @Test
    public void registerCustomEditors() {
        PropertyEditorRegistrar registrar = new MyPropertyEditorRegistrar();
        formConfig.setPropertyEditorRegistrars(new PropertyEditorRegistrar[] { registrar });

        SimpleTypeConverter registry = new SimpleTypeConverter();
        formConfig.getPropertyEditorRegistrar().registerCustomEditors(registry);

        assertEquals(123, registry.convertIfNecessary("anything", Integer.class));
    }

    @Test
    public void initFormConfig() throws Exception {
        // no groups
        try {
            initForm(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no groups"));
        }

        initForm(createArrayList(group1, group2, group3));

        // group.getFormConfig()
        assertSame(formConfig, group1.getFormConfig());
        assertSame(formConfig, group2.getFormConfig());
        assertSame(formConfig, group3.getFormConfig());

        // group keys
        assertEquals("g", group1.getKey());
        assertEquals("gr", group2.getKey());
        assertEquals("gro", group3.getKey());
    }

    @Test
    public void extendGroup_extend_notFound() throws Exception {
        group1.setParentGroup("group2");
        group2.setParentGroup("group3");

        try {
            initForm(createArrayList(group1, group2));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Parent or imported group name \"group3\" not found"));
        }
    }

    @Test
    public void extendGroup_import_notFound() throws Exception {
        group1.setParentGroup("group2");

        group2.setImports(createArrayList((Import) new ImportImpl("group3", null)));

        try {
            initForm(createArrayList(group1, group2));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Parent or imported group name \"group3\" not found"));
        }
    }

    @Test
    public void extendGroup_extends_cycle() throws Exception {
        group1.setParentGroup("group2");
        group2.setParentGroup("group3");
        group3.setParentGroup("group1");

        try {
            initForm(createArrayList(group1, group2, group3));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Cycle detected: grOUp1 -> group2 -> GROUP3 -> grOUp1"));
        }
    }

    @Test
    public void extendGroup_import_cycle() throws Exception {
        group1.setParentGroup("group2");
        group2.setParentGroup("group3");

        group3.setImports(createArrayList((Import) new ImportImpl("group1", null)));

        try {
            initForm(createArrayList(group1, group2, group3));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Cycle detected: grOUp1 -> group2 -> GROUP3 -> grOUp1"));
        }
    }

    @Test
    public void extendAndImportGroup() throws Exception {
        // group1 -> group2
        group1.setParentGroup("group2");

        // group2 imports group3.*
        group2.setImports(createArrayList((Import) new ImportImpl("group3", null)));

        // group3 imports group4.field4
        group3.setImports(createArrayList((Import) new ImportImpl("group4", "field4")));

        initForm(createArrayList(group1, group2, group3, group4));

        assertFields(group1, "field1", "field2", "field3", "field4");
        assertFields(group2, "field2", "field3", "field4");
        assertFields(group3, "field3", "field4");
        assertFields(group4, "field4", "field4.2");
    }

    private void assertFields(GroupConfig group, String... fieldNames) {
        List<FieldConfig> fields = group.getFieldConfigList();
        assertEquals(fieldNames.length, fields.size());

        int i = 0;
        for (FieldConfig field : fields) {
            assertEquals(fieldNames[i++], field.getName());
        }
    }

    @Test
    public void toString_() throws Exception {
        // empty
        assertEquals("FormConfig[groups: 0]", formConfig.toString());

        // with groups
        initForm(createArrayList(group1, group2, group3));
        assertEquals("FormConfig[groups: 3]", formConfig.toString());
    }
}
