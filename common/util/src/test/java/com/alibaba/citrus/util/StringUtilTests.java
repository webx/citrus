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

import java.util.Arrays;
import java.util.Formatter;

import org.junit.Test;

/**
 * 测试<code>StringUtil</code>。
 * 
 * @author Michael Zhou
 */
public class StringUtilTests {
    // ==========================================================================
    // 基本函数。 
    //
    // 注：对于大小写敏感的isEquals方法，请使用ObjectUtil.isEquals。
    // ==========================================================================

    @Test
    public void getLength() {
        assertEquals(0, StringUtil.getLength(null));
        assertEquals(1, StringUtil.getLength(" "));
        assertEquals(3, StringUtil.getLength("abc"));
    }

    @Test
    public void isEqualsIgnoreCase() {
        assertTrue(StringUtil.isEqualsIgnoreCase(null, null));
        assertFalse(StringUtil.isEqualsIgnoreCase(null, "abc"));
        assertFalse(StringUtil.isEqualsIgnoreCase("abc", null));
        assertTrue(StringUtil.isEqualsIgnoreCase("abc", "abc"));
        assertTrue(StringUtil.isEqualsIgnoreCase("abc", "ABC"));
    }

    // ==========================================================================
    // 判空函数。 
    //  
    // 以下方法用来判定一个字符串是否为： 
    // 1. null 
    // 2. empty - "" 
    // 3. blank - "全部是空白" - 空白由Character.isWhitespace所定义。 
    // ==========================================================================

