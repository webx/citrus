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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Formatter;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.util.i18n.LocaleUtil;
import com.alibaba.citrus.util.internal.Entities;

/**
 * 测试<code>StringEscapeUtil</code>。
 * 
 * @author Michael Zhou
 */
public class StringEscapeUtilTests {
    private Entities entities;

    @Before
    public void init() {
        entities = new Entities();
        entities.addEntity("foo", '\u00A1');
        entities.addEntity("bar", '\u00A2');
    }

    /* ==================================================================== */
    /* 测试Java和JavaScript。 */
    /* ==================================================================== */
    @Test
    public void escapeJava() throws IOException {
        assertEquals(null, StringEscapeUtil.escapeJava(null));

        try {
            StringEscapeUtil.escapeJava(null, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            StringEscapeUtil.escapeJava("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        assertEscapeJava("empty string", "", "", false);
        assertEscapeJava("foo", "foo", false);
        assertEscapeJava("tab", "\\t", "\t", false);
        assertEscapeJava("backslash", "\\\\", "\\", false);
        assertEscapeJava("single quote should not be escaped", "'", "'", false);
        assertEscapeJava("\\\\\\b\\t\\r", "\\\b\t\r", false);
        assertEscapeJava("\u1234", "\u1234", false);
        assertEscapeJava("\u0234", "\u0234", false);
        assertEscapeJava("\u00EF", "\u00ef", false);
        assertEscapeJava("\\u0001", "\u0001", false);
        assertEscapeJava("Should use capitalized unicode hex", "\\u001A", "\u001a", false);

        assertEscapeJava("He didn't say, \\\"stop!\\\"", "He didn't say, \"stop!\"", false);
        assertEscapeJava("non-breaking space", "This space is non-breaking:" + "\u00A0",
                "This space is non-breaking:\u00a0", false);
        assertEscapeJava("\uABCD\u1234\u012C", "\uABCD\u1234\u012C", false);

        // 对slash的escape
        assertEscapeJava("He didn't say, /stop!/", "He didn't say, /stop!/", false);

        // 不需要escape的字符串，应该返回原字符串，以提高效率
        assertEscapeJava("hello, i'm baobao", "hello, i'm baobao", false);
    }

    @Test
    public void escapeJavaStrict() throws IOException {
        assertEquals(null, StringEscapeUtil.escapeJava(null));

        try {
            StringEscapeUtil.escapeJava(null, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            StringEscapeUtil.escapeJava("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        assertEscapeJava("empty string", "", "", true);
        assertEscapeJava("foo", "foo", true);
        assertEscapeJava("tab", "\\t", "\t", true);
        assertEscapeJava("backslash", "\\\\", "\\", true);
        assertEscapeJava("single quote should not be escaped", "'", "'", true);
        assertEscapeJava("\\\\\\b\\t\\r", "\\\b\t\r", true);
        assertEscapeJava("\\u1234", "\u1234", true);
        assertEscapeJava("\\u0234", "\u0234", true);
        assertEscapeJava("\u00EF", "\u00ef", true);
        assertEscapeJava("\\u0001", "\u0001", true);
        assertEscapeJava("Should use capitalized unicode hex", "\\u001A", "\u001a", true);

        assertEscapeJava("He didn't say, \\\"stop!\\\"", "He didn't say, \"stop!\"", true);
        assertEscapeJava("non-breaking space", "This space is non-breaking:" + "\u00A0",
                "This space is non-breaking:\u00a0", true);
        assertEscapeJava("\\uABCD\\u1234\\u012C", "\uABCD\u1234\u012C", true);

        // 不需要escape的字符串，应该返回原字符串，以提高效率
        assertEscapeJava("hello, i'm baobao", "hello, i'm baobao", true);
    }

    private void assertEscapeJava(String escaped, String original, boolean strict) throws IOException {
        assertEscapeJava(null, escaped, original, strict);
    }

    private void assertEscapeJava(String message, String expected, String original, boolean strict) throws IOException {
        String converted = strict ? StringEscapeUtil.escapeJava(original, true) : StringEscapeUtil.escapeJava(original);

        message = "escapeJava(String) failed" + (message == null ? "" : ": " + message);
        assertEquals(message, expected, converted);

        // 如果converted和original相等的话，它们应该是同一个对象
        if (original != null && original.equals(converted)) {
            assertSame(original, converted);
        }

        StringWriter out = new StringWriter();

        if (strict) {
            StringEscapeUtil.escapeJava(original, out, true);
        } else {
            StringEscapeUtil.escapeJava(original, out);
        }

        assertEquals(expected, out.toString());
    }

    @Test
    public void unescapeJava() throws IOException {
        assertEquals(null, StringEscapeUtil.unescapeJava(null));

        try {
            StringEscapeUtil.unescapeJava(null, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            StringEscapeUtil.unescapeJava("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        assertUnescapeJava("\\u02-3", "\\u02-3");
        assertUnescapeJava("", "");
        assertUnescapeJava("test", "test");
        assertUnescapeJava("\ntest\b", "\\ntest\\b");
        assertUnescapeJava("\u123425foo\ntest\b", "\\u123425foo\\ntest\\b");
        assertUnescapeJava("'\foo\teste\r", "\\'\\foo\\teste\\r");
        assertUnescapeJava("\\", "\\");

        // foo
        assertUnescapeJava("lowercase unicode", "\uABCDx", "\\uabcdx");
        assertUnescapeJava("uppercase unicode", "\uABCDx", "\\uABCDx");
        assertUnescapeJava("unicode as final character", "\uABCD", "\\uabcd");

        // 不需要unescape的字符串，应该返回原字符串，以提高效率
        assertUnescapeJava("hello, i'm baobao", "hello, i'm baobao");
    }

    private void assertUnescapeJava(String unescaped, String original) throws IOException {
        assertUnescapeJava(null, unescaped, original);
    }

    private void assertUnescapeJava(String message, String unescaped, String original) throws IOException {
        String expected = unescaped;
        String actual = StringEscapeUtil.unescapeJava(original);

        assertEquals("unescape(String) failed" + (message == null ? "" : ": " + message) + ": expected '"
                + StringEscapeUtil.escapeJava(expected) // we escape this so we can see it in the error message
                + "' actual '" + StringEscapeUtil.escapeJava(actual) + "'", expected, actual);

        // 如果actual和original相等的话，它们应该是同一个对象
        if (original != null && original.equals(actual)) {
            assertSame(original, actual);
        }

        StringWriter out = new StringWriter();

        StringEscapeUtil.unescapeJava(original, out);
        assertEquals(unescaped, out.toString());
    }

    @Test
    public void escapeJavaScript() throws IOException {
        assertEquals(null, StringEscapeUtil.escapeJavaScript(null));

        try {
            StringEscapeUtil.escapeJavaScript(null, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            StringEscapeUtil.escapeJavaScript("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        assertEquals("He didn\\'t say, \\\"stop!\\\"", StringEscapeUtil.escapeJavaScript("He didn't say, \"stop!\""));

        // 对slash的escape
        assertEquals("He didn\\'t say, \\/stop!\\/", StringEscapeUtil.escapeJavaScript("He didn't say, /stop!/"));

        // 不需要escape的字符串，应该返回原字符串，以提高效率
        assertSame("hello, im baobao", StringEscapeUtil.escapeJavaScript("hello, im baobao"));

        // 非strict
        assertEquals("\u1234", StringEscapeUtil.escapeJavaScript("\u1234", false));
        assertEquals("\u0234", StringEscapeUtil.escapeJavaScript("\u0234", false));
        assertEquals("\u00EF", StringEscapeUtil.escapeJavaScript("\u00ef", false));
        assertEquals("\\u0001", StringEscapeUtil.escapeJavaScript("\u0001", false));
        assertEquals("Should use capitalized unicode hex", "\\u001A",
                StringEscapeUtil.escapeJavaScript("\u001a", false));

        // strict
        assertEquals("\\u1234", StringEscapeUtil.escapeJavaScript("\u1234", true));
        assertEquals("\\u0234", StringEscapeUtil.escapeJavaScript("\u0234", true));
        assertEquals("\u00EF", StringEscapeUtil.escapeJavaScript("\u00ef", true));
        assertEquals("\\u0001", StringEscapeUtil.escapeJavaScript("\u0001", true));
        assertEquals("Should use capitalized unicode hex", "\\u001A", StringEscapeUtil.escapeJavaScript("\u001a", true));
    }

    /* ==================================================================== */
    /* 测试HTML和XML。 */
    /* ==================================================================== */
    private String[][] HTML_ESCAPES = {
            { "no escaping", "plain text", "plain text" },
            { "no escaping", "plain text", "plain text" },
            { "empty string", "", "" },
            { "null", null, null },
            { "ampersand", "bread &amp; butter", "bread & butter" },
            { "quotes", "&quot;bread&quot; &amp; butter", "\"bread\" & butter" },
            { "final character only", "greater than &gt;", "greater than >" },
            { "first character only", "&lt; less than", "< less than" },
            { "apostrophe", "Huntington&#39;s chorea", "Huntington's chorea" },
            { "languages", "English,Fran&ccedil;ais,\u65E5\u672C\u8A9E (nihongo)",
                    "English,Fran\u00E7ais,\u65E5\u672C\u8A9E (nihongo)" },
            { "8-bit ascii doesn't number-escape", "~\u007F", "\u007E\u007F" },
            { "8-bit ascii does number-escape", "\u0080\u009F", "\u0080\u009F" },
            { "funny chars pass through OK", "Fran&ccedil;ais", "Fran\u00E7ais" }, { "nbsp", "&nbsp;", "\u00A0" },
            { "html versions", "&Beta;", "\u0392" }, { "illegal entity", "&amp;zzzz;", "&zzzz;" },
            { "illegal entity", "&amp;", "&" }, { "illegal entity", "&amp;;", "&;" },
            { "illegal entity", "&amp;#", "&#" }, { "illegal entity", "&amp;#;", "&#;" },
            { "illegal entity", "&amp;#abc;", "&#abc;" } };
    private String[][] HTML_UNESCAPES = {
            { "no escaping", "plain text", "plain text" },
            { "no escaping", "plain text", "plain text" },
            { "empty string", "", "" },
            { "null", null, null },
            { "ampersand", "bread &amp; butter", "bread & butter" },
            { "quotes", "&quot;bread&quot; &amp; butter", "\"bread\" & butter" },
            { "final character only", "greater than &gt;", "greater than >" },
            { "first character only", "&lt; less than", "< less than" },
            { "apostrophe", "Huntington&#39;s chorea", "Huntington's chorea" },
            { "languages", "English,Fran&ccedil;ais,\u65E5\u672C\u8A9E (nihongo)",
                    "English,Fran\u00E7ais,\u65E5\u672C\u8A9E (nihongo)" },
            { "8-bit ascii doesn't number-escape", "~\u007F", "\u007E\u007F" },
            { "8-bit ascii does number-escape", "\u0080\u009F", "\u0080\u009F" },
            { "funny chars pass through OK", "Fran&ccedil;ais", "Fran\u00E7ais" }, { "nbsp", "&nbsp;", "\u00A0" },
            { "html versions", "&Beta;", "\u0392" }, { "illegal entity", "&zzzz;", "&zzzz;" },
            { "illegal entity", "&", "&" }, { "illegal entity", "&;", "&;" }, { "illegal entity", "&#", "&#" },
            { "illegal entity", "&#;", "&#;" }, { "illegal entity", "&#abc;", "&#abc;" },
            { "number entity", "&#20013;", "中" }, { "illegal entity", "&#x", "&#x" },
            { "illegal entity", "&#x;", "&#x;" }, { "illegal entity", "&#xzzz;", "&#xzzz;" },
            { "number entity", "&#x4E2D;", "中" }, { "number entity", "&#X4E2D;", "中" }, };

    @Test
    public void escapeUnescapeHtml() throws IOException {
        for (int i = 0; i < HTML_ESCAPES.length; ++i) {
            String message = HTML_ESCAPES[i][0];
            String expected = HTML_ESCAPES[i][1];
            String original = HTML_ESCAPES[i][2];

            assertEquals(message, expected, StringEscapeUtil.escapeHtml(original));
            assertEscapeHtmlEntities(message, expected, original, true);
        }

        try {
            StringEscapeUtil.escapeHtml("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        for (int i = 0; i < HTML_UNESCAPES.length; ++i) {
            String message = HTML_UNESCAPES[i][0];
            String expected = HTML_UNESCAPES[i][2];
            String original = HTML_UNESCAPES[i][1];

            assertEquals(message, expected, StringEscapeUtil.unescapeHtml(original));
            assertEscapeHtmlEntities(message, expected, original, false);
        }

        try {
            StringEscapeUtil.unescapeHtml("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    private void assertEscapeHtmlEntities(String message, String expected, String str, boolean escape)
            throws IOException {
        StringWriter out = new StringWriter();

        if (escape) {
            String result = StringEscapeUtil.escapeHtml(str);

            assertEquals(message, expected, result);

            // 如果输入输出字符串相等，则它们应该是同一个字符串，这样系统性能更好
            if (result != null && result.equals(str)) {
                assertSame(str, result);
            }

            StringEscapeUtil.escapeHtml(str, out);
        } else {
            String result = StringEscapeUtil.unescapeHtml(str);

            assertEquals(message, expected, result);

            // 如果输入输出字符串相等，则它们应该是同一个字符串，这样系统性能更好
            if (result != null && result.equals(str)) {
                assertSame(str, result);
            }

            StringEscapeUtil.unescapeHtml(str, out);
        }

        String result = out.toString();

        assertEquals(message, expected, str == null && result.length() == 0 ? null : result);
    }

    @Test
    public void escapeUnescapeXml() throws Exception {
        assertEscapeXmlEntities("&lt;abc&gt;", "<abc>", true);
        assertEscapeXmlEntities("<abc>", "&lt;abc&gt;", false);

        assertEscapeXmlEntities("\u00A1", "\u00A1", true);
        assertEscapeXmlEntities("\u00A0", "\u00A0", true);
        assertEscapeXmlEntities("\u00A0", "&#160;", false);
        assertEscapeXmlEntities("&nbsp;", "&nbsp;", false);

        assertEscapeXmlEntities("ain't", "ain&apos;t", false);
        assertEscapeXmlEntities("ain&apos;t", "ain't", true);
        assertEscapeXmlEntities("", "", true);
        assertEscapeXmlEntities(null, null, true);
        assertEscapeXmlEntities(null, null, false);

        try {
            StringEscapeUtil.escapeXml("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            StringEscapeUtil.unescapeXml("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    private void assertEscapeXmlEntities(String expected, String str, boolean escape) throws IOException {
        StringWriter out = new StringWriter();

        if (escape) {
            String result = StringEscapeUtil.escapeXml(str);

            assertEquals(expected, result);

            // 如果输入输出字符串相等，则它们应该是同一个字符串，这样系统性能更好
            if (result != null && result.equals(str)) {
                assertSame(str, result);
            }

            StringEscapeUtil.escapeXml(str, out);
        } else {
            String result = StringEscapeUtil.unescapeXml(str);

            assertEquals(expected, result);

            // 如果输入输出字符串相等，则它们应该是同一个字符串，这样系统性能更好
            if (result != null && result.equals(str)) {
                assertSame(str, result);
            }

            StringEscapeUtil.unescapeXml(str, out);
        }

        String result = out.toString();

        assertEquals(expected, str == null && result.length() == 0 ? null : result);
    }

    /* ==================================================================== */
    /* 测试一般的entities。 */
    /* ==================================================================== */
    @Test
    public void escapeEntity() throws IOException {
        assertEscapeEntities("&foo;", "\u00A1", true);
        assertEscapeEntities("x&foo;", "x\u00A1", true);
        assertEscapeEntities("&foo;x", "\u00A1x", true);
        assertEscapeEntities("x&foo;x", "x\u00A1x", true);
        assertEscapeEntities("&foo;&bar;", "\u00A1\u00A2", true);

        try {
            StringEscapeUtil.escapeEntities(entities, "", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void unescapeNamedEntity() throws IOException {
        assertEscapeEntities("\u00A1", "&foo;", false);
        assertEscapeEntities("x\u00A1", "x&foo;", false);
        assertEscapeEntities("\u00A1x", "&foo;x", false);
        assertEscapeEntities("x\u00A1x", "x&foo;x", false);
        assertEscapeEntities("\u00A1\u00A2", "&foo;&bar;", false);

        try {
            StringEscapeUtil.unescapeEntities(entities, "", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void unescapeUnicodeEntity() throws IOException {
        assertEscapeEntities("\u00A1", "&#161;", false);
        assertEscapeEntities("x中x", "x&#x4E2D;x", false);

        entities = null;
        assertEscapeEntities("x中x", "x&#x4E2D;x", false);
    }

    @Test
    public void unescapeUnknownEntity() throws IOException {
        assertEscapeEntities("&zzzz;", "&zzzz;", false);
        assertEscapeEntities("&", "&", false);
        assertEscapeEntities("&;", "&;", false);
        assertEscapeEntities("&#", "&#", false);
        assertEscapeEntities("&#;", "&#;", false);
        assertEscapeEntities("&#abc;", "&#abc;", false);
    }

    private void assertEscapeEntities(String expected, String str, boolean escape) throws IOException {
        StringWriter out = new StringWriter();

        if (escape) {
            StringEscapeUtil.escapeEntities(entities, str, out);
        } else {
            StringEscapeUtil.unescapeEntities(entities, str, out);
        }

        assertEquals(expected, out.toString());
    }

    @Test
    public void testUnEscapeNullEntities() {
        assertEquals("中华人民共和国",
                StringEscapeUtil.unescapeEntities(null, "&#20013;&#21326;&#20154;&#27665;&#20849;&#21644;&#22269;"));
        assertEquals("&amp;&lt;", StringEscapeUtil.unescapeEntities(null, "&amp;&lt;"));
    }

    /* ==================================================================== */
    /* 测试SQL。 */
    /* 参考：http://www.jguru.com/faq/view.jsp?EID=8881 */
    /* ==================================================================== */
    @Test
    public void escapeSql() throws Exception {
        assertEscapeSql("don''t stop", "don't stop");
        assertEscapeSql("", "");
        assertEscapeSql(null, null);

        try {
            StringEscapeUtil.escapeSql("", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    private void assertEscapeSql(String expected, String str) throws IOException {
        StringWriter out = new StringWriter();

        StringEscapeUtil.escapeSql(str, out);

        assertEquals(expected, StringEscapeUtil.escapeSql(str));
        assertEquals(expected == null ? "" : expected, out.toString());
    }

    /* ==================================================================== */
    /* URL/URI encoding/decoding。 */
    /* 根据RFC2396：http://www.ietf.org/rfc/rfc2396.txt */
    /* ==================================================================== */
    @Test
    public void escapeURLStrict() throws Exception {
        String unreserved = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'()";

        assertSame(unreserved, StringEscapeUtil.escapeURL(unreserved));

        // 测试所有ISO-8859-1字符
        StringBuilder buffer = new StringBuilder(256);
        StringBuilder expectedBuffer = new StringBuilder(256 * 3);

        for (int i = 0; i < 256; i++) {
            buffer.append((char) i);

            if (i == ' ') {
                expectedBuffer.append('+');
            } else if (unreserved.indexOf(i) == -1) {
                new Formatter(expectedBuffer).format("%%%02X", i);
            } else {
                expectedBuffer.append((char) i);
            }
        }

        String str = buffer.toString();
        String expectedStr = expectedBuffer.toString();

        assertEquals(expectedStr, StringEscapeUtil.escapeURL(str, "8859_1"));

        // 测试writer
        StringWriter writer = new StringWriter();

        StringEscapeUtil.escapeURL(str, "8859_1", writer);
        assertEquals(expectedStr, writer.toString());

        // 测试中文
        assertEquals("%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA", StringEscapeUtil.escapeURL("中华人民共和国", "GBK"));

        // 中文writer
        writer = new StringWriter();
        StringEscapeUtil.escapeURL("中华人民共和国", "GBK", writer);
        assertEquals("%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA", writer.toString());
    }

    @Test
    public void escapeURLLoose() throws Exception {
        String reserved = ";/?:@&=+$,";

        assertEquals("%3B%2F%3F%3A%40%26%3D%2B%24%2C", StringEscapeUtil.escapeURL(reserved, "8859_1", false));

        // 测试所有ISO-8859-1字符
        StringBuilder buffer = new StringBuilder(256);
        StringBuilder expectedBuffer = new StringBuilder(256 * 3);

        for (int i = 0; i < 256; i++) {
            buffer.append((char) i);

            if (i == ' ') {
                expectedBuffer.append('+');
            } else if (reserved.indexOf(i) == -1 && i > 32) {
                expectedBuffer.append((char) i);
            } else {
                new Formatter(expectedBuffer).format("%%%02X", i);
            }
        }

        String str = buffer.toString();
        String expectedStr = expectedBuffer.toString();

        assertEquals(expectedStr, StringEscapeUtil.escapeURL(str, "8859_1", false));

        // 测试writer
        StringWriter writer = new StringWriter();

        StringEscapeUtil.escapeURL(str, "8859_1", writer, false);
        assertEquals(expectedStr, writer.toString());

        // 测试中文和全角空格
        str = "中华人民共和国";
        assertSame(str, StringEscapeUtil.escapeURL(str, "GBK", false));

        str = "中华人民共和国\u3000";
        assertEquals("中华人民共和国%A1%A1", StringEscapeUtil.escapeURL(str, "GBK", false));

        // 中文writer
        writer = new StringWriter();
        StringEscapeUtil.escapeURL("中华人民共和国", "GBK", writer, false);
        assertEquals("中华人民共和国", writer.toString());
    }

    @Test
    public void escapeURLEncoding() {
        LocaleUtil.setContext(Locale.CHINA, "GBK");
        assertEquals("%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA", StringEscapeUtil.escapeURL("中华人民共和国"));

        LocaleUtil.setContext(Locale.US, "8859_1");
        assertEquals("%3F%3F%3F%3F%3F%3F%3F", StringEscapeUtil.escapeURL("中华人民共和国"));

        LocaleUtil.resetContext();
    }

    @Test
    public void unescapeURLStrict() throws Exception {
        String unreserved = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'()";
        String escaped = StringEscapeUtil.escapeURL(unreserved);

        assertSame(unreserved, StringEscapeUtil.unescapeURL(escaped));

        // 测试所有ISO-8859-1字符
        StringBuilder buffer = new StringBuilder(256);
        StringBuilder expectedBuffer = new StringBuilder(256 * 3);

        for (int i = 0; i < 256; i++) {
            buffer.append((char) i);

            if (i == ' ') {
                expectedBuffer.append('+');
            } else if (unreserved.indexOf(i) == -1) {
                new Formatter(expectedBuffer).format("%%%02X", i);
            } else {
                expectedBuffer.append((char) i);
            }
        }

        String str = buffer.toString();
        String expectedStr = expectedBuffer.toString();

        escaped = StringEscapeUtil.escapeURL(str, "8859_1");
        assertEquals(expectedStr, escaped);
        assertEquals(str, StringEscapeUtil.unescapeURL(escaped, "8859_1"));

        // 测试writer
        StringWriter writer = new StringWriter();

        StringEscapeUtil.unescapeURL(escaped, "8859_1", writer);
        assertEquals(str, writer.toString());

        // 测试中文
        escaped = StringEscapeUtil.escapeURL("中华人民共和国", "GBK");
        assertEquals("中华人民共和国", StringEscapeUtil.unescapeURL(escaped, "GBK"));

        // 中文writer
        writer = new StringWriter();
        StringEscapeUtil.unescapeURL(escaped, "GBK", writer);
        assertEquals("中华人民共和国", writer.toString());

        // 混合中/英文、编码/未编码
        assertEquals("中华abc 人民共和国abc 人", StringEscapeUtil.unescapeURL("中华abc+\310%CB民共和国abc+\310%CB", "GBK"));

        // 错误的编码
        str = "abc%xx%20%1";
        assertEquals("abc%xx %1", StringEscapeUtil.unescapeURL(str));

        str = "abc%xx%1%";
        assertSame(str, StringEscapeUtil.unescapeURL(str));
    }

    @Test
    public void unescapeURLLoose() throws Exception {
        String reserved = ";/?:@&=+$,";
        String escaped = StringEscapeUtil.escapeURL(reserved, "8859_1", false);

        assertEquals(reserved, StringEscapeUtil.unescapeURL(escaped, "8859_1"));

        // 测试所有ISO-8859-1字符
        StringBuilder buffer = new StringBuilder(256);
        StringBuilder expectedBuffer = new StringBuilder(256 * 3);

        for (int i = 0; i < 256; i++) {
            buffer.append((char) i);

            if (i == ' ') {
                expectedBuffer.append('+');
            } else if (reserved.indexOf(i) == -1 && i > 32) {
                expectedBuffer.append((char) i);
            } else {
                new Formatter(expectedBuffer).format("%%%02X", i);
            }
        }

        String str = buffer.toString();
        String expectedStr = expectedBuffer.toString();

        escaped = StringEscapeUtil.escapeURL(str, "8859_1", false);

        assertEquals(expectedStr, escaped);
        assertEquals(str, StringEscapeUtil.unescapeURL(escaped, "8859_1"));

        // 测试writer
        StringWriter writer = new StringWriter();

        escaped = StringEscapeUtil.escapeURL(str, "8859_1", false);
        StringEscapeUtil.unescapeURL(escaped, "8859_1", writer);

        assertEquals(str, writer.toString());

        // 测试中文和全角空格
        str = "中华人民共和国";
        assertSame(str, StringEscapeUtil.unescapeURL("中华人民共和国", "GBK"));

        str = "中华人民共和国\u3000";
        assertEquals(str, StringEscapeUtil.unescapeURL("中华人民共和国%A1%A1", "GBK"));

        // 中文writer
        writer = new StringWriter();
        StringEscapeUtil.unescapeURL("中华人民共和国", "GBK", writer);
        assertEquals("中华人民共和国", writer.toString());
    }

    @Test
    public void unescapeURLEncoding() {
        LocaleUtil.setContext(Locale.CHINA, "GBK");
        assertEquals("中华人民共和国", StringEscapeUtil.unescapeURL("%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA"));

        LocaleUtil.setContext(Locale.US, "8859_1");
        assertEquals("\u00D6\u00D0\u00BB\u00AA\u00C8\u00CB\u00C3\u00F1\u00B9\u00B2\u00BA\u00CD\u00B9\u00FA",
                StringEscapeUtil.unescapeURL("%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA"));

        LocaleUtil.resetContext();
    }
}
