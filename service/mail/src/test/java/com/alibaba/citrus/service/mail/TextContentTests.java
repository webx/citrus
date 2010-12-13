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
package com.alibaba.citrus.service.mail;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.mail.builder.content.TextContent;

/**
 * ²âÊÔ´¿ÎÄ±¾µÄcontent¡£
 * 
 * @author Michael Zhou
 */
public class TextContentTests extends AbstractMailBuilderTests {
    private TextContent content;

    @Test
    public void empty_content() throws Exception {
        initContent(new TextContent());

        assertEquals("text/plain", content.getContentType()); // default content type

        assertThat(getMessageAsText(), containsAllRegex( //
                "Content-Type: text/plain; charset=UTF-8" + REGEX_EOL, //
                "Content-Transfer-Encoding: 8bit" + REGEX_EOL, //
                REGEX_EOL + REGEX_EOL + "$"));
    }

    @Test
    public void with_content() throws Exception {
        initContent(new TextContent("hello, world"));

        assertEquals("text/plain", content.getContentType()); // default content type

        assertThat(getMessageAsText(), containsAllRegex( //
                "Content-Type: text/plain; charset=UTF-8" + REGEX_EOL, //
                "Content-Transfer-Encoding: 8bit" + REGEX_EOL, //
                "hello, world$"));
    }

    @Test
    public void with_content_and_type() throws Exception {
        initContent(new TextContent("hello, world", "text/html"));

        assertEquals("text/html", content.getContentType());

        assertThat(getMessageAsText(), containsAllRegex( //
                "Content-Type: text/html; charset=UTF-8" + REGEX_EOL, //
                "Content-Transfer-Encoding: 8bit" + REGEX_EOL, //
                "hello, world$"));
    }

    @Test
    public void toString_defaultContentType() {
        initContent(new TextContent("hello, world"));

        String result = "";

        result += "TextContent {\n";
        result += "  contentType = text/plain\n";
        result += "  text        = hello, world\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    @Test
    public void toString_specifiedContentType() {
        initContent(new TextContent("hello, world", "text/html"));

        String result = "";

        result += "TextContent {\n";
        result += "  contentType = text/html\n";
        result += "  text        = hello, world\n";
        result += "}";

        assertEquals(result, content.toString());
    }

    private void initContent(TextContent content) {
        this.content = content;
        builder.setContent(content);
    }
}
