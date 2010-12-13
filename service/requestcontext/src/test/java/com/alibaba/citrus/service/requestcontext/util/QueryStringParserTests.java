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
package com.alibaba.citrus.service.requestcontext.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.util.i18n.LocaleUtil;

public class QueryStringParserTests {
    private QueryStringParser parser;
    private MapMock map;

    @Test
    public void charset() {
        LocaleUtil.setContext(Locale.CHINA, "GB2312");

        // no charset
        parser = new MyParser();
        assertEquals("GB2312", parser.getCharacterEncoding());

        // no defaults
        parser = new MyParser(null);
        assertEquals("GB2312", parser.getCharacterEncoding());

        parser = new MyParser("  ");
        assertEquals("GB2312", parser.getCharacterEncoding());

        parser = new MyParser(" UTF-8 ");
        assertEquals("UTF-8", parser.getCharacterEncoding());

        // with defaults
        parser = new MyParser(null, null);
        assertEquals("GB2312", parser.getCharacterEncoding());

        parser = new MyParser(null, "  ");
        assertEquals("GB2312", parser.getCharacterEncoding());

        parser = new MyParser(null, " UTF-8 ");
        assertEquals("UTF-8", parser.getCharacterEncoding());

        parser = new MyParser(" GBK ", " UTF-8 ");
        assertEquals("GBK", parser.getCharacterEncoding());

        LocaleUtil.resetContext();
    }

    @Test
    public void append() throws Exception {
        parser = new MyParser("GBK");

        parser.append("中", "国1");
        parser.append("中", "国2");
        parser.append("a", null);
        parser.append(null, "  ");
        parser.append("a", ":=,"); // 对:=,进行编码

        assertEquals("%D6%D0=%B9%FA1&%D6%D0=%B9%FA2&a=&=++&a=%3A%3D%2C", parser.toQueryString());
        assertEquals(null, parser.toQueryString());
    }

    @Test
    public void append_jsonLike() throws Exception {
        parser = new MyParser("GBK");
        parser.setEqualSign(" : ").setAndSign(", ");

        parser.append("中", "国1");
        parser.append("中", "国2");
        parser.append("a", null);
        parser.append(null, "  ");
        parser.append("a", ":=,"); // 对:=,进行编码

        assertEquals("%D6%D0 : %B9%FA1, %D6%D0 : %B9%FA2, a : ,  : ++, a : %3A%3D%2C", parser.toQueryString());
        assertEquals(null, parser.toQueryString());

        // turn off json-like
        parser.setEqualSign(null).setAndSign(null);

        parser.append("中", "国1");
        parser.append("中", "国2");
        parser.append("a", null);
        parser.append(null, "  ");
        parser.append("a", ":=,"); // 对:=,进行编码

        assertEquals("%D6%D0=%B9%FA1&%D6%D0=%B9%FA2&a=&=++&a=%3A%3D%2C", parser.toQueryString());
        assertEquals(null, parser.toQueryString());
    }

    @Test
    public void appendMap() throws Exception {
        parser = new MyParser("GBK");

        Map<String, String> params = createLinkedHashMap();

        params.put("中", "国1");
        params.put("a", null);
        params.put(null, "  ");
        params.put("b", ":="); // 对:=进行编码

        parser.append(params);

        assertEquals("%D6%D0=%B9%FA1&a=&=++&b=%3A%3D", parser.toQueryString());
        assertEquals(null, parser.toQueryString());
    }

    @Test
    public void parse() throws Exception {
        assertParse("  &a=1&a=2&b=3& ", null, "a", "1", "a", "2", "b", "3"); // key-values
        assertParse("a", null, "a", ""); // key only
        assertParse("a&b=&=1", null, "a", "", "b", ""); // hybrid
        assertParse("  &a = ++&a=+2+&b=+3+& ", null, "a", "  ", "a", " 2 ", "b", " 3 "); // with spaces，注意，忽略空格
    }

    @Test
    public void parse_jsonLike() throws Exception {
        assertParse("  ,a:1, a:2, b:3, ", null, true, "a", "1", "a", "2", "b", "3"); // key-values，注意，忽略空格
        assertParse("a", null, true, "a", ""); // key only
        assertParse("a,b:,:1", null, true, "a", "", "b", ""); // hybrid
        assertParse("  ,a:++,a:+2+,b:+3+, ", null, true, "a", "  ", "a", " 2 ", "b", " 3 "); // with spaces
    }

    @Test
    public void parseNoExtension() throws Exception {
        try {
            new QueryStringParser().parse("a=1");
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e,
                    exception("You should extend class QueryStringParser and override method add(String, String)"));
        }
    }

    /**
     * 相当于在IE地址栏中直接输入中文，没有URL encoding
     */
    @Test
    public void parseRawBytes() throws Exception {
        assertParse(reencode("中=国", "UTF-8"), "UTF-8", "中", "国");
        assertParse(reencode("&中=国&", "GBK"), "GBK", "中", "国");
    }

    /**
     * 相当于在Firefox地址栏中直接输入中文，有URL encoding
     */
    @Test
    public void parseURLEncoded() throws Exception {
        assertParse(urlencode("中=国", "UTF-8"), "UTF-8", "中", "国");
        assertParse(urlencode("&中=国&", "GBK"), "GBK", "中", "国");
    }

    /**
     * 直接是unicode字符。
     */
    @Test
    public void parseUnicode() throws Exception {
        assertParse("中=" + reencode("国", "UTF-8"), "UTF-8", "中", "国");
        assertParse("&" + reencode("中", "GBK") + "国=世界&", "GBK", "中国", "世界");
    }

    private void assertParse(String queryString, String charset, String... expectedKeyValues) {
        assertParse(queryString, charset, false, expectedKeyValues);
    }

    private void assertParse(String queryString, String charset, boolean jsonLike, String... expectedKeyValues) {
        parser = new MyParser(charset);

        if (jsonLike) {
            parser.setEqualSign(":").setAndSign(",");
        }

        map = createMock(MapMock.class);

        for (int i = 0; i < expectedKeyValues.length; i += 2) {
            map.add(expectedKeyValues[i], expectedKeyValues[i + 1]);
        }

        replay(map);
        parser.parse(queryString);
        verify(map);
    }

    private String reencode(String str, String charset) throws Exception {
        return new String(str.getBytes(charset), "8859_1");
    }

    private String urlencode(String str, String charset) throws Exception {
        str = str.replaceAll("=", ".equals.").replaceAll("&", ".and.");
        str = URLEncoder.encode(str, charset);
        str = str.replaceAll(".equals.", "=").replaceAll(".and.", "&");
        return str;
    }

    private class MyParser extends QueryStringParser {
        public MyParser() {
            super();
        }

        public MyParser(String charset) {
            super(charset);
        }

        public MyParser(String charset, String defaultCharset) {
            super(charset, defaultCharset);
        }

        @Override
        protected void add(String key, String value) {
            map.add(key, value);
        }
    }

    public static interface MapMock {
        void add(String key, String value);
    }
}
