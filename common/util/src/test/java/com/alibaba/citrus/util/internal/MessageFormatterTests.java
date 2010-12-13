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
package com.alibaba.citrus.util.internal;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>MessageFormatter</code>。
 * 
 * @author Michael Zhou
 */
public class MessageFormatterTests {
    private MyMessages msgs;

    @Before
    public void init() {
        msgs = new MyMessages();
    }

    @Test
    public void formatWithLocale() {
        MessageFormatter.setDefaultLocale(Locale.CHINA);

        assertEquals("a", msgs.format(MyMessages.a));
        assertEquals("a", msgs.format(MyMessages.a, Locale.CHINA));
        assertEquals("a: world", msgs.format(MyMessages.a, Locale.CHINA, "world"));

        assertEquals("hello {0}", msgs.format(MyMessages.b));
        assertEquals("hello {0}", msgs.format(MyMessages.b, Locale.CHINA));
        assertEquals("hello world", msgs.format(MyMessages.b, Locale.CHINA, "world"));

        assertEquals("你好 {0}", msgs.format(MyMessages.c));
        assertEquals("你好 {0}", msgs.format(MyMessages.c, Locale.CHINA));
        assertEquals("你好 world", msgs.format(MyMessages.c, Locale.CHINA, "world"));

        assertEquals("你好 world", msgs.format(MyMessages.d, "world"));
        assertEquals("你好 world", msgs.format(MyMessages.d, Locale.CHINA, "world"));

        // 由于Locale.ENGLISH并没有对应的properties文件存在，所以由系统的locale来决定其结果。
        if (Locale.getDefault().equals(Locale.CHINA)) {
            assertEquals("你好 world", msgs.format(MyMessages.d, Locale.ENGLISH, "world"));
        } else {
            assertEquals("hello world", msgs.format(MyMessages.d, Locale.ENGLISH, "world"));
        }
    }

    @Test
    public void specialFormat_exception() {
        assertEquals("hello IllegalArgumentException", msgs.format(MyMessages.b, new IllegalArgumentException()));
        assertEquals("hello IllegalArgumentException - error",
                msgs.format(MyMessages.b, new IllegalArgumentException("error")));
    }

    @Test
    public void specialFormat_class() {
        assertEquals("hello Integer", msgs.format(MyMessages.b, Integer.class));
        assertEquals("hello int[]", msgs.format(MyMessages.b, int[].class));
    }

    @Test
    public void specialFormat_number() {
        assertEquals("hello NaN", msgs.format(MyMessages.b, Double.NaN));
        assertEquals("hello -Infinity", msgs.format(MyMessages.b, Double.NEGATIVE_INFINITY));
        assertEquals("hello Infinity", msgs.format(MyMessages.b, Double.POSITIVE_INFINITY));

        assertEquals("hello NaN", msgs.format(MyMessages.b, Float.NaN));
        assertEquals("hello -Infinity", msgs.format(MyMessages.b, Float.NEGATIVE_INFINITY));
        assertEquals("hello Infinity", msgs.format(MyMessages.b, Float.POSITIVE_INFINITY));
    }

    @Test
    public void specialFormat_null() {
        assertEquals("hello ", msgs.format(MyMessages.b, (Object) null));
    }

    @Test
    public void specialFormat_method() throws Exception {
        assertEquals("hello boolean String.startsWith(String, int)",
                msgs.format(MyMessages.b, String.class.getMethod("startsWith", String.class, int.class)));

        assertEquals("hello FileInputStream(String) throws FileNotFoundException",
                msgs.format(MyMessages.b, FileInputStream.class.getConstructor(String.class)));
    }
}
