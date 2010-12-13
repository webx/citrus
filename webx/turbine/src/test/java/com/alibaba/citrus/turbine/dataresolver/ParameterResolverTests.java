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

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.PropertyEditor;
import java.io.File;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.junit.Test;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.turbine.dataresolver.impl.ParameterResolverFactory;

public class ParameterResolverTests extends AbstractDataResolverTests {
    @Test
    public void nodeps() {
        ParameterResolverFactory resolverFactory = new ParameterResolverFactory(null);

        try {
            resolverFactory.getDataResolver(new DataResolverContext(String.class, null, null));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no ParserRequestContext defined"));
        }
    }

    @Test
    public void getInt() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetInt", "bbb=222");
        assertLog("actionLog", Integer.class, 0);

        execute("action", "param.myAction", "doGetInt", "aaa=&bbb=222");
        assertLog("actionLog", Integer.class, 0);

        // single value
        execute("action", "param.myAction", "doGetInt", "aaa=111&bbb=222");
        assertLog("actionLog", Integer.class, 111);

        // multiple values -> single value
        execute("action", "param.myAction", "doGetInt", "aaa=111&aaa=222");
        assertLog("actionLog", Integer.class, 111);

        // wrong format
        try {
            execute("action", "param.myAction", "doGetInt", "aaa=wrong&aaa=222");
            fail();
        } catch (ActionEventException e) {
            assertThat(
                    e,
                    exception(TypeMismatchException.class,
                            "Failed to convert value of type [java.lang.String] to required type [java.lang.Integer]",
                            "wrong"));
        }
    }

    @Test
    public void getIntDefault() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetIntDefault", "bbb=222");
        assertLog("actionLog", Integer.class, 123);

        execute("action", "param.myAction", "doGetIntDefault", "aaa=&bbb=222");
        assertLog("actionLog", Integer.class, 123);

        // single value
        execute("action", "param.myAction", "doGetIntDefault", "aaa=111&bbb=222");
        assertLog("actionLog", Integer.class, 111);

        // multiple values -> single value
        execute("action", "param.myAction", "doGetIntDefault", "aaa=111&aaa=222");
        assertLog("actionLog", Integer.class, 111);
    }

    @Test
    public void getIntArray() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetIntArray", "aaa=");
        assertArrayEquals(new int[] {}, (int[]) request.getAttribute("actionLog"));

        // single value
        execute("action", "param.myAction", "doGetIntArray", "aaa=111");
        assertArrayEquals(new int[] { 111 }, (int[]) request.getAttribute("actionLog"));

        // multiple values
        execute("action", "param.myAction", "doGetIntArray", "aaa=111&aaa=222");
        assertArrayEquals(new int[] { 111, 222 }, (int[]) request.getAttribute("actionLog"));
    }

    @Test
    public void getIntArrayDefault() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetIntArrayDefault", "aaa=");
        assertArrayEquals(new int[] { 123 }, (int[]) request.getAttribute("actionLog"));

        // single value
        execute("action", "param.myAction", "doGetIntArrayDefault", "aaa=111");
        assertArrayEquals(new int[] { 111 }, (int[]) request.getAttribute("actionLog"));

        // multiple values
        execute("action", "param.myAction", "doGetIntArrayDefault", "aaa=111&aaa=222");
        assertArrayEquals(new int[] { 111, 222 }, (int[]) request.getAttribute("actionLog"));
    }

    @Test
    public void getInteger() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetInteger", "bbb=222");
        assertLog("actionLog", Integer.class, null);

        execute("action", "param.myAction", "doGetInteger", "aaa=&bbb=222");
        assertLog("actionLog", Integer.class, null);

        // single value
        execute("action", "param.myAction", "doGetInteger", "aaa=111&bbb=222");
        assertLog("actionLog", Integer.class, 111);

        // multiple values -> single value
        execute("action", "param.myAction", "doGetInteger", "aaa=111&aaa=222");
        assertLog("actionLog", Integer.class, 111);
    }

    @Test
    public void getIntegerDefault() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetIntegerDefault", "bbb=222");
        assertLog("actionLog", Integer.class, 123);

        execute("action", "param.myAction", "doGetIntegerDefault", "aaa=&bbb=222");
        assertLog("actionLog", Integer.class, 123);

        // single value
        execute("action", "param.myAction", "doGetIntegerDefault", "aaa=111&bbb=222");
        assertLog("actionLog", Integer.class, 111);

        // multiple values -> single value
        execute("action", "param.myAction", "doGetIntegerDefault", "aaa=111&aaa=222");
        assertLog("actionLog", Integer.class, 111);
    }

    @Test
    public void getIntegerArray() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetIntegerArray", "aaa=");
        assertArrayEquals(new Integer[] {}, (Integer[]) request.getAttribute("actionLog"));

        // single value
        execute("action", "param.myAction", "doGetIntegerArray", "aaa=111");
        assertArrayEquals(new Integer[] { 111 }, (Integer[]) request.getAttribute("actionLog"));

        // multiple values
        execute("action", "param.myAction", "doGetIntegerArray", "aaa=111&aaa=222");
        assertArrayEquals(new Integer[] { 111, 222 }, (Integer[]) request.getAttribute("actionLog"));
    }

    @Test
    public void getIntegerArrayDefault() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetIntegerArrayDefault", "");
        assertArrayEquals(new Integer[] { 123, 456 }, (Integer[]) request.getAttribute("actionLog"));

        // single value
        execute("action", "param.myAction", "doGetIntegerArray", "aaa=111");
        assertArrayEquals(new Integer[] { 111 }, (Integer[]) request.getAttribute("actionLog"));

        // multiple values
        execute("action", "param.myAction", "doGetIntegerArray", "aaa=111&aaa=222");
        assertArrayEquals(new Integer[] { 111, 222 }, (Integer[]) request.getAttribute("actionLog"));
    }

    @Test
    public void getIntegerList() throws Exception {
        execute("action", "param.myAction", "doGetIntegerList", "aaa=111&aaa=222");

        @SuppressWarnings("unchecked")
        List<Integer> array = (List<Integer>) request.getAttribute("actionLog");

        assertArrayEquals(new Integer[] { 111, 222 }, array.toArray(new Integer[0]));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getIntegerListDefault() throws Exception {
        // default values
        execute("action", "param.myAction", "doGetIntegerListDefault", "");

        List<Integer> array = (List<Integer>) request.getAttribute("actionLog");

        assertArrayEquals(new Integer[] { 123, 456 }, array.toArray(new Integer[0]));

        // multiple values
        execute("action", "param.myAction", "doGetIntegerListDefault", "aaa=111&aaa=222");

        array = (List<Integer>) request.getAttribute("actionLog");

        assertArrayEquals(new Integer[] { 111, 222 }, array.toArray(new Integer[0]));
    }

    @Test
    public void getLong() throws Exception {
        // single value - custom editor
        execute("action", "param.myAction", "doGetLong", "aaa=ten&bbb=222");
        assertLog("actionLog", Long.class, 10L);

        // single value
        execute("action", "param.myAction", "doGetLong", "aaa=11&bbb=222");
        assertLog("actionLog", Long.class, 11L);
    }

    @Test
    public void getLongDefault() throws Exception {
        // default value - custom editor
        execute("action", "param.myAction", "doGetLongDefault", "");
        assertLog("actionLog", Long.class, 10L);

        // single value
        execute("action", "param.myAction", "doGetLongDefault", "aaa=11&bbb=222");
        assertLog("actionLog", Long.class, 11L);
    }

    @Test
    public void getBool() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetBool", "bbb=222");
        assertLog("actionLog", Boolean.class, false);

        execute("action", "param.myAction", "doGetBool", "aaa=&bbb=222");
        assertLog("actionLog", Boolean.class, false);

        // single value
        execute("action", "param.myAction", "doGetBool", "aaa=true&bbb=222");
        assertLog("actionLog", Boolean.class, true);

        // multiple values -> single value
        execute("action", "param.myAction", "doGetBool", "aaa=false&aaa=222");
        assertLog("actionLog", Boolean.class, false);
    }

    @Test
    public void getBooleanArrayDefault() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetBooleanArrayDefault", "bbb=222");
        assertArrayEquals(new Boolean[] { true, false }, (Boolean[]) request.getAttribute("actionLog"));

        execute("action", "param.myAction", "doGetBooleanArrayDefault", "aaa=&bbb=222");
        assertArrayEquals(new Boolean[] { true, false }, (Boolean[]) request.getAttribute("actionLog"));

        // single value
        execute("action", "param.myAction", "doGetBooleanArrayDefault", "aaa=true&bbb=222");
        assertArrayEquals(new Boolean[] { true }, (Boolean[]) request.getAttribute("actionLog"));

        // multiple values
        execute("action", "param.myAction", "doGetBooleanArrayDefault", "aaa=false&aaa=false");
        assertArrayEquals(new Boolean[] { false, false }, (Boolean[]) request.getAttribute("actionLog"));
    }

    @Test
    public void getString() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetString", "bbb=222");
        assertLog("actionLog", String.class, null);

        execute("action", "param.myAction", "doGetString", "aaa=&bbb=222");
        assertLog("actionLog", String.class, null);

        // single value
        execute("action", "param.myAction", "doGetString", "aaa=111&bbb=222");
        assertLog("actionLog", String.class, "111");

        // multiple values -> single value
        execute("action", "param.myAction", "doGetString", "aaa=111&aaa=222");
        assertLog("actionLog", String.class, "111");
    }

    @Test
    public void getStringArrayDefault() throws Exception {
        // default value
        execute("action", "param.myAction", "doGetStringArrayDefault", "bbb=222");
        assertArrayEquals(new String[] { "", "abc" }, (String[]) request.getAttribute("actionLog"));

        execute("action", "param.myAction", "doGetStringArrayDefault", "aaa=&bbb=222");
        assertArrayEquals(new String[] { "", "abc" }, (String[]) request.getAttribute("actionLog"));

        // single value
        execute("action", "param.myAction", "doGetStringArrayDefault", "aaa=111&bbb=222");
        assertArrayEquals(new String[] { "111" }, (String[]) request.getAttribute("actionLog"));

        // multiple values -> single value
        execute("action", "param.myAction", "doGetStringArrayDefault", "aaa=111&aaa=222");
        assertArrayEquals(new String[] { "111", "222" }, (String[]) request.getAttribute("actionLog"));
    }

    @Test
    public void getFileItem() throws Exception {
        getInvocationContext("/app1", "myFile", new File(srcdir, "test.txt"), "eventSubmit_doGetFileItem", "yes");
        initRequestContext();

        moduleLoaderService.getModule("action", "param.myAction").execute();
        FileItem fi = (FileItem) newRequest.getAttribute("actionLog");

        assertEquals("test", fi.getString("UTF-8"));
    }

    @Test
    public void getFileItemAsString() throws Exception {
        getInvocationContext("/app1", "myFile", new File(srcdir, "test.txt"), "eventSubmit_doGetFileItemAsString",
                "yes");
        initRequestContext();

        moduleLoaderService.getModule("action", "param.myAction").execute();
        String fi = (String) newRequest.getAttribute("actionLog");

        assertThat(new File(fi).toURI().getPath(), containsString("config/test.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getFileItemAsList() throws Exception {
        getInvocationContext("/app1", "myFile",
                new File[] { new File(srcdir, "test.txt"), new File(srcdir, "test.txt") },
                "eventSubmit_doGetFileItemList", "yes");

        initRequestContext();

        moduleLoaderService.getModule("action", "param.myAction").execute();
        List<FileItem> fi = (List<FileItem>) newRequest.getAttribute("actionLog");

        assertEquals(2, fi.size());
        assertEquals("test", fi.get(0).getString("UTF-8"));
        assertEquals("test", fi.get(1).getString("UTF-8"));
    }

    @Test
    public void error_NoName() throws Exception {
        try {
            execute("action", "param.myActionErrorNoName", "doPerform", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing @Param's name"));
        }
    }

    @Test
    public void error_DefaultValueAndDefaultValues() throws Exception {
        try {
            execute("action", "param.myActionErrorDefaultValueAndDefaultValues", "doPerform", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("use @Param(... defaultValue=\"...\") or @Param(... defaultValues={...}):"));
        }
    }

    public static class MyRegistrar implements PropertyEditorRegistrar {
        public void registerCustomEditors(PropertyEditorRegistry registry) {
            PropertyEditor editor = new CustomNumberEditor(Long.class, true) {
                @Override
                public void setAsText(String text) {
                    if ("ten".equals(text)) {
                        setValue(10L);
                    } else {
                        super.setAsText(text);
                    }
                }
            };

            registry.registerCustomEditor(Long.class, editor);
            registry.registerCustomEditor(long.class, editor);
        }
    }
}
