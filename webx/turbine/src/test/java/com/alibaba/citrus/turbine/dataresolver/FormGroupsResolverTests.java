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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.test2.module.action.form.MyData;

public class FormGroupsResolverTests extends AbstractDataResolverTests {
    private Group[] groupArray;
    private List<Group> groupList;
    private MyData[] dataArray;
    private Collection<MyData> dataList;

    @Test
    public void error_NoName() throws Exception {
        try {
            execute("action", "form.groups.myActionErrorNoName", "doPerform", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing @FormGroups's name"));
        }
    }

    @Test
    public void getGroups() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, empty form
        execute("action", "form.groups.myAction", "doGetGroups", "");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, invalid
        execute("action", "form.groups.myAction", "doGetGroups", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.groups.myAction", "doGetGroups", "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=1" //
                + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=2");
        groupArray = (Group[]) newRequest.getAttribute("actionLog");
        assertNotNull(groupArray);
        assertEquals(2, groupArray.length);
        assertEquals("aaa", groupArray[0].getInstanceKey());
        assertEquals(1, groupArray[0].getField("field2").getIntegerValue());
        assertEquals("bbb", groupArray[1].getInstanceKey());
        assertEquals(2, groupArray[1].getField("field2").getIntegerValue());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.groups.myScreen", "doGetGroups", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        groupArray = (Group[]) newRequest.getAttribute("screenLog");
        assertNotNull(groupArray);
        assertEquals(2, groupArray.length);

        for (Group group : groupArray) {
            assertFalse(group.isValid());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getGroupsDontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.groups.myAction", "doGetGroupsDontSkipAction", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        groupList = (List<Group>) newRequest.getAttribute("actionLog");
        assertNotNull(groupList);
        assertEquals(2, groupList.size());

        for (Group group : groupList) {
            assertFalse(group.isValid());
        }

        // GET, valid
        execute("action", "form.groups.myAction", "doGetGroupsDontSkipAction",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=1" //
                        + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=2");
        groupList = (List<Group>) newRequest.getAttribute("actionLog");
        assertNotNull(groupList);
        assertEquals(2, groupList.size());

        for (Group group : groupList) {
            assertTrue(group.isValid());
        }
    }

    @Test
    public void getGroupsBeans() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, invalid
        execute("action", "form.groups.myAction", "doGetGroupsBeans", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.groups.myAction", "doGetGroupsBeans", "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=1" //
                + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=2");
        dataArray = (MyData[]) newRequest.getAttribute("actionLog");
        assertNotNull(dataArray);
        assertEquals(2, dataArray.length);
        assertArrayEquals(new Object[] { "a", "b" }, dataArray[0].getField1().toArray());
        assertArrayEquals(new Object[] { "c", "d" }, dataArray[1].getField1().toArray());
        assertEquals(1, dataArray[0].getField2());
        assertEquals(2, dataArray[1].getField2());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.groups.myScreenGetGroupsBeans", "doGetGroupsBeans", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        dataArray = (MyData[]) newRequest.getAttribute("screenLog");
        assertNull(dataArray);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getGroupsBeans_dontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.groups.myAction", "doGetGroupsBeansDontSkipAction", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        dataList = (List<MyData>) newRequest.getAttribute("actionLog");
        assertNull(dataList);

        // GET, valid
        execute("action", "form.groups.myAction", "doGetGroupsBeansDontSkipAction",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=1" //
                        + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=2");
        dataList = (Collection<MyData>) newRequest.getAttribute("actionLog");
        assertNotNull(dataList);
        assertEquals(2, dataList.size());

        Iterator<MyData> i = dataList.iterator();
        MyData data = i.next();
        assertArrayEquals(new Object[] { "a", "b" }, data.getField1().toArray());
        assertEquals(1, data.getField2());

        data = i.next();
        assertArrayEquals(new Object[] { "c", "d" }, data.getField1().toArray());
        assertEquals(2, data.getField2());
    }

    @Test
    public void getGroupInstance_bean_convertError_quiet() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, valid
        execute("action", "form.groups.myAction", "doGetGroupsBeans", "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=abc" //
                + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=def");
        dataArray = (MyData[]) newRequest.getAttribute("actionLog");
        assertNotNull(dataArray);
        assertEquals(2, dataArray.length);
        assertArrayEquals(new Object[] { "a", "b" }, dataArray[0].getField1().toArray());
        assertArrayEquals(new Object[] { "c", "d" }, dataArray[1].getField1().toArray());
        assertEquals(0, dataArray[0].getField2());
        assertEquals(0, dataArray[1].getField2());
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
            execute("action", "form.groups.myAction", "doGetGroupsBeans",
                    "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=abc" //
                            + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=def");
            fail();
        } catch (ActionEventException e) {
            assertThat(e, exception(TypeMismatchException.class, "java.lang.String", "java.lang.Integer", "abc"));
        }
    }
}
