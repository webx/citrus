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

import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.test2.module.action.form.MyData;

public class FormGroupResolverTests extends AbstractDataResolverTests {
    private Group group;
    private MyData data;

    @Test
    public void error_NoName() throws Exception {
        try {
            execute("action", "form.group.myActionErrorNoName", "doPerform", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing @FormGroup's name"));
        }
    }

    @Test
    public void getGroup() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, empty form
        execute("action", "form.group.myAction", "doGetGroup", "");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, invalid
        execute("action", "form.group.myAction", "doGetGroup", "_fm.m._0.f=&_fm.m._0.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.group.myAction", "doGetGroup", "_fm.m._0.f=a&_fm.m._0.fi=b");
        group = (Group) newRequest.getAttribute("actionLog");
        assertNotNull(group);
        assertTrue(group.isValid());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.group.myScreen", "doGetGroup", "_fm.m._0.f=&_fm.m._0.fi=");
        group = (Group) newRequest.getAttribute("screenLog");
        assertNotNull(group);
        assertFalse(group.isValid());
    }

    @Test
    public void getGroupDontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.group.myAction", "doGetGroupDontSkipAction", "_fm.m._0.f=&_fm.m._0.fi=");
        group = (Group) newRequest.getAttribute("actionLog");
        assertNotNull(group);
        assertFalse(group.isValid());

        // GET, valid
        execute("action", "form.group.myAction", "doGetGroupDontSkipAction", "_fm.m._0.f=a&_fm.m._0.fi=b");
        group = (Group) newRequest.getAttribute("actionLog");
        assertNotNull(group);
        assertTrue(group.isValid());
    }

    @Test
    public void getGroupInstance_bean() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, invalid
        execute("action", "form.group.myAction", "doGetGroupInstanceBean", "_fm.m.aaa.f=&_fm.m.aaa.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.group.myAction", "doGetGroupInstanceBean",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=123");
        data = (MyData) newRequest.getAttribute("actionLog");
        assertNotNull(data);
        assertArrayEquals(new Object[] { "a", "b" }, data.getField1().toArray());
        assertEquals(123, data.getField2());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.group.myScreenGetGroupInstanceBean", "doGetGroupInstanceBean",
                "_fm.m.aaa.f=&_fm.m.aaa.fi=");
        data = (MyData) newRequest.getAttribute("screenLog");
        assertNull(data);
    }

    @Test
    public void getGroupInstance_bean_dontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.group.myAction", "doGetGroupInstanceBeanDontSkipAction", "_fm.m.aaa.f=&_fm.m.aaa.fi=");
        data = (MyData) newRequest.getAttribute("actionLog");
        assertNull(data);

        // GET, valid
        execute("action", "form.group.myAction", "doGetGroupInstanceBeanDontSkipAction",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=123");
        data = (MyData) newRequest.getAttribute("actionLog");
        assertNotNull(data);
        assertArrayEquals(new Object[] { "a", "b" }, data.getField1().toArray());
        assertEquals(123, data.getField2());
    }

    @Test
    public void getGroupInstance_bean_convertError_quiet() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, valid
        execute("action", "form.group.myAction", "doGetGroupInstanceBean",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=abc");
        data = (MyData) newRequest.getAttribute("actionLog");
        assertNotNull(data);
        assertArrayEquals(new Object[] { "a", "b" }, data.getField1().toArray());
        assertEquals(0, data.getField2());
    }

    @Test
    public void getGroupInstance_bean_convertError_noisy() throws Exception {
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
            execute("action", "form.group.myAction", "doGetGroupInstanceBean",
                    "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=abc");
            fail();
        } catch (ActionEventException e) {
            assertThat(e, exception(TypeMismatchException.class, "java.lang.String", "java.lang.Integer", "abc"));
        }
    }
}
