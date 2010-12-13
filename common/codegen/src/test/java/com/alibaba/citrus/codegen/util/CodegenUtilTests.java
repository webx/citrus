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
package com.alibaba.citrus.codegen.util;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.util.CollectionUtil;

/**
 * ≤‚ ‘<code>CodegenUtil</code>°£
 * 
 * @author Michael Zhou
 */
public class CodegenUtilTests {

    @Test
    public void generateClassName_withClass() {
        String value;

        value = CodegenUtil.generateClassName(String.class);
        assertTrue(value.startsWith("java.lang.$String_"));

        value = CodegenUtil.generateClassName((Class<?>) null);
        assertTrue(value.startsWith("$_") && value.length() > 2);
    }

    @Test
    public void generateClassName_withClass_pacakge() {
        String value;

        value = CodegenUtil.generateClassName(String.class, "codegen");
        assertTrue(value.startsWith("codegen.$String_"));

        value = CodegenUtil.generateClassName((Class<?>) null, "codegen");
        assertTrue(value.startsWith("codegen.$_"));

        value = CodegenUtil.generateClassName(String.class, "");
        assertTrue(value.startsWith("$String_"));

        value = CodegenUtil.generateClassName((Class<?>) null, "");
        assertTrue(value.startsWith("$_") && value.length() > 2);
    }

    @Test
    public void generateClassName_withClassName() {
        String value;

        value = CodegenUtil.generateClassName("java.lang.String");
        assertTrue(value.startsWith("java.lang.$String_"));

        value = CodegenUtil.generateClassName("  java.lang.String ");
        assertTrue(value.startsWith("java.lang.$String_"));

        value = CodegenUtil.generateClassName((String) null);
        assertTrue(value.startsWith("$_") && value.length() > 2);

        value = CodegenUtil.generateClassName("  ");
        assertTrue(value.startsWith("$_") && value.length() > 2);
    }

    @Test
    public void generateClassName_withClassName_pacakge() {
        String value;

        value = CodegenUtil.generateClassName("java.lang.String", "codegen");
        assertTrue(value.startsWith("codegen.$String_"));

        value = CodegenUtil.generateClassName("  java.lang.String ", "codegen");
        assertTrue(value.startsWith("codegen.$String_"));

        value = CodegenUtil.generateClassName((String) null, "codegen");
        assertTrue(value.startsWith("codegen.$_"));

        value = CodegenUtil.generateClassName("  ", "codegen");
        assertTrue(value.startsWith("codegen.$_"));

        value = CodegenUtil.generateClassName("java.lang.String", "");
        assertTrue(value.startsWith("$String_"));

        value = CodegenUtil.generateClassName("  java.lang.String ", "");
        assertTrue(value.startsWith("$String_"));

        value = CodegenUtil.generateClassName((String) null, "");
        assertTrue(value.startsWith("$_"));

        value = CodegenUtil.generateClassName("  ", "");
        assertTrue(value.startsWith("$_"));

        value = CodegenUtil.generateClassName("", "");
        assertTrue(value.startsWith("$_"));
    }

    @Test
    public void uid() throws InterruptedException {
        final Map<String, String> names = CollectionUtil.createConcurrentHashMap();
        final int threadCount = 10;
        final int loop = 10000;

        Thread[] threads = new Thread[threadCount];
        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i < loop; i++) {
                    String name = CodegenUtil.generateClassName("");
                    names.put(name, name);
                }
            }
        };

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(runnable, "thread " + (i + 1));
        }

        long start = System.currentTimeMillis();

        for (Thread element : threads) {
            element.start();
        }

        for (Thread element : threads) {
            element.join();
        }

        System.out.printf("generateClassName duration: %,d ms\n\n", System.currentTimeMillis() - start);

        assertEquals(threadCount * loop, names.size());
    }
}
