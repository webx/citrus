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

import static com.alibaba.citrus.util.BasicConstant.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport;
import com.alibaba.citrus.service.form.AbstractFormServiceTests;
import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;

/**
 * 测试form service配置及功能。
 * 
 * @author Michael Zhou
 */
public class FormServiceConfigTests extends AbstractFormServiceTests {
    private FormConfigImpl formConfig;
    private GroupConfigImpl groupConfig;
    private FieldConfigImpl fieldConfig;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form-config.xml");
    }

    private PropertyEditorRegistrarsSupport getRegisrarsSupport() {
        return (PropertyEditorRegistrarsSupport) formConfig.getPropertyEditorRegistrar();
    }

    @Test
    public void emptyFormConfig() {
        getFormConfig("emptyFormConfig");

        assertEquals(0, getRegisrarsSupport().size());
        assertEquals(0, formConfig.getGroupConfigList().size());
        assertEquals(true, formConfig.isPostOnlyByDefault());
    }

    @Test
    public void nonEmptyFormConfig() {
        getFormConfig("nonEmptyFormConfig");

        assertEquals(2, getRegisrarsSupport().size());
        assertEquals(2, formConfig.getGroupConfigList().size());
        assertEquals(false, formConfig.isPostOnlyByDefault());
    }

    @Test
    public void emptyGroupConfig() {
        getFormConfig("form1");
        getGroupConfig("emptyGroupConfig");

        assertEquals("emptyGroupConfig", groupConfig.getName());
        assertEquals(0, groupConfig.getFieldConfigList().size());
        assertEquals(0, groupConfig.getImports().size());
        assertEquals("e", groupConfig.getKey());
        assertEquals(null, groupConfig.getParentGroup());
        assertEquals(true, groupConfig.isTrimmingByDefault());

        // postOnlyByDefault == true
        formConfig.setPostOnlyByDefault(true);
        assertEquals(true, groupConfig.isPostOnly());

        // postOnlyByDefault == false
        formConfig.setPostOnlyByDefault(false);
        assertEquals(false, groupConfig.isPostOnly());
    }

    @Test
    public void nonEmptyGroupConfig() {
        getFormConfig("form1");
        getGroupConfig("nonEmptyGroupConfig");

        assertEquals("nonEmptyGroupConfig", groupConfig.getName());
        assertEquals(2, groupConfig.getFieldConfigList().size());
        assertEquals(0, groupConfig.getImports().size());
        assertEquals("n", groupConfig.getKey());
        assertEquals(null, groupConfig.getParentGroup());
        assertEquals(false, groupConfig.isTrimmingByDefault());

        // postOnlyByDefault == true
        formConfig.setPostOnlyByDefault(true);
        assertEquals(true, groupConfig.isPostOnly());

        // postOnlyByDefault == false
        formConfig.setPostOnlyByDefault(false);
        assertEquals(true, groupConfig.isPostOnly());
    }

    @Test
    public void emptyFieldConfig() {
        getFormConfig("form2");
        getGroupConfig("group1");
        getFieldConfig("emptyFieldConfig");

        assertEquals("emptyFieldConfig", fieldConfig.getName());
        assertArrayEquals(EMPTY_STRING_ARRAY, fieldConfig.getDefaultValues());
        assertEquals("emptyFieldConfig", fieldConfig.getDisplayName());
        assertEquals("e", fieldConfig.getKey());
        assertEquals("emptyFieldConfig", fieldConfig.getPropertyName());
        assertEquals(0, fieldConfig.getValidators().size());

        // trimmingByDefault=true
        groupConfig.setTrimmingByDefault(true);
        assertEquals(true, fieldConfig.isTrimming());

        // trimmingByDefault=false
        groupConfig.setTrimmingByDefault(false);
        assertEquals(false, fieldConfig.isTrimming());
    }

    @Test
    public void nonEmptyFieldConfig() {
        getFormConfig("form2");
        getGroupConfig("group1");
        getFieldConfig("nonEmptyFieldConfig");

        assertEquals("nonEmptyFieldConfig", fieldConfig.getName());
        assertArrayEquals(new String[] { "a", "b", "c" }, fieldConfig.getDefaultValues());
        assertEquals("非空域", fieldConfig.getDisplayName());
        assertEquals("n", fieldConfig.getKey());
        assertEquals("propertyName", fieldConfig.getPropertyName());
        assertEquals(2, fieldConfig.getValidators().size());

        // trimmingByDefault=true
        groupConfig.setTrimmingByDefault(true);
        assertEquals(false, fieldConfig.isTrimming());

        // trimmingByDefault=false
        groupConfig.setTrimmingByDefault(false);
        assertEquals(false, fieldConfig.isTrimming());
    }

    @Test
    public void extendsGroup() {
        getFormConfig("form3");
        getGroupConfig("group2");
        assertEquals(2, groupConfig.getFieldConfigList().size());

        getFieldConfig("field2");
        assertEquals(2, fieldConfig.getValidators().size());
    }

    @Test
    public void importGroup() {
        getFormConfig("form3");
        getGroupConfig("group3");
        assertEquals(2, groupConfig.getFieldConfigList().size());

        getFieldConfig("field2");
        assertEquals(1, fieldConfig.getValidators().size());
    }

    private void getFormConfig(String name) {
        getFormService(name);

        formConfig = (FormConfigImpl) formService.getFormConfig();

        assertNotNull(formConfig);
        assertSame(formService, formConfig.getFormService());
    }

    private void getGroupConfig(String name) {
        groupConfig = (GroupConfigImpl) formConfig.getGroupConfig(name);

        assertNotNull(groupConfig);
        assertSame(formConfig, groupConfig.getFormConfig());
    }

    private void getFieldConfig(String name) {
        fieldConfig = (FieldConfigImpl) groupConfig.getFieldConfig(name);

        assertNotNull(fieldConfig);
        assertSame(groupConfig, fieldConfig.getGroupConfig());
    }
}
