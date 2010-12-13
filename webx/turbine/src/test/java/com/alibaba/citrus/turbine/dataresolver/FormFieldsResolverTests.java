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

import java.util.List;

import org.junit.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;

public class FormFieldsResolverTests extends AbstractDataResolverTests {
    private Field[] fieldArray;
    private List<Field> fieldList;
    private int[] dataArray;
    private List<Long> dataList;

    @Test
    public void getFields() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, empty form
        execute("action", "form.fields.myAction", "doGetFields", "");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, invalid
        execute("action", "form.fields.myAction", "doGetFields", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.fields.myAction", "doGetFields", "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=1" //
                + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=2");
        fieldArray = (Field[]) newRequest.getAttribute("actionLog");
        assertNotNull(fieldArray);
        assertEquals(2, fieldArray.length);
        assertEquals("a", fieldArray[0].getStringValue());
        assertEquals("c", fieldArray[1].getStringValue());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.fields.myScreen", "doGetFields", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        fieldArray = (Field[]) newRequest.getAttribute("screenLog");
        assertNotNull(fieldArray);
        assertEquals(2, fieldArray.length);

        for (Field field : fieldArray) {
            assertFalse(field.isValid());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getFieldsDontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.fields.myAction", "doGetFieldsDontSkipAction", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        fieldList = (List<Field>) newRequest.getAttribute("actionLog");
        assertNotNull(fieldList);
        assertEquals(2, fieldList.size());

        for (Field field : fieldList) {
            assertFalse(field.isValid());
        }

        // GET, valid
        execute("action", "form.fields.myAction", "doGetFieldsDontSkipAction",
                "_fm.m.aaa.f=a&_fm.m.aaa.f=b&_fm.m.aaa.fi=1" //
                        + "&_fm.m.bbb.f=c&_fm.m.bbb.f=d&_fm.m.bbb.fi=2");
        fieldList = (List<Field>) newRequest.getAttribute("actionLog");
        assertNotNull(fieldList);
        assertEquals(2, fieldList.size());

        for (Field field : fieldList) {
            assertTrue(field.isValid());
        }
    }

    @Test
    public void getFieldsBeans() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, invalid
        execute("action", "form.fields.myAction", "doGetFieldsBeans", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.fields.myAction", "doGetFieldsBeans", "_fm.m.aaa.f=11&_fm.m.aaa.f=12&_fm.m.aaa.fi=1" //
                + "&_fm.m.bbb.f=21&_fm.m.bbb.f=22&_fm.m.bbb.fi=2");
        dataArray = (int[]) newRequest.getAttribute("actionLog");
        assertNotNull(dataArray);
        assertArrayEquals(new int[] { 11, 21 }, dataArray);

        // GET, valid，多值
        execute("action", "form.fields.myAction", "doGetFieldsBeans2", "_fm.m.aaa.f=11&_fm.m.aaa.f=12&_fm.m.aaa.fi=1" //
                + "&_fm.m.bbb.f=21&_fm.m.bbb.f=22&_fm.m.bbb.fi=2");
        int[][] dataArray2 = (int[][]) newRequest.getAttribute("actionLog");
        assertNotNull(dataArray2);
        assertArrayEquals(new int[][] { { 11, 12 }, { 21, 22 } }, dataArray2);

        // GET, invalid, but screen不支持skip
        execute("screen", "form.fields.myScreenGetFieldsBeans", "doGetFieldsBeans", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        dataArray = (int[]) newRequest.getAttribute("screenLog");
        assertNull(dataArray);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getFieldsBeans_dontSkipAction() throws Exception {
        // 默认值：skipIfInvalid=false

        // GET, invalid
        execute("action", "form.fields.myAction", "doGetFieldsBeansDontSkipAction", "_fm.m.aaa.f=&_fm.m.aaa.fi=" //
                + "&_fm.m.bbb.f=&_fm.m.bbb.fi=");
        dataList = (List<Long>) newRequest.getAttribute("actionLog");
        assertNull(dataList);

        // GET, valid
        execute("action", "form.fields.myAction", "doGetFieldsBeansDontSkipAction",
                "_fm.m.aaa.f=11&_fm.m.aaa.f=12&_fm.m.aaa.fi=1" //
                        + "&_fm.m.bbb.f=21&_fm.m.bbb.f=22&_fm.m.bbb.fi=2");
        dataList = (List<Long>) newRequest.getAttribute("actionLog");
        assertNotNull(dataList);
        assertArrayEquals(new Object[] { 11L, 21L }, dataList.toArray());

        // GET, valid，多值
        execute("action", "form.fields.myAction", "doGetFieldsBeansDontSkipAction2",
                "_fm.m.aaa.f=11&_fm.m.aaa.f=12&_fm.m.aaa.fi=1" //
                        + "&_fm.m.bbb.f=21&_fm.m.bbb.f=22&_fm.m.bbb.fi=2");
        List<Long[]> dataList2 = (List<Long[]>) newRequest.getAttribute("actionLog");
        assertNotNull(dataList2);
        assertArrayEquals(new Long[][] { { 11L, 12L }, { 21L, 22L } }, dataList2.toArray(new Long[0][]));
    }

    @Test
    public void getFieldInstance_bean_convertError_quiet() throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, valid
        execute("action", "form.fields.myAction", "doGetFieldsBeans", "_fm.m.aaa.f=aa&_fm.m.aaa.f=12&_fm.m.aaa.fi=abc" //
                + "&_fm.m.bbb.f=21&_fm.m.bbb.f=22&_fm.m.bbb.fi=def");
        dataArray = (int[]) newRequest.getAttribute("actionLog");
        assertNotNull(dataArray);
        assertArrayEquals(new int[] { 0, 21 }, dataArray);
    }

    @Test
    public void getFieldInstance_bean_convertError_noisy() throws Exception {
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
            execute("action", "form.fields.myAction", "doGetFieldsBeans",
                    "_fm.m.aaa.f=aa&_fm.m.aaa.f=12&_fm.m.aaa.fi=abc" //
                            + "&_fm.m.bbb.f=21&_fm.m.bbb.f=22&_fm.m.bbb.fi=def");
            fail();
        } catch (ActionEventException e) {
            assertThat(e, exception(TypeMismatchException.class, "java.lang.String", "java.lang.Integer", "aa"));
        }
    }
}
