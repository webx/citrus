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
package com.alibaba.citrus.util.internal.regex;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class WildcardCompilerTests {
    @Test
    public void pathNameWildcard() throws Exception {
        Pattern pattern = PathNameWildcardCompiler.compilePathName("/ab?/def/**/ghi/*.jsp");

        assertTrue(contains("/abc/def/ghi/test.jsp", pattern, "c", "", "test"));
        assertTrue(contains("/abd/def/xxx/ghi/test.jsp", pattern, "d", "xxx", "test"));
        assertTrue(contains("/abe/def/xxx/yyy/ghi/test.jsp", pattern, "e", "xxx/yyy", "test"));
        assertTrue(contains("/abf/def/ghi/.jsp", pattern, "f", "", ""));
        assertTrue(contains("/abg/def/ghi/.jsp", pattern, "g", "", ""));

        assertFalse(contains("/ab/def/ghi/test.jsp", pattern));
        assertFalse(contains("/abcd/def/ghi/test.jsp", pattern));
        assertFalse(contains("/abc/def/xxxghi/test.jsp", pattern));
        assertFalse(contains("/abc/defxxx/ghi/test.jsp", pattern));
        assertFalse(contains("/abc/def/ghi/jsp", pattern));

        pattern = PathNameWildcardCompiler.compilePathName("/xxx/yyy/**");

        assertTrue(contains("/xxx/yyy/", pattern, ""));
        assertTrue(contains("/xxx/yyy/zzz", pattern, "zzz"));
        assertTrue(contains("/xxx/yyy/zzz/aaa", pattern, "zzz/aaa"));
        assertTrue(contains("/xxx/yyy/zzz/aaa/", pattern, "zzz/aaa/"));

        assertFalse(contains("/xxx/yyy", pattern));
        assertFalse(contains("xxx/yyy", pattern));
        assertFalse(contains("xxx/yyy/zzz", pattern));
        assertFalse(contains("xxx/yyy/zzz/aaa", pattern));
        assertFalse(contains("xxx/yyy/zzz/aaa/", pattern));

        pattern = PathNameWildcardCompiler.compilePathName("/xxx/yyy");

        assertTrue(contains("/xxx/yyy", pattern));
        assertFalse(contains("/xxx/yyyzzz", pattern));

        pattern = PathNameWildcardCompiler.compilePathName("/xxx/yyy*");

        assertTrue(contains("/xxx/yyy", pattern, ""));
        assertTrue(contains("/xxx/yyyzzz", pattern, "zzz"));

        // Ãÿ ‚¥¶¿Ì
        pattern = PathNameWildcardCompiler.compilePathName("/");

        assertTrue(contains("", pattern));
        assertTrue(contains("/xxx/yyy", pattern));

        pattern = PathNameWildcardCompiler.compilePathName("");

        assertTrue(contains("", pattern));
        assertTrue(contains("/xxx/yyy", pattern));
    }

    @Test
    public void pathNameRelevant() {
        assertEquals(0, PathNameWildcardCompiler.getPathNameRelevancy(null));
        assertEquals(0, PathNameWildcardCompiler.getPathNameRelevancy("  "));
        assertEquals(0, PathNameWildcardCompiler.getPathNameRelevancy(""));

        assertEquals(0, PathNameWildcardCompiler.getPathNameRelevancy("/*/**"));
        assertEquals(1, PathNameWildcardCompiler.getPathNameRelevancy("/a?/**"));
        assertEquals(3, PathNameWildcardCompiler.getPathNameRelevancy("/a?/**/bc"));
    }

    @Test
    public void classNameWildcard() throws Exception {
        Pattern pattern = ClassNameWildcardCompiler.compileClassName("ab?.def.**.ghi.*.jsp");

        assertTrue(contains("abc.def.ghi.test.jsp", pattern, "c", "", "test"));
        assertTrue(contains("abd.def.xxx.ghi.test.jsp", pattern, "d", "xxx", "test"));
        assertTrue(contains("abe.def.xxx.yyy.ghi.test.jsp", pattern, "e", "xxx.yyy", "test"));

        assertFalse(contains("abf.def.ghi..jsp", pattern));
        assertFalse(contains("abg.def.ghi..jsp", pattern));

        assertFalse(contains("ab.def.ghi.test.jsp", pattern));
        assertFalse(contains("abcd.def.ghi.test.jsp", pattern));
        assertFalse(contains("abc.def.xxxghi.test.jsp", pattern));
        assertFalse(contains("abc.defxxx.ghi.test.jsp", pattern));
        assertFalse(contains("abc.def.ghi.jsp", pattern));

        pattern = ClassNameWildcardCompiler.compileClassName("xxx.yyy.**");

        assertTrue(contains("xxx.yyy.", pattern, ""));
        assertTrue(contains("xxx.yyy.zzz", pattern, "zzz"));
        assertTrue(contains("xxx.yyy.zzz.aaa", pattern, "zzz.aaa"));
        assertTrue(contains("xxx.yyy.zzz.aaa.", pattern, "zzz.aaa."));

        assertFalse(contains("xxx.yyy", pattern));
        assertFalse(contains("xxx.yyy", pattern));

        pattern = ClassNameWildcardCompiler.compileClassName("xxx.yyy");

        assertTrue(contains("xxx.yyy", pattern));
        assertFalse(contains("xxx.yyyzzz", pattern));

        pattern = ClassNameWildcardCompiler.compileClassName("xxx.yyy*");

        assertTrue(contains("xxx.yyy", pattern, ""));
        assertTrue(contains("xxx.yyyzzz", pattern, "zzz"));

        pattern = ClassNameWildcardCompiler.compileClassName("");

        assertTrue(contains("", pattern));
        assertTrue(contains("xxx.yyy", pattern));
    }

    @Test
    public void classNameRelevant() {
        assertEquals(0, ClassNameWildcardCompiler.getClassNameRelevancy(null));
        assertEquals(0, ClassNameWildcardCompiler.getClassNameRelevancy("  "));
        assertEquals(0, ClassNameWildcardCompiler.getClassNameRelevancy(""));

        assertEquals(0, ClassNameWildcardCompiler.getClassNameRelevancy("*.**"));
        assertEquals(1, ClassNameWildcardCompiler.getClassNameRelevancy("a?.**"));
        assertEquals(3, ClassNameWildcardCompiler.getClassNameRelevancy("a?.**.bc"));
    }

    @Test
    public void normalizePathName() {
        assertEquals(null, PathNameWildcardCompiler.normalizePathName(null));
        assertEquals("", PathNameWildcardCompiler.normalizePathName(" "));

        assertEquals("/a/b/c/", PathNameWildcardCompiler.normalizePathName(" /a\\\\b//c// "));
        assertEquals("a/b/c", PathNameWildcardCompiler.normalizePathName(" a\\\\b\\/c "));

        assertEquals("/*/**/?/", PathNameWildcardCompiler.normalizePathName(" /*\\\\**//?// "));
        assertEquals("*/**/?", PathNameWildcardCompiler.normalizePathName(" *\\\\**\\/? "));
    }

    @Test
    public void normalizeClassName() {
        assertEquals(null, ClassNameWildcardCompiler.normalizeClassName(null));
        assertEquals("", ClassNameWildcardCompiler.normalizeClassName(" "));

        assertEquals("a.b.c", ClassNameWildcardCompiler.normalizeClassName(" .a..b//c.. "));
        assertEquals("a.b.c", ClassNameWildcardCompiler.normalizeClassName(" .a..b\\/c.. "));

        assertEquals("*.**.?", ClassNameWildcardCompiler.normalizeClassName(" .*..**//?.. "));
        assertEquals("*.**.?", ClassNameWildcardCompiler.normalizeClassName(" .*..**\\/?.. "));
    }

    @Test
    public void classNameToPathName() {
        assertEquals(null, ClassNameWildcardCompiler.classNameToPathName(null));
        assertEquals("", ClassNameWildcardCompiler.classNameToPathName(" "));

        assertEquals("a/b/c", ClassNameWildcardCompiler.classNameToPathName(" .a..b//c.. "));
        assertEquals("a/b/c", ClassNameWildcardCompiler.classNameToPathName(" .a..b\\/c.. "));

        assertEquals("*/**/?", ClassNameWildcardCompiler.classNameToPathName(" .*..**//?.. "));
        assertEquals("*/**/?", ClassNameWildcardCompiler.classNameToPathName(" .*..**\\/?.. "));
    }

    @Test
    public void stress() throws Exception {
        final int concurrency = 10;
        final int loops = 10000; // 100000;
        final Pattern pattern = PathNameWildcardCompiler.compilePathName("/abc/def/**/ghi/*.jsp");

        Runnable runnable = new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();

                for (int i = 0; i < loops; i++) {
                    assertTrue(contains("/abc/def/xyz/uvw/ghi/test.jsp", pattern, "xyz/uvw", "test"));
                }

                long duration = System.currentTimeMillis() - start;

                System.out.println(Thread.currentThread().getName() + " takes " + getDuration(duration));
            }
        };

        Thread[] threads = new Thread[concurrency];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(runnable, "Thread_" + i);
        }

        long start = System.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long duration = System.currentTimeMillis() - start;

        System.out.println("Total time: " + getDuration(duration));
    }

    private static String getDuration(long duration) {
        long ms = duration % 1000;
        long secs = duration / 1000 % 60;
        long min = duration / 1000 / 60;

        return MessageFormat.format("{0,choice,0#|.1#{0,number,integer}m}" + " {1,choice,0#|.1#{1,number,integer}s}"
                + " {2,number,integer}ms", min, secs, ms);
    }

    private boolean contains(String input, Pattern pattern, String... matches) {
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            assertEquals(matches.length, matcher.groupCount());

            for (int i = 0; i < matches.length; i++) {
                assertEquals(matches[i], matcher.group(i + 1));
            }

            return true;
        } else {
            assertTrue(isEmptyArray(matches));
            return false;
        }
    }
}
