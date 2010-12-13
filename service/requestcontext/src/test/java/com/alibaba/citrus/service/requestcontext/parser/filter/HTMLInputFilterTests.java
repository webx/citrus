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
package com.alibaba.citrus.service.requestcontext.parser.filter;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HTMLInputFilterTests {
    private HTMLInputFilter vFilter;

    @Before
    public void init() {
        vFilter = new HTMLInputFilter();
    }

    @After
    public void dispose() {
        vFilter = null;
    }

    private void t(String input, String htmlResult) {
        t(input, input, htmlResult);
    }

    private void t(String input, String ordinaryResult, String htmlResult) {
        assertEquals(htmlResult, vFilter.filter(input));
        assertEquals(ordinaryResult, vFilter.filter(input, false));
    }

    @Test
    public void test_ordinary() {
        t("Frequency response: 5~300mhz<0. 15db 300~1000mhz<0. 45db", "Frequency response: 5~300mhz");
        t("Moisture: <10% Tea Saponin: >13%", "Moisture: 13%");
        t("test<script>test</script>", "testtest", "testtest");
        t("test<script>test", "testtest", "testtest");
        t("testtest</script>", "testtest", "testtest");
        t("test<safe>test</safe>", "test<safe>test</safe>", "testtest");
        t("test<safe>test", "test<safe>test", "testtest");

        String s = "";

        s = "4. Oil absorption, g/100g, < 24\n";
        s += "5. Water absorption, g/100g, < 25\n";
        s += "6. Hydraulic dispersion %, > 85\n";
        s += "7. Resistance rate o/cm, > 3200";

        vFilter.filter(s, false);
    }

    @Test
    public void test_basics() {
        t("", "");
        t("hello", "hello");

        String[] expectedResults = new String[256];

        for (int i = 0; i < 256; i++) {
            expectedResults[i] = String.valueOf((char) i);
        }

        expectedResults['&'] = "&amp;";
        expectedResults['<'] = "";
        expectedResults['>'] = "";

        for (int i = 0; i < expectedResults.length; i++) {
            t(String.valueOf((char) i), expectedResults[i]);
        }
    }

    @Test
    public void test_balancing_tags() {
        t("<b>hello", "<b>hello</b>");
        t("<b>hello", "<b>hello</b>");
        t("hello<b>", "hello");
        t("hello</b>", "hello", "hello");
        t("hello<b/>", "hello<b>", "hello");
        t("<b><b><b>hello", "<b><b><b>hello</b></b></b>");
        t("</b><b>", "<b>", "");
    }

    @Test
    public void test_end_slashes() {
        t("<img>", "<img />", "<img />");
        t("<img/>", "<img />", "<img />");
        t("<b/></b>", "<b></b>", "");
    }

    @Test
    public void test_balancing_angle_brackets() {
        if (HTMLInputFilter.ALWAYS_MAKE_TAGS) {
            t("<img src=\"foo\"", "<img src=\"foo\" />");
            t("i>", "");
            t("<img src=\"foo\"/", "<img src=\"foo\" />");
            t(">", "");
            t("foo<b", "foo");
            t("b>foo", "<b>foo</b>");
            t("><b", "");
            t("b><", "");
            t("><b>", "");
        } else {
            t("<img src=\"foo\"", "&lt;img src=\"foo\"");
            t("b>", "b&gt;");
            t("<img src=\"foo\"/", "&lt;img src=\"foo\"/");
            t(">", "&gt;");
            t("foo<b", "foo&lt;b");
            t("b>foo", "b&gt;foo");
            t("><b", "&gt;&lt;b");
            t("b><", "b&gt;&lt;");
            t("><b>", "&gt;");
        }
    }

    @Test
    public void test_attributes() {
        t("<img src=foo>", "<img src=\"foo\" />", "<img src=\"foo\" />");
        t("<img asrc=foo>", "<img />", "<img />");
        t("<img src=test test>", "<img src=\"test\" />", "<img src=\"test\" />");
    }

    @Test
    public void test_disallow_script_tags() {
        t("<script>", "", "");

        if (HTMLInputFilter.ALWAYS_MAKE_TAGS) {
            t("<script", "");
        } else {
            t("<script", "&lt;script");
        }
        t("<script/>", "", "");
        t("</script>", "", "");
        t("<script woo=yay>", "", "");
        t("<script woo=\"yay\">", "", "");
        t("<script woo=\"yay>", "", "");
        t("<script woo=\"yay<b>", "", "");
        t("<script<script>>", ">", "");
        t("<<script>script<script>>", "<<script>script>", "script");
        t("<<script><script>>", "<<script>>", "");
        t("<<script>script>>", "<<script>script>>", "");
        t("<<script<script>>", "<<script<script>>", "");
    }

    @Test
    public void test_protocols() {
        t("<a href=\"http://foo\">bar</a>", "<a href=\"http://foo\">bar</a>");
        // we don't allow ftp. t("<a href=\"ftp://foo\">bar</a>", "<a href=\"ftp://foo\">bar</a>");
        t("<a href=\"mailto:foo\">bar</a>", "<a href=\"mailto:foo\">bar</a>");
        t("<a href=\"javascript:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java script:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java\tscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java\nscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java" + HTMLInputFilter.chr(1) + "script:foo\">bar</a>", "<a href=\"#foo\">bar</a>",
                "<a href=\"#foo\">bar</a>");
        t("<a href=\"jscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"vbscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"view-source:foo\">bar</a>", "<a href=\"#foo\">bar</a>", "<a href=\"#foo\">bar</a>");
    }

    @Test
    public void test_self_closing_tags() {
        t("<img src=\"a\">", "<img src=\"a\" />", "<img src=\"a\" />");
        t("<img src=\"a\">foo</img>", "<img src=\"a\" />foo", "<img src=\"a\" />foo");
        t("</img>", "", "");
    }

    @Test
    public void test_comments() {
        if (HTMLInputFilter.STRIP_COMMENTS) {
            t("<!-- a<b --->", "");
        } else {
            t("<!-- a<b --->", "<!-- a<b --->", "<!-- a&lt;b --->");
        }
    }
}
