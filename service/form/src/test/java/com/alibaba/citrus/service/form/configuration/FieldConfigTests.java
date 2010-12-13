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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;

public class FieldConfigTests {
    private FormConfigImpl form;
    private GroupConfigImpl group;
    private FieldConfigImpl field;
    private Validator v1;
    private Validator v2;
    private Validator v3;
    private Validator v4;

    @Before
    public void init() throws Exception {
        field = new FieldConfigImpl();
        field.setName("field1");

        group = new GroupConfigImpl();
        group.setName("group1");
        group.setFieldConfigImplList(createArrayList(field));

        form = new FormConfigImpl();
        form.setGroupConfigImplList(createArrayList(group));

        v1 = createMock(Validator.class);
        v2 = createMock(Validator.class);
        v3 = createMock(Validator.class);
        v4 = createMock(Validator.class);
    }

    @Test
    public void getGroupConfig() throws Exception {
        // no group
        assertNull(field.getGroupConfig());

        // with group
        form.afterPropertiesSet();
        assertSame(group, field.getGroupConfig());
    }

    @Test
    public void getName() {
        FieldConfigImpl fieldConfig = new FieldConfigImpl();

        // no name
        assertNull(fieldConfig.getName());

        // empty name
        fieldConfig.setName("");
        assertNull(fieldConfig.getName());

        fieldConfig.setName("  ");
        assertNull(fieldConfig.getName());

        // normal name
        fieldConfig.setName("  hello ");
        assertEquals("hello", fieldConfig.getName());
    }

    @Test
    public void getKey() throws Exception {
        // no key
        assertNull(field.getKey());

        // empty key
        field.setKey("");
        assertNull(field.getKey());

        field.setKey("  ");
        assertNull(field.getKey());

        // normal key
        field.setKey("  hello ");
        assertEquals("hello", field.getKey());

        // via form.init
        form.afterPropertiesSet();
        assertEquals("f", field.getKey());
    }

    @Test
    public void getDisplayName() throws Exception {
        FieldConfigImpl fieldConfig = new FieldConfigImpl();

        // init null
        assertNull(fieldConfig.getDisplayName());

        // empty
        fieldConfig.setDisplayName(null);
        assertNull(fieldConfig.getDisplayName());

        fieldConfig.setDisplayName("  ");
        assertNull(fieldConfig.getDisplayName());

        // same as name by default
        fieldConfig.setName("field1");
        assertEquals("field1", fieldConfig.getDisplayName());

        fieldConfig.setDisplayName(null);
        assertEquals("field1", fieldConfig.getDisplayName());

        fieldConfig.setDisplayName("  ");
        assertEquals("field1", fieldConfig.getDisplayName());

        // specific display name
        fieldConfig.setDisplayName(" displayName1 ");
        assertEquals("displayName1", fieldConfig.getDisplayName());
    }

    @Test
    public void isTrimming() throws Exception {
        // default, no group config
        assertTrue(field.isTrimming());

        // default, with group config
        form.afterPropertiesSet();

        group.setTrimmingByDefault(false);
        assertFalse(field.isTrimming());

        group.setTrimmingByDefault(true);
        assertTrue(field.isTrimming());

        // set value
        field.setTrimming(false);
        assertFalse(field.isTrimming());
    }

    @Test
    public void getPropertyName() throws Exception {
        FieldConfigImpl fieldConfig = new FieldConfigImpl();

        // default, no name
        assertNull(fieldConfig.getPropertyName());

        // default, with name
        assertEquals("field1", field.getPropertyName());

        // empty property name
        field.setPropertyName(null);
        assertEquals("field1", field.getPropertyName());

        field.setPropertyName("  ");
        assertEquals("field1", field.getPropertyName());

        // specific property name
        field.setPropertyName(" property.name ");
        assertEquals("property.name", field.getPropertyName());
    }

