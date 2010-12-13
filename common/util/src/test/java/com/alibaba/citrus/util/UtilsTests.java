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
package com.alibaba.citrus.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.Test;

import com.alibaba.citrus.util.SystemUtil.UserInfo;

/**
 * ≤‚ ‘<code>Utils</code>¿‡°£
 * 
 * @author Michael Zhou
 */
public class UtilsTests {
    @Test
    public void utils_math() {
        assertTrue(Utils.getUtils().containsKey("mathUtil"));

        for (String key : Utils.getUtils().keySet()) {
            Object util = Utils.getUtils().get(key);

            if (!"mathUtil".equals(key) && !"utils".equals(key)) {
                assertEquals(StringUtil.capitalize(key), ClassUtil.getSimpleClassName(util.getClass()));
            }

            System.out.println(key + " => " + ObjectUtil.identityToString(util));
        }
    }

    @Test
    public void utils_mixin() throws Exception {
        Object utils = Utils.getUtils().get("utils");
        Method m;

        // ArrayUtil.class
        m = utils.getClass().getMethod("arrayLength", Object.class);
        assertEquals(0, m.invoke(utils, new int[0]));

        // ClassLoaderUtil.class
        m = utils.getClass().getMethod("loadClass", String.class);
        assertEquals(String.class, m.invoke(utils, "java.lang.String"));

        // ClassUtil.class
        m = utils.getClass().getMethod("getFriendlyClassName", Class.class);
        assertEquals("int[]", m.invoke(utils, int[].class));

        // ExceptionUtil.class
        m = utils.getClass().getMethod("causedBy", Throwable.class, Class.class);
        assertEquals(true, m.invoke(utils, new RuntimeException(new IOException()), IOException.class));

        // FileUtil.class
        m = utils.getClass().getMethod("normalizePath", String.class);
        assertEquals("a/c", m.invoke(utils, "a/b/../c"));

        // LocaleUtil.class
        m = utils.getClass().getMethod("isLocaleSupported", Locale.class);
        assertEquals(true, m.invoke(utils, Locale.US));

        // MessageUtil.class
        m = utils.getClass().getMethod("formatMessage", String.class, Object[].class);
        assertEquals("a, b", m.invoke(utils, "{0}, {1}", new Object[] { "a", "b" }));

        // ObjectUtil.class
        m = utils.getClass().getMethod("isEmptyObject", Object.class);
        assertEquals(true, m.invoke(utils, (Object) null));

        // StreamUtil.class
        m = utils.getClass().getMethod("readText", InputStream.class, String.class, boolean.class);
        assertEquals("hello", m.invoke(utils, new ByteArrayInputStream("hello".getBytes()), "8859_1", true));

        // StringEscapeUtil.class
        m = utils.getClass().getMethod("escapeURL", String.class);
        assertEquals("+", m.invoke(utils, " "));

        // StringUtil.class
        m = utils.getClass().getMethod("isBlank", String.class);
        assertEquals(true, m.invoke(utils, " "));

        // SystemUtil.class
        m = utils.getClass().getMethod("getUserInfo");
        assertTrue(m.invoke(utils) instanceof UserInfo);

        // Math.class
        m = utils.getClass().getMethod("abs", double.class);
        assertEquals(123D, m.invoke(utils, -123D));
    }
}
