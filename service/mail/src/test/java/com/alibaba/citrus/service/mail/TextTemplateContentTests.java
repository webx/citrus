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

import com.alibaba.citrus.service.mail.builder.content.TextTemplateContent;

/**
 * 测试基于模板的文本content。
 * 
 * @author Michael Zhou
 */
public class TextTemplateContentTests extends AbstractTemplateContentTests<TextTemplateContent> {
    @Test
    public void getContentType() {
        assertEquals("text/plain", content.getContentType());

        content.setContentType("  text/html ");
        assertEquals("text/html", content.getContentType());
    }

    @Test
    public void toString_() {
        // empty content
        String result = "";

        result += "TextTemplateContent {\n";
        result += "  contentType  = text/plain\n";
        result += "  templateName = <null>\n";
        result += "}";

        assertEquals(result, content.toString());

        // with template and contentType
        result = "";

        result += "TextTemplateContent {\n";
        result += "  contentType  = text/html\n";
        result += "  templateName = mytemplate.vm\n";
        result += "}";

        content.setContentType("text/html");
        content.setTemplate("mytemplate.vm");

        assertEquals(result, content.toString());
    }

    @Test
    public void mytemplate_vm() throws Exception {
        initContent(new TextTemplateContent("mail/mytemplate.vm"), true, false);
        assert_TextPlain_mytemplate_noPullTools("velocity");
    }

    @Test
    public void mytemplate_ftl() throws Exception {
        initContent(new TextTemplateContent("mail/mytemplate.ftl"), true, false);
        assert_TextPlain_mytemplate_noPullTools("freemarker");
    }

    @Test
    public void mytemplate_vm_contentType() throws Exception {
        initContent(new TextTemplateContent("mail/mytemplate.vm", "text/html"), true, false);
        assert_TextHtml_mytemplate_noPullTools("velocity");
    }

    @Test
    public void mytemplate_ftl_contentType() throws Exception {
        initContent(new TextTemplateContent("mail/mytemplate.ftl", "text/html"), true, false);
        assert_TextHtml_mytemplate_noPullTools("freemarker");
    }

    @Test
    public void mytemplate_vm_pull() throws Exception {
        initContent(new TextTemplateContent("mail/mytemplate.vm"), true, true);
        builder.setAttribute("hello", "world");
        assert_TextPlain_mytemplate_pullTools_and_vars("velocity");
    }

    @Test
    public void mytemplate_ftl_pull() throws Exception {
        initContent(new TextTemplateContent("mail/mytemplate.ftl"), true, true);
        builder.setAttribute("hello", "world");
        assert_TextPlain_mytemplate_pullTools_and_vars("freemarker");
    }

    private void assert_TextPlain_mytemplate_noPullTools(String extra) throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: text/plain; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("\\$myconst" + REGEX_EOL));
        assertThat(eml, containsRegex("\\$stringUtil" + REGEX_EOL));
        assertThat(eml, containsRegex("\\$hello" + REGEX_EOL));
        assertThat(eml, containsRegex(extra + REGEX_EOL));
    }

    private void assert_TextHtml_mytemplate_noPullTools(String extra) throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: text/html; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("\\$myconst" + REGEX_EOL));
        assertThat(eml, containsRegex("\\$stringUtil" + REGEX_EOL));
        assertThat(eml, containsRegex("\\$hello" + REGEX_EOL));
        assertThat(eml, containsRegex(extra + REGEX_EOL));
    }

    private void assert_TextPlain_mytemplate_pullTools_and_vars(String extra) throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: text/plain; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("Constant\\[MailConstant\\]"));
        assertThat(eml, containsRegex("com.alibaba.citrus.util.StringUtil@"));
        assertThat(eml, containsRegex("world" + REGEX_EOL));
        assertThat(eml, containsRegex(extra + REGEX_EOL));
    }

    @Override
    protected TextTemplateContent createContent() {
        return new TextTemplateContent();
    }
}