    @Test
    public void isEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("bob"));
        assertFalse(StringUtil.isEmpty("  bob  "));

        // Unicode空白
        assertFalse(StringUtil.isEmpty("\u3000"));
        assertFalse(StringUtil.isEmpty("\r\n"));
    }

    @Test
    public void isBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertFalse(StringUtil.isBlank("bob"));
        assertFalse(StringUtil.isBlank("  bob  "));

        // Unicode空白
        assertTrue(StringUtil.isBlank("\u3000"));
        assertTrue(StringUtil.isBlank("\r\n"));
    }

    // ==========================================================================
    // 默认值函数。 
    //  
    // 当字符串为empty或blank时，将字符串转换成指定的默认字符串。
    // 判断字符串为null时，可用更通用的ObjectUtil.defaultIfNull。
    // ==========================================================================

    @Test
    public void defaultIfEmpty() {
        assertEquals("default", StringUtil.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtil.defaultIfEmpty("", "default"));
        assertEquals("  ", StringUtil.defaultIfEmpty("  ", "default"));
        assertEquals("bat", StringUtil.defaultIfEmpty("bat", "default"));
    }

    @Test
    public void defaultIfBlank() {
        assertEquals("default", StringUtil.defaultIfBlank(null, "default"));
        assertEquals("default", StringUtil.defaultIfBlank("", "default"));
        assertEquals("default", StringUtil.defaultIfBlank("  ", "default"));
        assertEquals("bat", StringUtil.defaultIfBlank("bat", "default"));
    }

    // ==========================================================================
    // 去空白的函数。 
    //  
    // 以下方法用来除去一个字串首尾的空白。 
    // ==========================================================================

    @Test
    public void trim() {
        assertNull(StringUtil.trim(null));
        assertEquals("", StringUtil.trim(""));
        assertEquals("", StringUtil.trim("     "));
        assertEquals("abc", StringUtil.trim("abc"));
        assertEquals("abc", StringUtil.trim("    abc    "));
    }

    @Test
    public void trimToNull() {
        String abc = "abc";

        assertNull(StringUtil.trimToNull(null));
        assertNull(StringUtil.trimToNull(""));
        assertNull(StringUtil.trimToNull("     "));
        assertSame(abc, StringUtil.trimToNull(abc)); // 字符串原样返回，确保效率
        assertEquals("abc", StringUtil.trimToNull("    abc    "));
    }

    @Test
    public void trimToEmpty() {
        String abc = "abc";

        assertEquals("", StringUtil.trimToEmpty(null));
        assertEquals("", StringUtil.trimToEmpty(""));
        assertEquals("", StringUtil.trimToEmpty("     "));
        assertSame(abc, StringUtil.trimToEmpty(abc)); // 字符串原样返回，确保效率
        assertEquals("abc", StringUtil.trimToEmpty("    abc    "));
    }

    @Test
    public void trim_chars() {
        String abc = "abc";

        assertNull(StringUtil.trim(null, null));
        assertEquals("", StringUtil.trim("", null));
        assertEquals("", StringUtil.trim("     ", null));
        assertSame(abc, StringUtil.trim(abc, null)); // 字符串原样返回，确保效率
        assertEquals("abc", StringUtil.trim("    abc    ", null));

        // Unicode空白
        assertEquals("abc", StringUtil.trim("\u3000abc\r\n", null));

        // 两个参数
        assertNull(StringUtil.trim(null, "xyz"));
        assertEquals("", StringUtil.trim("", "xyz"));
        assertEquals(abc, StringUtil.trim(abc, null));
        assertEquals("abc", StringUtil.trim("  abc", null));
        assertEquals("abc", StringUtil.trim("abc  ", null));
        assertEquals("abc", StringUtil.trim(" abc ", null));
        assertEquals("  abc", StringUtil.trim("  abcyx", "xyz"));
        assertEquals("  abcyx", StringUtil.trim("  abcyx", ""));
    }

    @Test
    public void trimStart() {
        String abc = "abc";

        assertNull(StringUtil.trimStart(null));
        assertEquals("", StringUtil.trimStart(""));
        assertSame(abc, StringUtil.trimStart(abc));
        assertEquals("abc", StringUtil.trimStart("  abc"));
        assertEquals("abc  ", StringUtil.trimStart("abc  "));
        assertEquals("abc ", StringUtil.trimStart(" abc "));

        // Unicode空白
        assertEquals("abc\u3000", StringUtil.trimStart("\u3000abc\u3000"));

        // 两个参数
        assertNull(StringUtil.trimStart(null, "xyz"));
        assertEquals("", StringUtil.trimStart("", "xyz"));
        assertEquals(abc, StringUtil.trimStart(abc, null));
        assertEquals("abc", StringUtil.trimStart("  abc", null));
        assertEquals("abc  ", StringUtil.trimStart("abc  ", null));
        assertEquals("abc ", StringUtil.trimStart(" abc ", null));
        assertEquals(" abc ", StringUtil.trimStart(" abc ", ""));
        assertEquals("abcx", StringUtil.trimStart("xxabcx", "xyz"));

        abc = "  abcyx";
        assertEquals(abc, StringUtil.trimStart(abc, "xyz"));
    }

    @Test
    public void trimEnd() {
        String abc = "abc";

        assertNull(StringUtil.trimEnd(null));
        assertEquals("", StringUtil.trimEnd(""));
        assertSame(abc, StringUtil.trimEnd(abc));
        assertEquals("  abc", StringUtil.trimEnd("  abc"));
        assertEquals("abc", StringUtil.trimEnd("abc  "));
        assertEquals(" abc", StringUtil.trimEnd(" abc "));

        // Unicode空白
        assertEquals("\u3000abc", StringUtil.trimEnd("\u3000abc\u3000"));

        // 两个参数
        assertNull(StringUtil.trimEnd(null, "xyz"));
        assertEquals("", StringUtil.trimEnd("", "xyz"));
        assertEquals(abc, StringUtil.trimEnd(abc, null));
        assertEquals("  abc", StringUtil.trimEnd("  abc", null));
        assertEquals("abc", StringUtil.trimEnd("abc  ", null));
        assertEquals(" abc", StringUtil.trimEnd(" abc ", null));
        assertEquals("  abc", StringUtil.trimEnd("  abcyx", "xyz"));
        assertEquals("  abcyx", StringUtil.trimEnd("  abcyx", ""));
    }

    @Test
    public void trimToNull_chars() {
        String abc = "abc";

        assertNull(StringUtil.trimToNull(null, null));
        assertNull(StringUtil.trimToNull("", null));
        assertNull(StringUtil.trimToNull("     ", null));
        assertSame(abc, StringUtil.trimToNull(abc, null)); // 字符串原样返回，确保效率
        assertEquals("abc", StringUtil.trimToNull("    abc    ", null));

        // Unicode空白
        assertEquals("abc", StringUtil.trimToNull("\u3000abc\r\n", null));

        // 两个参数
        assertNull(StringUtil.trimToNull(null, "xyz"));
        assertNull(StringUtil.trimToNull("", "xyz"));
        assertEquals(abc, StringUtil.trimToNull(abc, null));
        assertEquals("abc", StringUtil.trimToNull("  abc", null));
        assertEquals("abc", StringUtil.trimToNull("abc  ", null));
        assertEquals("abc", StringUtil.trimToNull(" abc ", null));
        assertEquals("  abc", StringUtil.trimToNull("  abcyx", "xyz"));
    }

    @Test
    public void trimToEmpty_chars() {
        String abc = "abc";

        assertEquals("", StringUtil.trimToEmpty(null, null));
        assertEquals("", StringUtil.trimToEmpty("", null));
        assertEquals("", StringUtil.trimToEmpty("     ", null));
        assertSame(abc, StringUtil.trimToEmpty(abc, null)); // 字符串原样返回，确保效率
        assertEquals("abc", StringUtil.trimToEmpty("    abc    ", null));

        // Unicode空白
        assertEquals("abc", StringUtil.trimToEmpty("\u3000abc\r\n", null));

        // 两个参数
        assertEquals("", StringUtil.trimToEmpty(null, "xyz"));
        assertEquals("", StringUtil.trimToEmpty("", "xyz"));
        assertEquals(abc, StringUtil.trimToEmpty(abc, null));
        assertEquals("abc", StringUtil.trimToEmpty("  abc", null));
        assertEquals("abc", StringUtil.trimToEmpty("abc  ", null));
        assertEquals("abc", StringUtil.trimToEmpty(" abc ", null));
        assertEquals("  abc", StringUtil.trimToEmpty("  abcyx", "xyz"));
    }

    // ==========================================================================
    // 大小写转换。 
    // ==========================================================================

    @Test
    public void capitalize() {
        assertNull(StringUtil.capitalize(null));
        assertEquals("", StringUtil.capitalize(""));
        assertEquals("Cat", StringUtil.capitalize("cat"));
        assertEquals("CAt", StringUtil.capitalize("cAt"));
    }

    @Test
    public void uncapitalize() {
        assertNull(StringUtil.uncapitalize(null));
        assertEquals("", StringUtil.uncapitalize(""));
        assertEquals("cat", StringUtil.uncapitalize("Cat"));
        assertEquals("CAT", StringUtil.uncapitalize("CAT"));
    }

    @Test
    public void swapCase() {
        assertNull(StringUtil.swapCase(null));
        assertEquals("", StringUtil.swapCase(""));
        assertEquals("tHE DOG HAS A bone", StringUtil.swapCase("The dog has a BONE"));

        char titleCaseChar = '\u01C5';
        char lowerCaseChar = '\u01C6';

        assertEquals(lowerCaseChar + "ABCdEF", StringUtil.swapCase(titleCaseChar + "abcDef"));
    }

    @Test
    public void toUpperCase() {
        assertNull(StringUtil.toUpperCase(null));
        assertEquals("", StringUtil.toUpperCase(""));
        assertEquals("ABC", StringUtil.toUpperCase("aBc"));
    }

    @Test
    public void toLowerCase() {
        assertNull(StringUtil.toLowerCase(null));
        assertEquals("", StringUtil.toLowerCase(""));
        assertEquals("abc", StringUtil.toLowerCase("aBc"));
    }

    @Test
    public void toCamelCase() {
        assertNull(StringUtil.toCamelCase(null));
        assertEquals("", StringUtil.toCamelCase(""));

        assertEquals("a", StringUtil.toCamelCase("A"));
        assertEquals("a", StringUtil.toCamelCase("a"));

        assertEquals("ab", StringUtil.toCamelCase("AB"));
        assertEquals("ab", StringUtil.toCamelCase("Ab"));
        assertEquals("aB", StringUtil.toCamelCase("aB"));
        assertEquals("ab", StringUtil.toCamelCase("ab"));

        assertEquals("aB", StringUtil.toCamelCase("A_B"));
        assertEquals("aB", StringUtil.toCamelCase("A_b"));
        assertEquals("aB", StringUtil.toCamelCase("a_B"));
        assertEquals("aB", StringUtil.toCamelCase("a_b"));

        assertEquals("aBc", StringUtil.toCamelCase("aBc"));
        assertEquals("aBcDef", StringUtil.toCamelCase("  aBc def "));
        assertEquals("aBcDef", StringUtil.toCamelCase("aBcDef"));
        assertEquals("aBcDefGhi", StringUtil.toCamelCase("aBc def_ghi"));
        assertEquals("aBcDefGhi", StringUtil.toCamelCase("aBcDefGhi"));
        assertEquals("aBcDefGhi123", StringUtil.toCamelCase("aBc def_ghi 123"));
        assertEquals("aBcDefGhi123", StringUtil.toCamelCase("aBcDefGhi123"));
        assertEquals("aBcDefGhi123", StringUtil.toCamelCase("aBcDEFGhi123"));

        assertEquals("123ABcDefGhi", StringUtil.toCamelCase("123aBcDEFGhi")); // 数字开始

        // 不保留下划线
        assertEquals("ab", StringUtil.toCamelCase("__AB__"));
        assertEquals("ab", StringUtil.toCamelCase("__Ab__"));
        assertEquals("aB", StringUtil.toCamelCase("__aB__"));
        assertEquals("ab", StringUtil.toCamelCase("__ab__"));

        assertEquals("aB", StringUtil.toCamelCase("__A__B__"));
        assertEquals("aB", StringUtil.toCamelCase("__A__b__"));
        assertEquals("aB", StringUtil.toCamelCase("__a__B__"));
        assertEquals("aB", StringUtil.toCamelCase("__a__b__"));

        // 保留除下划线以外的其它分隔符
        assertEquals("..ab..", StringUtil.toCamelCase("..AB.."));
        assertEquals("..ab..", StringUtil.toCamelCase("..Ab.."));
        assertEquals("..aB..", StringUtil.toCamelCase("..aB.."));
        assertEquals("..ab..", StringUtil.toCamelCase("..ab.."));

        assertEquals("..a..b..", StringUtil.toCamelCase("..A..B.."));
        assertEquals("..a..b..", StringUtil.toCamelCase("..A..b.."));
        assertEquals("..a..b..", StringUtil.toCamelCase("..a..B.."));
        assertEquals("..a..b..", StringUtil.toCamelCase("..a..b.."));

        assertEquals("..a..123B..", StringUtil.toCamelCase("..A..123B.."));
        assertEquals("..a..123B..", StringUtil.toCamelCase("..A..123b.."));
        assertEquals("..a..123B..", StringUtil.toCamelCase("..a..123B.."));
        assertEquals("..a..123B..", StringUtil.toCamelCase("..a..123b.."));

        assertEquals("fmh.m.0.n", StringUtil.toCamelCase("_fmh.m._0.n"));
        assertEquals("aaa-bbb-ccc", StringUtil.toCamelCase("aaa-bbb-ccc"));
    }

    @Test
    public void toPascalCase() {
        assertNull(StringUtil.toPascalCase(null));
        assertEquals("", StringUtil.toPascalCase(""));

        assertEquals("A", StringUtil.toPascalCase("A"));
        assertEquals("A", StringUtil.toPascalCase("a"));

        assertEquals("Ab", StringUtil.toPascalCase("AB"));
        assertEquals("Ab", StringUtil.toPascalCase("Ab"));
        assertEquals("AB", StringUtil.toPascalCase("aB"));
        assertEquals("Ab", StringUtil.toPascalCase("ab"));

        assertEquals("AB", StringUtil.toPascalCase("A_B"));
        assertEquals("AB", StringUtil.toPascalCase("A_b"));
        assertEquals("AB", StringUtil.toPascalCase("a_B"));
        assertEquals("AB", StringUtil.toPascalCase("a_b"));

        assertEquals("ABc", StringUtil.toPascalCase("aBc"));
        assertEquals("ABcDef", StringUtil.toPascalCase("  aBc def "));
        assertEquals("ABcDef", StringUtil.toPascalCase("aBcDef"));
        assertEquals("ABcDefGhi", StringUtil.toPascalCase("aBc def_ghi"));
        assertEquals("ABcDefGhi", StringUtil.toPascalCase("aBcDefGhi"));
        assertEquals("ABcDefGhi123", StringUtil.toPascalCase("aBc def_ghi 123"));
        assertEquals("ABcDefGhi123", StringUtil.toPascalCase("aBcDefGhi123"));
        assertEquals("ABcDefGhi123", StringUtil.toPascalCase("aBcDEFGhi123"));

        assertEquals("123ABcDefGhi", StringUtil.toPascalCase("123aBcDEFGhi")); // 数字开始

        // 不保留下划线
        assertEquals("Ab", StringUtil.toPascalCase("__AB__"));
        assertEquals("Ab", StringUtil.toPascalCase("__Ab__"));
        assertEquals("AB", StringUtil.toPascalCase("__aB__"));
        assertEquals("Ab", StringUtil.toPascalCase("__ab__"));

        assertEquals("AB", StringUtil.toPascalCase("__A__B__"));
        assertEquals("AB", StringUtil.toPascalCase("__A__b__"));
        assertEquals("AB", StringUtil.toPascalCase("__a__B__"));
        assertEquals("AB", StringUtil.toPascalCase("__a__b__"));

        // 保留除下划线以外的其它分隔符
        assertEquals("..Ab..", StringUtil.toPascalCase("..AB.."));
        assertEquals("..Ab..", StringUtil.toPascalCase("..Ab.."));
        assertEquals("..AB..", StringUtil.toPascalCase("..aB.."));
        assertEquals("..Ab..", StringUtil.toPascalCase("..ab.."));

        assertEquals("..A..B..", StringUtil.toPascalCase("..A..B.."));
        assertEquals("..A..B..", StringUtil.toPascalCase("..A..b.."));
        assertEquals("..A..B..", StringUtil.toPascalCase("..a..B.."));
        assertEquals("..A..B..", StringUtil.toPascalCase("..a..b.."));

        assertEquals("..A..123B..", StringUtil.toPascalCase("..A..123B.."));
        assertEquals("..A..123B..", StringUtil.toPascalCase("..A..123b.."));
        assertEquals("..A..123B..", StringUtil.toPascalCase("..a..123B.."));
        assertEquals("..A..123B..", StringUtil.toPascalCase("..a..123b.."));

        assertEquals("Fmh.M.0.N", StringUtil.toPascalCase("_fmh.m._0.n"));
        assertEquals("Aaa-Bbb-Ccc", StringUtil.toPascalCase("aaa-bbb-ccc"));
    }

    @Test
    public void toUpperCaseWithUnderscores() {
        assertNull(StringUtil.toUpperCaseWithUnderscores(null));
        assertEquals("", StringUtil.toUpperCaseWithUnderscores(""));

        assertEquals("A", StringUtil.toUpperCaseWithUnderscores("A"));
        assertEquals("A", StringUtil.toUpperCaseWithUnderscores("a"));

        assertEquals("AB", StringUtil.toUpperCaseWithUnderscores("AB"));
        assertEquals("AB", StringUtil.toUpperCaseWithUnderscores("Ab"));
        assertEquals("A_B", StringUtil.toUpperCaseWithUnderscores("aB"));
        assertEquals("AB", StringUtil.toUpperCaseWithUnderscores("ab"));

        assertEquals("A_B", StringUtil.toUpperCaseWithUnderscores("A_B"));
        assertEquals("A_B", StringUtil.toUpperCaseWithUnderscores("A_b"));
        assertEquals("A_B", StringUtil.toUpperCaseWithUnderscores("a_B"));
        assertEquals("A_B", StringUtil.toUpperCaseWithUnderscores("a_b"));

        assertEquals("A_BC", StringUtil.toUpperCaseWithUnderscores("aBc"));
        assertEquals("A_BC_DEF", StringUtil.toUpperCaseWithUnderscores("  aBc def "));
        assertEquals("A_BC_DEF", StringUtil.toUpperCaseWithUnderscores("aBcDef"));
        assertEquals("A_BC_DEF_GHI", StringUtil.toUpperCaseWithUnderscores("aBc def_ghi"));
        assertEquals("A_BC_DEF_GHI", StringUtil.toUpperCaseWithUnderscores("aBcDefGhi"));
        assertEquals("A_BC_DEF_GHI_123", StringUtil.toUpperCaseWithUnderscores("aBc def_ghi 123"));
        assertEquals("A_BC_DEF_GHI_123", StringUtil.toUpperCaseWithUnderscores("aBcDefGhi123"));
        assertEquals("A_BC_DEF_GHI_123", StringUtil.toUpperCaseWithUnderscores("aBcDEFGhi123"));

        assertEquals("123_A_BC_DEF_GHI", StringUtil.toUpperCaseWithUnderscores("123aBcDEFGhi")); // 数字开始

        // 保留下划线
        assertEquals("__AB__", StringUtil.toUpperCaseWithUnderscores("__AB__"));
        assertEquals("__AB__", StringUtil.toUpperCaseWithUnderscores("__Ab__"));
        assertEquals("__A_B__", StringUtil.toUpperCaseWithUnderscores("__aB__"));
        assertEquals("__AB__", StringUtil.toUpperCaseWithUnderscores("__ab__"));

        assertEquals("__A__B__", StringUtil.toUpperCaseWithUnderscores("__A__B__"));
        assertEquals("__A__B__", StringUtil.toUpperCaseWithUnderscores("__A__b__"));
        assertEquals("__A__B__", StringUtil.toUpperCaseWithUnderscores("__a__B__"));
        assertEquals("__A__B__", StringUtil.toUpperCaseWithUnderscores("__a__b__"));

        // 保留所有的分隔符
        assertEquals("..AB..", StringUtil.toUpperCaseWithUnderscores("..AB.."));
        assertEquals("..AB..", StringUtil.toUpperCaseWithUnderscores("..Ab.."));
        assertEquals("..A_B..", StringUtil.toUpperCaseWithUnderscores("..aB.."));
        assertEquals("..AB..", StringUtil.toUpperCaseWithUnderscores("..ab.."));

        assertEquals("..A..B..", StringUtil.toUpperCaseWithUnderscores("..A..B.."));
        assertEquals("..A..B..", StringUtil.toUpperCaseWithUnderscores("..A..b.."));
        assertEquals("..A..B..", StringUtil.toUpperCaseWithUnderscores("..a..B.."));
        assertEquals("..A..B..", StringUtil.toUpperCaseWithUnderscores("..a..b.."));

        assertEquals("..A..123_B..", StringUtil.toUpperCaseWithUnderscores("..A..123B.."));
        assertEquals("..A..123_B..", StringUtil.toUpperCaseWithUnderscores("..A..123b.."));
        assertEquals("..A..123_B..", StringUtil.toUpperCaseWithUnderscores("..a..123B.."));
        assertEquals("..A..123_B..", StringUtil.toUpperCaseWithUnderscores("..a..123b.."));

        assertEquals("_FMH.M._0.N", StringUtil.toUpperCaseWithUnderscores("_fmh.m._0.n"));
        assertEquals("AAA-BBB-CCC", StringUtil.toUpperCaseWithUnderscores("aaa-bbb-ccc"));
    }

    @Test
    public void toLowerCaseWithUnderscores() {
        assertNull(StringUtil.toLowerCaseWithUnderscores(null));
        assertEquals("", StringUtil.toLowerCaseWithUnderscores(""));

        assertEquals("a", StringUtil.toLowerCaseWithUnderscores("A"));
        assertEquals("a", StringUtil.toLowerCaseWithUnderscores("a"));

        assertEquals("ab", StringUtil.toLowerCaseWithUnderscores("AB"));
        assertEquals("ab", StringUtil.toLowerCaseWithUnderscores("Ab"));
        assertEquals("a_b", StringUtil.toLowerCaseWithUnderscores("aB"));
        assertEquals("ab", StringUtil.toLowerCaseWithUnderscores("ab"));

        assertEquals("a_b", StringUtil.toLowerCaseWithUnderscores("A_B"));
        assertEquals("a_b", StringUtil.toLowerCaseWithUnderscores("A_b"));
        assertEquals("a_b", StringUtil.toLowerCaseWithUnderscores("a_B"));
        assertEquals("a_b", StringUtil.toLowerCaseWithUnderscores("a_b"));

        assertEquals("a_bc", StringUtil.toLowerCaseWithUnderscores("aBc"));
        assertEquals("a_bc_def", StringUtil.toLowerCaseWithUnderscores("  aBc def "));
        assertEquals("a_bc_def", StringUtil.toLowerCaseWithUnderscores("aBcDef"));
        assertEquals("a_bc_def_ghi", StringUtil.toLowerCaseWithUnderscores("aBc def_ghi"));
        assertEquals("a_bc_def_ghi", StringUtil.toLowerCaseWithUnderscores("aBcDefGhi"));
        assertEquals("a_bc_def_ghi_123", StringUtil.toLowerCaseWithUnderscores("aBc def_ghi 123"));
        assertEquals("a_bc_def_ghi_123", StringUtil.toLowerCaseWithUnderscores("aBcDefGhi123"));
        assertEquals("a_bc_def_ghi_123", StringUtil.toLowerCaseWithUnderscores("aBcDEFGhi123"));

        assertEquals("123_a_bc_def_ghi", StringUtil.toLowerCaseWithUnderscores("123aBcDEFGhi")); // 数字开始

        // 保留下划线
        assertEquals("__ab__", StringUtil.toLowerCaseWithUnderscores("__AB__"));
        assertEquals("__ab__", StringUtil.toLowerCaseWithUnderscores("__Ab__"));
        assertEquals("__a_b__", StringUtil.toLowerCaseWithUnderscores("__aB__"));
        assertEquals("__ab__", StringUtil.toLowerCaseWithUnderscores("__ab__"));

        assertEquals("__a__b__", StringUtil.toLowerCaseWithUnderscores("__A__B__"));
        assertEquals("__a__b__", StringUtil.toLowerCaseWithUnderscores("__A__b__"));
        assertEquals("__a__b__", StringUtil.toLowerCaseWithUnderscores("__a__B__"));
        assertEquals("__a__b__", StringUtil.toLowerCaseWithUnderscores("__a__b__"));

        // 保留所有的分隔符
        assertEquals("..ab..", StringUtil.toLowerCaseWithUnderscores("..AB.."));
        assertEquals("..ab..", StringUtil.toLowerCaseWithUnderscores("..Ab.."));
        assertEquals("..a_b..", StringUtil.toLowerCaseWithUnderscores("..aB.."));
        assertEquals("..ab..", StringUtil.toLowerCaseWithUnderscores("..ab.."));

        assertEquals("..a..b..", StringUtil.toLowerCaseWithUnderscores("..A..B.."));
        assertEquals("..a..b..", StringUtil.toLowerCaseWithUnderscores("..A..b.."));
        assertEquals("..a..b..", StringUtil.toLowerCaseWithUnderscores("..a..B.."));
        assertEquals("..a..b..", StringUtil.toLowerCaseWithUnderscores("..a..b.."));

        assertEquals("..a..123_b..", StringUtil.toLowerCaseWithUnderscores("..A..123B.."));
        assertEquals("..a..123_b..", StringUtil.toLowerCaseWithUnderscores("..A..123b.."));
        assertEquals("..a..123_b..", StringUtil.toLowerCaseWithUnderscores("..a..123B.."));
        assertEquals("..a..123_b..", StringUtil.toLowerCaseWithUnderscores("..a..123b.."));

        assertEquals("_fmh.m._0.n", StringUtil.toLowerCaseWithUnderscores("_fmh.m._0.n"));
        assertEquals("aaa-bbb-ccc", StringUtil.toLowerCaseWithUnderscores("aaa-bbb-ccc"));
    }

    // ==========================================================================
    // 字符串分割函数。 
    //  
    // 将字符串按指定分隔符分割。 
    // ==========================================================================

    @Test
    public void splitChar() {
        assertNull(StringUtil.split(null, '.'));
        assertArrayEquals(new String[0], StringUtil.split("", '.'));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtil.split("a.b.c", '.'));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtil.split("a..b.c", '.'));
        assertArrayEquals(new String[] { "a:b:c" }, StringUtil.split("a:b:c", '.'));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtil.split("a b c", ' '));
    }

    @Test
    public void splitString() {
        assertNull(StringUtil.split(null, ":"));
        assertArrayEquals(new String[0], StringUtil.split("", ":"));
        assertArrayEquals(new String[] { "abc", "def" }, StringUtil.split("abc def", null));
        assertArrayEquals(new String[] { "abc", "def" }, StringUtil.split("abc def", " "));
        assertArrayEquals(new String[] { "abc", "def" }, StringUtil.split("abc  def", " "));
        assertArrayEquals(new String[] { "ab", "cd", "ef" }, StringUtil.split(" ab:  cd::ef  ", ": "));
        assertArrayEquals(new String[] { "abc.def" }, StringUtil.split("abc.def", ""));
    }

    @Test
    public void splitStringMax() {
        assertNull(StringUtil.split(null, null, 0));
        assertArrayEquals(new String[0], StringUtil.split("", null, 0));

        assertArrayEquals(new String[] { "ab", "cd", "ef" }, StringUtil.split("ab cd ef", null, 0));
        assertArrayEquals(new String[] { "ab", "cd", "ef" }, StringUtil.split("  ab   cd ef  ", null, 0));
        assertArrayEquals(new String[] { "ab", "cd ef  " }, StringUtil.split("  ab   cd ef  ", null, 2));

        assertArrayEquals(new String[] { "ab", "cd", "ef" }, StringUtil.split("ab:cd::ef", ":", 0));
        assertArrayEquals(new String[] { "ab", "cd:ef" }, StringUtil.split("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[] { "abc.def" }, StringUtil.split("abc.def", "", 2));

        assertArrayEquals(new String[] { "ab", "cd: ef" }, StringUtil.split("ab: cd: ef", ": ", 2));
    }

    // ==========================================================================
    // 字符串连接函数。 
    //  
    // 将多个对象按指定分隔符连接成字符串。 
    // ==========================================================================

    @Test
    public void joinArray() {
        assertNull(StringUtil.join((Object[]) null, ";"));
        assertEquals("", StringUtil.join(new String[0], ";"));
        assertEquals("", StringUtil.join(new String[] { null }, ";"));
        assertEquals("a--b--c", StringUtil.join(new String[] { "a", "b", "c" }, "--"));
        assertEquals("abc", StringUtil.join(new String[] { "a", "b", "c" }, null));
        assertEquals("abc", StringUtil.join(new String[] { "a", "b", "c" }, ""));
        assertEquals(",,a", StringUtil.join(new String[] { null, "", "a" }, ","));
    }

    @Test
    public void joinIterable() {
        assertNull(StringUtil.join((Iterable<?>) null, ";"));
        assertEquals("", StringUtil.join(Arrays.asList(new String[0]), ";"));
        assertEquals("", StringUtil.join(Arrays.asList(new String[] { null }), ";"));
        assertEquals("a--b--c", StringUtil.join(Arrays.asList(new String[] { "a", "b", "c" }), "--"));
        assertEquals("abc", StringUtil.join(Arrays.asList(new String[] { "a", "b", "c" }), null));
        assertEquals("abc", StringUtil.join(Arrays.asList(new String[] { "a", "b", "c" }), ""));
        assertEquals(",,a", StringUtil.join(Arrays.asList(new String[] { null, "", "a" }), ","));
    }

    // ==========================================================================
    // 字符串查找函数 —— 字符或字符串。  
    // ==========================================================================
    @Test
    public void indexOfChar() {
        assertEquals(-1, StringUtil.indexOf(null, 'a'));
        assertEquals(-1, StringUtil.indexOf("", 'a'));
        assertEquals(0, StringUtil.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtil.indexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtil.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtil.indexOf("", 'a', 0));
        assertEquals(2, StringUtil.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtil.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtil.indexOf("aabaabaa", 'b', 9));
        assertEquals(2, StringUtil.indexOf("aabaabaa", 'b', -1));
    }

    @Test
    public void indexOfString() {
        assertEquals(-1, StringUtil.indexOf(null, "a"));
        assertEquals(-1, StringUtil.indexOf("aaa", null));
        assertEquals(0, StringUtil.indexOf("", ""));
        assertEquals(0, StringUtil.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtil.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtil.indexOf("aabaabaa", "ab"));
        assertEquals(0, StringUtil.indexOf("aabaabaa", ""));

        assertEquals(-1, StringUtil.indexOf(null, "a", 0));
        assertEquals(-1, StringUtil.indexOf("aaa", null, 0));
        assertEquals(0, StringUtil.indexOf("", "", 0));
        assertEquals(0, StringUtil.indexOf("aabaabaa", "a", 0));
        assertEquals(2, StringUtil.indexOf("aabaabaa", "b", 0));
        assertEquals(1, StringUtil.indexOf("aabaabaa", "ab", 0));
        assertEquals(5, StringUtil.indexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtil.indexOf("aabaabaa", "b", 9));
        assertEquals(2, StringUtil.indexOf("aabaabaa", "b", -1));
        assertEquals(2, StringUtil.indexOf("aabaabaa", "", 2));
        assertEquals(3, StringUtil.indexOf("abc", "", 9));
    }

    @Test
    public void lastIndexOfChar() {
        assertEquals(-1, StringUtil.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtil.lastIndexOf("", 'a'));
        assertEquals(7, StringUtil.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtil.lastIndexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtil.lastIndexOf(null, 'a', 0));
        assertEquals(-1, StringUtil.lastIndexOf("", 'a', 0));
        assertEquals(5, StringUtil.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtil.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtil.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtil.lastIndexOf("aabaabaa", 'b', 9));
        assertEquals(-1, StringUtil.lastIndexOf("aabaabaa", 'b', -1));
        assertEquals(0, StringUtil.lastIndexOf("aabaabaa", 'a', 0));
    }

    @Test
    public void lastIndexOfString() {
        assertEquals(-1, StringUtil.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtil.lastIndexOf("abc", null));
        assertEquals(-1, StringUtil.lastIndexOf("", "a"));
        assertEquals(7, StringUtil.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringUtil.lastIndexOf("aabaabaa", "b"));

        assertEquals(-1, StringUtil.lastIndexOf(null, "a", 0));
        assertEquals(-1, StringUtil.lastIndexOf("aaa", null, 0));
        assertEquals(7, StringUtil.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(5, StringUtil.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(4, StringUtil.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(5, StringUtil.lastIndexOf("aabaabaa", "b", 9));
        assertEquals(-1, StringUtil.lastIndexOf("aabaabaa", "b", -1));
        assertEquals(0, StringUtil.lastIndexOf("aabaabaa", "a", 0));
        assertEquals(-1, StringUtil.lastIndexOf("aabaabaa", "b", 0));
    }

    @Test
    public void indexOfAnyChar() {
        assertEquals(-1, StringUtil.indexOfAny(null, "a".toCharArray()));
        assertEquals(-1, StringUtil.indexOfAny("", "a".toCharArray()));
        assertEquals(-1, StringUtil.indexOfAny("abc", (char[]) null));
        assertEquals(-1, StringUtil.indexOfAny("abc", "".toCharArray()));
        assertEquals(0, StringUtil.indexOfAny("zzabyycdxx", "za".toCharArray()));
        assertEquals(3, StringUtil.indexOfAny("zzabyycdxx", "by".toCharArray()));
        assertEquals(-1, StringUtil.indexOfAny("aba", "z".toCharArray()));
    }

    @Test
    public void indexOfAnyString() {
        assertEquals(-1, StringUtil.indexOfAny(null, "a"));
        assertEquals(-1, StringUtil.indexOfAny("", "a"));
        assertEquals(-1, StringUtil.indexOfAny("abc", (String) null));
        assertEquals(-1, StringUtil.indexOfAny("abc", ""));
        assertEquals(0, StringUtil.indexOfAny("zzabyycdxx", "za"));
        assertEquals(3, StringUtil.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtil.indexOfAny("aba", "z"));
    }

    @Test
    public void indexOfAnyStrings() {
        assertEquals(-1, StringUtil.indexOfAny(null, new String[1]));
        assertEquals(-1, StringUtil.indexOfAny("abc", (String[]) null));
        assertEquals(-1, StringUtil.indexOfAny("abc", new String[0]));
        assertEquals(2, StringUtil.indexOfAny("zzabyycdxx", new String[] { "ab", "cd" }));
        assertEquals(2, StringUtil.indexOfAny("zzabyycdxx", new String[] { "cd", "ab" }));
        assertEquals(-1, StringUtil.indexOfAny("zzabyycdxx", new String[] { "mn", "op" }));
        assertEquals(1, StringUtil.indexOfAny("zzabyycdxx", new String[] { "zab", "aby" }));
        assertEquals(1, StringUtil.indexOfAny("zzabyycdxx", new String[] { null, "zab", "aby" }));
        assertEquals(0, StringUtil.indexOfAny("zzabyycdxx", new String[] { "" }));
        assertEquals(0, StringUtil.indexOfAny("", new String[] { "" }));
        assertEquals(-1, StringUtil.indexOfAny("", new String[] { "a" }));
    }

    @Test
    public void indexOfAnyButChar() {
        assertEquals(-1, StringUtil.indexOfAnyBut(null, "a".toCharArray()));
        assertEquals(-1, StringUtil.indexOfAnyBut("", "a".toCharArray()));
        assertEquals(-1, StringUtil.indexOfAnyBut("a", (char[]) null));
        assertEquals(-1, StringUtil.indexOfAnyBut("a", "".toCharArray()));
        assertEquals(3, StringUtil.indexOfAnyBut("zzabyycdxx", "za".toCharArray()));
        assertEquals(0, StringUtil.indexOfAnyBut("zzabyycdxx", "by".toCharArray()));
        assertEquals(-1, StringUtil.indexOfAnyBut("aba", "ab".toCharArray()));
    }

    @Test
    public void indexOfAnyButString() {
        assertEquals(-1, StringUtil.indexOfAnyBut(null, "a"));
        assertEquals(-1, StringUtil.indexOfAnyBut("", "a"));
        assertEquals(-1, StringUtil.indexOfAnyBut("a", (String) null));
        assertEquals(-1, StringUtil.indexOfAnyBut("a", ""));
        assertEquals(3, StringUtil.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(0, StringUtil.indexOfAnyBut("zzabyycdxx", "by"));
        assertEquals(-1, StringUtil.indexOfAnyBut("aba", "ab"));
    }

    @Test
    public void lastIndexAnyStrings() {
        assertEquals(-1, StringUtil.lastIndexOfAny(null, new String[1]));
        assertEquals(-1, StringUtil.lastIndexOfAny("abc", (String[]) null));
        assertEquals(-1, StringUtil.lastIndexOfAny("abc", new String[0]));
        assertEquals(-1, StringUtil.lastIndexOfAny("abc", new String[] { null }));
        assertEquals(6, StringUtil.lastIndexOfAny("zzabyycdxx", new String[] { "ab", "cd" }));
        assertEquals(6, StringUtil.lastIndexOfAny("zzabyycdxx", new String[] { "cd", "ab" }));
        assertEquals(-1, StringUtil.lastIndexOfAny("zzabyycdxx", new String[] { "mn", "op" }));
        assertEquals(-1, StringUtil.lastIndexOfAny("zzabyycdxx", new String[] { "mn", "op" }));
        assertEquals(10, StringUtil.lastIndexOfAny("zzabyycdxx", new String[] { "mn", "" }));
    }

    @Test
    public void containsChar() {
        assertFalse(StringUtil.contains(null, 'a'));
        assertFalse(StringUtil.contains("", 'a'));
        assertTrue(StringUtil.contains("abc", 'a'));
        assertFalse(StringUtil.contains("abc", 'z'));
    }

    @Test
    public void containsString() {
        assertFalse(StringUtil.contains(null, "a"));
        assertFalse(StringUtil.contains("aaa", null));
        assertTrue(StringUtil.contains("", ""));
        assertTrue(StringUtil.contains("abc", ""));
        assertTrue(StringUtil.contains("abc", "a"));
        assertFalse(StringUtil.contains("abc", "z"));
    }

    @Test
    public void countMatches() {
        assertEquals(0, StringUtil.countMatches(null, "a"));
        assertEquals(0, StringUtil.countMatches("", "a"));
        assertEquals(0, StringUtil.countMatches("abba", null));
        assertEquals(0, StringUtil.countMatches("abba", ""));
        assertEquals(2, StringUtil.countMatches("abba", "a"));
        assertEquals(1, StringUtil.countMatches("abba", "ab"));
        assertEquals(0, StringUtil.countMatches("abba", "xxx"));
    }

    @Test
    public void containsOnlyChar() {
        assertFalse(StringUtil.containsOnly(null, "a".toCharArray()));
        assertFalse(StringUtil.containsOnly("abc", (char[]) null));
        assertTrue(StringUtil.containsOnly("", "a".toCharArray()));
        assertFalse(StringUtil.containsOnly("ab", "".toCharArray()));
        assertTrue(StringUtil.containsOnly("abab", "abc".toCharArray()));
        assertFalse(StringUtil.containsOnly("ab1", "abc".toCharArray()));
        assertFalse(StringUtil.containsOnly("abz", "abc".toCharArray()));
    }

    @Test
    public void containsOnlyString() {
        assertFalse(StringUtil.containsOnly(null, "a"));
        assertFalse(StringUtil.containsOnly("abc", (String) null));
        assertTrue(StringUtil.containsOnly("", "a"));
        assertFalse(StringUtil.containsOnly("ab", ""));
        assertTrue(StringUtil.containsOnly("abab", "abc"));
        assertFalse(StringUtil.containsOnly("ab1", "abc"));
        assertFalse(StringUtil.containsOnly("abz", "abc"));
    }

    @Test
    public void containsNoneChar() {
        assertTrue(StringUtil.containsNone(null, "a".toCharArray()));
        assertTrue(StringUtil.containsNone("abc", (char[]) null));
        assertTrue(StringUtil.containsNone("", "a".toCharArray()));
        assertTrue(StringUtil.containsNone("ab", "".toCharArray()));
        assertTrue(StringUtil.containsNone("abab", "xyz".toCharArray()));
        assertTrue(StringUtil.containsNone("ab1", "xyz".toCharArray()));
        assertFalse(StringUtil.containsNone("abz", "xyz".toCharArray()));
    }

    @Test
    public void containsNoneString() {
        assertTrue(StringUtil.containsNone(null, "a"));
        assertTrue(StringUtil.containsNone("abc", (String) null));
        assertTrue(StringUtil.containsNone("", "a"));
        assertTrue(StringUtil.containsNone("ab", ""));
        assertTrue(StringUtil.containsNone("abab", "xyz"));
        assertTrue(StringUtil.containsNone("ab1", "xyz"));
        assertFalse(StringUtil.containsNone("abz", "xyz"));
    }

    // ==========================================================================
    // 取子串函数。
    // ==========================================================================

    @Test
    public void substring() {
        assertNull(StringUtil.substring(null, 1));
        assertEquals("", StringUtil.substring("", 1));
        assertEquals("abc", StringUtil.substring("abc", 0));
        assertEquals("c", StringUtil.substring("abc", 2));
        assertEquals("", StringUtil.substring("abc", 4));
        assertEquals("bc", StringUtil.substring("abc", -2));
        assertEquals("abc", StringUtil.substring("abc", -4));

        assertNull(StringUtil.substring(null, 1, 2));
        assertEquals("", StringUtil.substring("", 1, 2));
        assertEquals("ab", StringUtil.substring("abc", 0, 2));
        assertEquals("", StringUtil.substring("abc", 2, 0));
        assertEquals("c", StringUtil.substring("abc", 2, 4));
        assertEquals("", StringUtil.substring("abc", 4, 6));
        assertEquals("", StringUtil.substring("abc", 2, 2));
        assertEquals("b", StringUtil.substring("abc", -2, -1));
        assertEquals("ab", StringUtil.substring("abc", -4, 2));
        assertEquals("", StringUtil.substring("abc", -5, -4));
    }

    @Test
    public void left() {
        assertNull(StringUtil.left(null, 1));
        assertEquals("", StringUtil.left("abc", -1));
        assertEquals("", StringUtil.left("", 1));
        assertEquals("", StringUtil.left("abc", 0));
        assertEquals("ab", StringUtil.left("abc", 2));
        assertEquals("abc", StringUtil.left("abc", 4));
    }

    @Test
    public void right() {
        assertNull(StringUtil.right(null, 1));
        assertEquals("", StringUtil.right("abc", -1));
        assertEquals("", StringUtil.right("", 1));
        assertEquals("", StringUtil.right("abc", 0));
        assertEquals("bc", StringUtil.right("abc", 2));
        assertEquals("abc", StringUtil.right("abc", 4));
    }

    @Test
    public void mid() {
        assertNull(StringUtil.mid(null, 1, 2));
        assertEquals("", StringUtil.mid("abc", 1, -1));
        assertEquals("", StringUtil.mid("", 0, 1));
        assertEquals("ab", StringUtil.mid("abc", 0, 2));
        assertEquals("abc", StringUtil.mid("abc", 0, 4));
        assertEquals("c", StringUtil.mid("abc", 2, 4));
        assertEquals("", StringUtil.mid("abc", 4, 2));
        assertEquals("ab", StringUtil.mid("abc", -2, 2));
    }

    // ==========================================================================
    // 搜索并取子串函数。
    // ==========================================================================
    @Test
    public void substringBefore() {
        assertNull(StringUtil.substringBefore(null, "a"));
        assertEquals("", StringUtil.substringBefore("", "a"));
        assertEquals("", StringUtil.substringBefore("abc", "a"));
        assertEquals("a", StringUtil.substringBefore("abcba", "b"));
        assertEquals("ab", StringUtil.substringBefore("abc", "c"));
        assertEquals("abc", StringUtil.substringBefore("abc", "d"));
        assertEquals("", StringUtil.substringBefore("abc", ""));
        assertEquals("abc", StringUtil.substringBefore("abc", null));
    }

    @Test
    public void substringAfter() {
        assertNull(StringUtil.substringAfter(null, "a"));
        assertEquals("", StringUtil.substringAfter("", "a"));
        assertEquals("", StringUtil.substringAfter("abc", null));
        assertEquals("bc", StringUtil.substringAfter("abc", "a"));
        assertEquals("cba", StringUtil.substringAfter("abcba", "b"));
        assertEquals("", StringUtil.substringAfter("abc", "c"));
        assertEquals("", StringUtil.substringAfter("abc", "d"));
        assertEquals("abc", StringUtil.substringAfter("abc", ""));
    }

    @Test
    public void substringBeforeLast() {
        assertNull(StringUtil.substringBeforeLast(null, "a"));
        assertEquals("", StringUtil.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtil.substringBeforeLast("abcba", "b"));
        assertEquals("ab", StringUtil.substringBeforeLast("abc", "c"));
        assertEquals("", StringUtil.substringBeforeLast("a", "a"));
        assertEquals("a", StringUtil.substringBeforeLast("a", "z"));
        assertEquals("a", StringUtil.substringBeforeLast("a", null));
        assertEquals("a", StringUtil.substringBeforeLast("a", ""));
    }

    @Test
    public void substringAfterLast() {
        assertNull(StringUtil.substringAfterLast(null, "a"));
        assertEquals("", StringUtil.substringAfterLast("", "a"));
        assertEquals("", StringUtil.substringAfterLast("abc", ""));
        assertEquals("", StringUtil.substringAfterLast("abc", null));
        assertEquals("bc", StringUtil.substringAfterLast("abc", "a"));
        assertEquals("a", StringUtil.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtil.substringAfterLast("abc", "c"));
        assertEquals("", StringUtil.substringAfterLast("a", "a"));
        assertEquals("", StringUtil.substringAfterLast("a", "z"));
    }

    @Test
    public void substringBetween() {
        assertNull(StringUtil.substringBetween(null, "a"));
        assertEquals("", StringUtil.substringBetween("", ""));
        assertNull(StringUtil.substringBetween("", "tag"));
        assertNull(StringUtil.substringBetween("tagabctag", null));
        assertEquals("", StringUtil.substringBetween("tagabctag", ""));
        assertEquals("abc", StringUtil.substringBetween("tagabctag", "tag"));

        assertNull(StringUtil.substringBetween(null, "a", "b"));
        assertEquals("", StringUtil.substringBetween("", "", ""));
        assertNull(StringUtil.substringBetween("", "", "tag"));
        assertNull(StringUtil.substringBetween("", "tag", "tag"));
        assertNull(StringUtil.substringBetween("yabcz", null, null));
        assertEquals("", StringUtil.substringBetween("yabcz", "", ""));
        assertEquals("abc", StringUtil.substringBetween("yabcz", "y", "z"));
        assertEquals("abc", StringUtil.substringBetween("yabczyabcz", "y", "z"));
    }

    // ==========================================================================
    // 删除字符。 
    // ==========================================================================

    @Test
    public void deleteWhitespace() {
        assertNull(StringUtil.deleteWhitespace(null));
        assertEquals("", StringUtil.deleteWhitespace(""));
        assertEquals("abc", StringUtil.deleteWhitespace("abc"));
        assertEquals("abc", StringUtil.deleteWhitespace("   ab  c  "));
    }

    // ==========================================================================
    // 替换子串。 
    // ==========================================================================

    @Test
    public void replace() {
        assertNull(StringUtil.replace(null, "a", "z"));
        assertEquals("", StringUtil.replace("", "a", "z"));
        assertEquals("aba", StringUtil.replace("aba", null, null));
        assertEquals("aba", StringUtil.replace("aba", null, null));
        assertEquals("aba", StringUtil.replace("aba", "a", null));
        assertEquals("b", StringUtil.replace("aba", "a", ""));
        assertEquals("zbz", StringUtil.replace("aba", "a", "z"));
    }

    @Test
    public void replaceMax() {
        assertNull(StringUtil.replace(null, "a", "z", 1));
        assertEquals("", StringUtil.replace("", "a", "z", 1));
        assertEquals("abaa", StringUtil.replace("abaa", null, null, 1));
        assertEquals("abaa", StringUtil.replace("abaa", null, null, 1));
        assertEquals("abaa", StringUtil.replace("abaa", "a", null, 1));
        assertEquals("baa", StringUtil.replace("abaa", "a", "", 1));
        assertEquals("abaa", StringUtil.replace("abaa", "a", "z", 0));
        assertEquals("zbaa", StringUtil.replace("abaa", "a", "z", 1));
        assertEquals("zbza", StringUtil.replace("abaa", "a", "z", 2));
        assertEquals("zbzz", StringUtil.replace("abaa", "a", "z", -1));
    }

    @Test
    public void replaceChar() {
        assertNull(StringUtil.replaceChar(null, 'a', 'b'));
        assertEquals("", StringUtil.replaceChar("", 'a', 'b'));
        assertEquals("aycya", StringUtil.replaceChar("abcba", 'b', 'y'));
        assertEquals("abcba", StringUtil.replaceChar("abcba", 'z', 'y'));
    }

    @Test
    public void replaceChars() {
        assertNull(StringUtil.replaceChars(null, "a", "b"));
        assertEquals("", StringUtil.replaceChars("", "a", "b"));
        assertEquals("abc", StringUtil.replaceChars("abc", null, "b"));
        assertEquals("abc", StringUtil.replaceChars("abc", "", "a"));
        assertEquals("ac", StringUtil.replaceChars("abc", "b", null));
        assertEquals("ac", StringUtil.replaceChars("abc", "b", ""));
        assertEquals("ayzya", StringUtil.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtil.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtil.replaceChars("abcba", "bc", "yzx"));

        String s = new String("abcba");

        assertSame(s, StringUtil.replaceChars(s, "de", "xy")); // 未改变
    }

    @Test
    public void overlay() {
        assertNull(StringUtil.overlay(null, "abc", 0, 1));
        assertEquals("abc", StringUtil.overlay("", "abc", 0, 0));
        assertEquals("abef", StringUtil.overlay("abcdef", null, 2, 4));
        assertEquals("abef", StringUtil.overlay("abcdef", "", 2, 4));
        assertEquals("abef", StringUtil.overlay("abcdef", "", 4, 2));
        assertEquals("abzzzzef", StringUtil.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtil.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtil.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abzzzz", StringUtil.overlay("abcdef", "zzzz", 2, 8));
        assertEquals("zzzzabcdef", StringUtil.overlay("abcdef", "zzzz", -2, -3));
        assertEquals("abcdefzzzz", StringUtil.overlay("abcdef", "zzzz", 8, 10));
    }

    // ==========================================================================
    // Perl风格的chomp和chop函数。 
    // ==========================================================================

    @Test
    public void chomp() {
        assertNull(StringUtil.chomp(null));
        assertEquals("", StringUtil.chomp(""));
        assertEquals("abc ", StringUtil.chomp("abc \r"));
        assertEquals("abc", StringUtil.chomp("abc\n"));
        assertEquals("abc", StringUtil.chomp("abc\r\n"));
        assertEquals("abc\r\n", StringUtil.chomp("abc\r\n\r\n"));
        assertEquals("abc\n", StringUtil.chomp("abc\n\r"));
        assertEquals("abc\n\rabc", StringUtil.chomp("abc\n\rabc"));
        assertEquals("a", StringUtil.chomp("a"));
        assertEquals("", StringUtil.chomp("\r"));
        assertEquals("", StringUtil.chomp("\n"));
        assertEquals("", StringUtil.chomp("\r\n"));
    }

    @Test
    public void chompString() {
        assertNull(StringUtil.chomp(null, "aa"));
        assertEquals("", StringUtil.chomp("", "aa"));
        assertEquals("foo", StringUtil.chomp("foobar", "bar"));
        assertEquals("foobar", StringUtil.chomp("foobar", "baz"));
        assertEquals("", StringUtil.chomp("foo", "foo"));
        assertEquals("foo ", StringUtil.chomp("foo ", "foo"));
        assertEquals(" ", StringUtil.chomp(" foo", "foo"));
        assertEquals("foo", StringUtil.chomp("foo", "foooo"));
        assertEquals("foo", StringUtil.chomp("foo", ""));
        assertEquals("foo", StringUtil.chomp("foo", null));
    }

    @Test
    public void chop() {
        assertEquals(null, StringUtil.chop(null));
        assertEquals("", StringUtil.chop(""));
        assertEquals("abc ", StringUtil.chop("abc \r"));
        assertEquals("abc", StringUtil.chop("abc\n"));
        assertEquals("abc", StringUtil.chop("abc\r\n"));
        assertEquals("ab", StringUtil.chop("abc"));
        assertEquals("abc\nab", StringUtil.chop("abc\nabc"));
        assertEquals("", StringUtil.chop("a"));
        assertEquals("", StringUtil.chop("\r"));
        assertEquals("", StringUtil.chop("\n"));
        assertEquals("", StringUtil.chop("\r\n"));
    }

    // ==========================================================================
    // 重复字符串。 
    // ==========================================================================

    @Test
    public void repeat() {
        assertNull(StringUtil.repeat(null, 2));

        String s = new String();
        assertSame(s, StringUtil.repeat(s, 1));
        assertSame(s, StringUtil.repeat(s, 2));

        s = new String("test");
        assertSame(s, StringUtil.repeat(s, 1));

        assertEquals("", StringUtil.repeat("", 0));
        assertEquals("", StringUtil.repeat("", 2));
        assertEquals("aaa", StringUtil.repeat("a", 3));
        assertEquals("abab", StringUtil.repeat("ab", 2));
        assertEquals("abcdabcd", StringUtil.repeat("abcd", 2));
        assertEquals("", StringUtil.repeat("a", -2));
    }

    // ==========================================================================
    // 反转字符串。
    // ==========================================================================

    @Test
    public void reverse() {
        assertNull(StringUtil.reverse(null));
        assertEquals("", StringUtil.reverse(""));
        assertEquals("tab", StringUtil.reverse("bat"));
    }

    @Test
    public void reverseDelimited() {
        assertNull(StringUtil.reverseDelimited(null, 'x'));
        assertEquals("", StringUtil.reverseDelimited("", 'x'));
        assertEquals("a.b.c", StringUtil.reverseDelimited("a.b.c", 'x'));
        assertEquals("c.b.a", StringUtil.reverseDelimited("a.b.c", '.'));
    }

    @Test
    public void reverseDelimitedString() {
        assertNull(StringUtil.reverseDelimited(null, "", ""));
        assertEquals("", StringUtil.reverseDelimited("", "", ""));
        assertEquals("a.b.c", StringUtil.reverseDelimited("a.b.c", null, null));
        assertEquals("a.b.c", StringUtil.reverseDelimited("a.b.c", "", null));
        assertEquals("c,b,a", StringUtil.reverseDelimited("a.b.c", ".", ","));
        assertEquals("c b a", StringUtil.reverseDelimited("a.b.c", ".", null));
    }

    // ==========================================================================
    // 取得字符串的缩略。
    // ==========================================================================

    @Test
    public void abbreviate() {
        assertEquals(null, StringUtil.abbreviate(null, 4));
        assertEquals("", StringUtil.abbreviate("", 4));
        assertEquals("abc...", StringUtil.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtil.abbreviate("abcdefg", 7));
        assertEquals("abcdefg", StringUtil.abbreviate("abcdefg", 8));
        assertEquals("a...", StringUtil.abbreviate("abcdefg", 4));

        // maxWidth不足4，则设置成4
        assertEquals("a...", StringUtil.abbreviate("abcdefg", 3));
    }

    @Test
    public void abbreviateOffset() {
        assertEquals(null, StringUtil.abbreviate(null, 0, 4));
        assertEquals("", StringUtil.abbreviate("", 0, 4));
        assertEquals("abcdefg...", StringUtil.abbreviate("abcdefghijklmno", -1, 10));
        assertEquals("abcdefg...", StringUtil.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("abcdefg...", StringUtil.abbreviate("abcdefghijklmno", 1, 10));
        assertEquals("abcdefg...", StringUtil.abbreviate("abcdefghijklmno", 4, 10));
        assertEquals("...fghi...", StringUtil.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ghij...", StringUtil.abbreviate("abcdefghijklmno", 6, 10));
        assertEquals("...ijklmno", StringUtil.abbreviate("abcdefghijklmno", 8, 10));
        assertEquals("...ijklmno", StringUtil.abbreviate("abcdefghijklmno", 10, 10));
        assertEquals("...ijklmno", StringUtil.abbreviate("abcdefghijklmno", 12, 10));
        assertEquals("...ijklmno", StringUtil.abbreviate("abcdefghijklmno", 100, 10));

        // maxWidth不足4，则设置成4
        assertEquals("a...", StringUtil.abbreviate("abcdefghij", 0, 3));

        // maxWidth不足7，则设置成7
        assertEquals("...f...", StringUtil.abbreviate("abcdefghij", 5, 6));
    }

    // ==========================================================================
    // 比较两个字符串的异同。
    // ==========================================================================

    @Test
    public void difference() {
        assertEquals("robot", StringUtil.difference("i am a machine", "i am a robot"));
        assertNull(StringUtil.difference(null, null));
        assertEquals("", StringUtil.difference("", ""));
        assertEquals("", StringUtil.difference("", null));
        assertEquals("abc", StringUtil.difference("", "abc"));
        assertEquals("", StringUtil.difference("abc", ""));
        assertEquals("", StringUtil.difference("abc", "abc"));
        assertEquals("xyz", StringUtil.difference("ab", "abxyz"));
        assertEquals("xyz", StringUtil.difference("abcde", "abxyz"));
        assertEquals("xyz", StringUtil.difference("abcde", "xyz"));
    }

    @Test
    public void indexOfDifference() {
        assertEquals(7, StringUtil.indexOfDifference("i am a machine", "i am a robot"));
        assertEquals(-1, StringUtil.indexOfDifference(null, null));
        assertEquals(-1, StringUtil.indexOfDifference("", null));
        assertEquals(-1, StringUtil.indexOfDifference("", ""));
        assertEquals(0, StringUtil.indexOfDifference("", "abc"));
        assertEquals(0, StringUtil.indexOfDifference("abc", ""));
        assertEquals(-1, StringUtil.indexOfDifference("abc", "abc"));
        assertEquals(2, StringUtil.indexOfDifference("ab", "abxyz"));
        assertEquals(2, StringUtil.indexOfDifference("abcde", "abxyz"));
        assertEquals(0, StringUtil.indexOfDifference("abcde", "xyz"));
        assertEquals(-1, StringUtil.indexOfDifference("abcde", new String("abcde")));
    }

    // ==========================================================================
    // 将数字或字节转换成ASCII字符串的函数。 
    // ==========================================================================

    @Test
    public void longToString() {
        longToString("", 0);
        longToString("1", 62);
        longToString("2", 62 + 62);
    }

    private void longToString(String prefix, long base) {
        char ch = '0';

        for (long i = base; i < base + 10; i++, ch++) {
            assertEquals(ch + prefix, StringUtil.longToString(i));
            assertEquals(ch + prefix, StringUtil.longToString(-i));
        }

        ch = 'A';

        for (long i = base + 10; i < base + 36; i++, ch++) {
            assertEquals(ch + prefix, StringUtil.longToString(i));
            assertEquals(ch + prefix, StringUtil.longToString(-i));
        }

        ch = 'a';

        for (long i = base + 36; i < base + 62; i++, ch++) {
            assertEquals(ch + prefix, StringUtil.longToString(i));
            assertEquals(ch + prefix, StringUtil.longToString(-i));
        }

        long l = 0 //
                + 62L //
                + 10 * 256L //
                + 2 * 256 * 256L //
                + 1 * 256 * 256 * 256L //
                + 0 * 256 * 256 * 256 * 256L;

        assertEquals("cIx81", StringUtil.longToString(l));
        assertEquals("IJG2A", StringUtil.longToString(l, true));
    }

    @Test
    public void bytesToString() {
        assertEquals("0", StringUtil.bytesToString(null));
        assertEquals("0", StringUtil.bytesToString(new byte[] {}));
        assertEquals("0", StringUtil.bytesToString(new byte[] { 0, 0, 0, 0, 0, 0 }));
        assertEquals("1", StringUtil.bytesToString(new byte[] { 0, 0, 0, 0, 0, 1 }));
        assertEquals("GWO823H", StringUtil.bytesToString(new byte[] { 1, 0, 0, 0, 0, 0 }));
        assertEquals("cIx81", StringUtil.bytesToString(new byte[] { 0, 1, 2, 10, 62 }));
        assertEquals("cIx8QaO8KjH", StringUtil.bytesToString(new byte[] { 0, 1, 2, 10, 62, 0, 1, 2, 10, 62 }));

        assertEquals("IJG2Y0YVRQ5V2", StringUtil.bytesToString(new byte[] { 0, 1, 2, 10, 62, 0, 1, 2, 10, 62 }, true));
    }

    // ==========================================================================
    // 缩进排版函数。 
    // ==========================================================================

    @Test
    public void indent() {
        // str is empty
        assertEquals(null, StringUtil.indent((String) null, "  "));
        assertEquals("", StringUtil.indent("", "  "));

        // indent is empty
        assertEquals("a\nb", StringUtil.indent("a\nb", null));
        assertEquals("a\nb", StringUtil.indent("a\nb", ""));

        // indents
        assertEquals("\n  aaa\n  bbb\n  ccc", StringUtil.indent("\naaa\nbbb\nccc", "  "));
        assertEquals("aa\n  aaa\n  bbb\n\n  ccc\n\n", StringUtil.indent("aa\naaa\nbbb\n\nccc\n\n", "  "));
    }

    @Test
    public void indentWithBuffer() {
        StringBuilder buf;
        Formatter format;

        buf = new StringBuilder();
        StringUtil.indent(buf, "", "");
        assertEquals("", buf.toString());

        buf = new StringBuilder();
        StringUtil.indent(buf, "a\nb", "  ");
        assertEquals("a\n  b", buf.toString());

        buf = new StringBuilder();
        format = new Formatter(buf);

        StringUtil.indent(format, "a\nb", "  ");
        assertEquals("a\n  b", buf.toString());

        // try appendable other than string builder
        StringBuffer buf2 = new StringBuffer();
        format = new Formatter(buf2);

        StringUtil.indent(format, "a\nb", "  ");
        assertEquals("a\n  b", buf2.toString());
    }
}