    @Test
    public void getDefaultValues() {
        // default
        assertNull(field.getDefaultValue());

        // set empty array
        field.setDefaultValues(null);
        assertNull(field.getDefaultValue());
        assertArrayEquals(new String[0], field.getDefaultValues());

        field.setDefaultValues(new String[0]);
        assertNull(field.getDefaultValue());
        assertArrayEquals(new String[0], field.getDefaultValues());

        // set array
        field.setDefaultValues(new String[] { "aaa", "bbb", "ccc" });
        assertEquals("aaa", field.getDefaultValue());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, field.getDefaultValues());
    }

    @Test
    public void getValidators() {
        // default
        assertArrayEquals(new Object[0], field.getValidators().toArray());

        // set validators
        field.setValidators(createArrayList(v1, v2, v3));
        assertArrayEquals(new Object[] { v1, v2, v3 }, field.getValidators().toArray());

        // unmodifiable
        try {
            field.getValidators().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void merge() throws Exception {
        FieldConfigImpl src = new FieldConfigImpl();
        FieldConfigImpl copy;

        src.setName("name1");

        // groupConfig
        copy = mergeField(null, src);
        assertEquals("groupCopy", copy.getGroupConfig().getName());

        // name
        assertEquals("name1", mergeField(null, src).getName());

        // displayName
        src.setDisplayName("displayName1");
        assertEquals("displayName1", mergeField(null, src).getDisplayName());

        copy = new FieldConfigImpl();
        copy.setDisplayName("displayName2");
        assertEquals("displayName2", mergeField(copy, src).getDisplayName());

        // defaultValues
        src.setDefaultValues(new String[] { "aaa", "bbb", "ccc" });
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, mergeField(null, src).getDefaultValues());

        copy = new FieldConfigImpl();
        copy.setDefaultValues(new String[] { "ddd" });
        assertArrayEquals(new String[] { "ddd" }, mergeField(copy, src).getDefaultValues());

        // trimming
        assertEquals(true, mergeField(null, src).isTrimming());

        src.setTrimming(false);
        assertEquals(false, mergeField(null, src).isTrimming());

        copy = new FieldConfigImpl();
        copy.setTrimming(true);
        assertEquals(true, mergeField(copy, src).isTrimming());

        // propertyName
        assertEquals("name1", mergeField(null, src).getPropertyName());

        src.setPropertyName("propertyName1");
        assertEquals("propertyName1", mergeField(null, src).getPropertyName());

        copy = new FieldConfigImpl();
        copy.setPropertyName("propertyName2");
        assertEquals("propertyName2", mergeField(copy, src).getPropertyName());

        // validators
        assertArrayEquals(new Object[0], mergeField(null, src).getValidators().toArray());

        expect(v1.clone()).andReturn(v3).anyTimes();
        v1.init(src);
        expectLastCall().anyTimes();
        replay(v1);

        v3.init(isA(FieldConfig.class));
        expectLastCall().anyTimes();
        replay(v3);

        src.setValidators(createArrayList(v1));
        assertArrayEquals(new Object[] { v3 }, mergeField(null, src).getValidators().toArray());

        copy = new FieldConfigImpl();

        expect(v2.clone()).andReturn(v4).anyTimes();
        v2.init(copy);
        expectLastCall().once();
        replay(v2);

        replay(v4);

        copy.setValidators(createArrayList(v2));
        assertArrayEquals(new Object[] { v2, v3 }, mergeField(copy, src).getValidators().toArray());

        verify(v1, v2, v3, v4);
    }

    private FieldConfigImpl mergeField(FieldConfigImpl copy, FieldConfigImpl src) throws Exception {
        GroupConfigImpl groupCopy = new GroupConfigImpl();
        groupCopy.setName("groupCopy");

        List<FieldConfigImpl> fields = createArrayList();

        if (copy != null) {
            copy.setName(src.getName());
            fields.add(copy);
        }

        groupCopy.setFieldConfigImplList(fields);
        groupCopy.setParentGroup("groupSrc");
        groupCopy.afterPropertiesSet();

        GroupConfigImpl groupSrc = new GroupConfigImpl();
        groupSrc.setName("groupSrc");
        groupSrc.setFieldConfigImplList(createArrayList(src));
        groupSrc.afterPropertiesSet();

        form = new FormConfigImpl();
        form.setGroupConfigImplList(createArrayList(groupCopy, groupSrc));
        form.afterPropertiesSet();

        FieldConfigImpl copy2 = (FieldConfigImpl) groupCopy.getFieldConfig(src.getName());

        if (copy != null) {
            assertSame(copy, copy2);
        }

        return copy2;
    }

    @Test
    public void toString_() throws Exception {
        // empty
        assertEquals("FieldConfig[group: null, name: field1, validators: 0]", field.toString());

        // with group
        form.afterPropertiesSet();
        assertEquals("FieldConfig[group: group1, name: field1, validators: 0]", field.toString());

        // with validators
        field.setValidators(createArrayList(v1, v2, v3));
        assertEquals("FieldConfig[group: group1, name: field1, validators: 3]", field.toString());
    }
}
