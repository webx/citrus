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
package com.alibaba.citrus.turbine.dataresolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;

public class FormFieldResolverTests extends AbstractDataResolverTests {
    private Field field;
    private String[] value1;
    private Integer value2;

    @Test
    public void getField() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, empty form
        execute("action", "form.field.myAction", "doGetField", "");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, invalid
        execute("action", "form.field.myAction", "doGetField", "_fm.m._0.f=&_fm.m._0.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.field.myAction", "doGetField", "_fm.m._0.f=a&_fm.m._0.fi=b");
        field = (Field) newRequest.getAttribute("actionLog");
        assertNotNull(field);
        assertTrue(field.isValid());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.field.myScreen", "doGetField", "_fm.m._0.f=&_fm.m._0.fi=");
        field = (Field) newRequest.getAttribute("screenLog");
        assertNotNull(field);
        assertFalse(field.isValid());
    }

    @Test
    public void getFieldDontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.field.myAction", "doGetFieldDontSkipAction", "_fm.m._0.f=&_fm.m._0.fi=");
        field = (Field) newRequest.getAttribute("actionLog");
        assertNotNull(field);
        assertFalse(field.isValid());

        // GET, valid
        execute("action", "form.field.myAction", "doGetFieldDontSkipAction", "_fm.m._0.f=a&_fm.m._0.fi=b");
        field = (Field) newRequest.getAttribute("actionLog");
        assertNotNull(field);
        assertTrue(field.isValid());
    }

    @Test
    public void getFieldInstance_value() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, invalid
        execute("action", "form.field.myAction", "doGetFieldInstanceValue", "_fm.m.aaa.f=&_fm.m.aaa.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.field.myAction", "doGetFieldInstanceValue",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=123");
        value1 = (String[]) newRequest.getAttribute("actionLog");
        assertNotNull(value1);
        assertArrayEquals(new String[] { "a", "b" }, value1);

        // GET, invalid, but screen不支持skip
        execute("screen", "form.field.myScreenGetFieldInstanceValue", "doGetFieldInstanceValue",
                "_fm.m.aaa.f=&_fm.m.aaa.fi=");
        value1 = (String[]) newRequest.getAttribute("screenLog");
        assertNull(value1);
    }

    @Test
    public void getFieldInstance_value_dontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.field.myAction", "doGetFieldInstanceValueDontSkipAction", "_fm.m.aaa.f=&_fm.m.aaa.fi=");
        value2 = (Integer) newRequest.getAttribute("actionLog");
        assertNull(value1);

        // GET, valid
        execute("action", "form.field.myAction", "doGetFieldInstanceValueDontSkipAction",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=123");
        value2 = (Integer) newRequest.getAttribute("actionLog");
        assertNotNull(value2);
        assertEquals(123, value2.intValue());
    }

    @Test
    public void getFieldInstance_value_convertError_quiet() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, valid
        execute("action", "form.field.myAction", "doGetFieldInstanceValueDontSkipAction",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=abc");
        value2 = (Integer) newRequest.getAttribute("actionLog");
        assertNull(value2);
    }

    @Test
    public void getFieldInstance_value_convertError_noisy() throws Exception {
        // 默认值：skipIfInvalid=true
        System.setProperty("convertQuiet", "false");

        try {
            ApplicationContext factory = createContext("dataresolver/services-dataresolver.xml");
            moduleLoaderService = (ModuleLoaderService) factory.getBean("moduleLoaderService");
        } finally {
            System.clearProperty("convertQuiet");
        }

        // GET, valid
        try {
            execute("action", "form.field.myAction", "doGetFieldInstanceValueDontSkipAction",
                    "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=abc");
            fail();
        } catch (ActionEventException e) {
            assertThat(e, exception(TypeMismatchException.class, "java.lang.String", "java.lang.Integer", "abc"));
        }
    }
}
