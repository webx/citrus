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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.mail.builder.content.HTMLTemplateContent;
import com.alibaba.citrus.util.StringUtil;

/**
 * 测试基于模板的html content。
 * 
 * @author Michael Zhou
 */
public class HTMLTemplateContentTests extends AbstractTemplateContentTests<HTMLTemplateContent> {
    @Test
    public void getContentType() {
        assertEquals("text/html", content.getContentType());

        content.setContentType("  text/xhtml ");
        assertEquals("text/xhtml", content.getContentType());
    }

    @Test
    public void addInlineResource() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.vm"), true, true);

        // id is null
        try {
            content.addInlineResource(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("The ID of inline resource was not specified"));
        }

        try {
            content.addInlineResource("  ", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("The ID of inline resource was not specified"));
        }

        // prefix is null
        try {
            content.addInlineResource("id", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("The prefix of inline resource was not specified"));
        }

        try {
            content.addInlineResource("id", "  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("The prefix of inline resource was not specified"));
        }

        // dup ids
        content.addInlineResource(" id ", "  prefix/ ");

        try {
            content.addInlineResource("id", "prefix2");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Duplicated ID \"id\" of inline resource"));
        }

        // normal case
        content.addInlineResource("id2", "prefix2");

        @SuppressWarnings("unchecked")
        Map<String, String> inlineResourceMap = (Map<String, String>) getFieldValue(content, "inlineResourceMap",
                Map.class);

        assertEquals(2, inlineResourceMap.size());
        assertEquals("prefix/", inlineResourceMap.get("id"));
        assertEquals("prefix2", inlineResourceMap.get("id2"));
    }

    @Test
    public void toString_() {
        // empty content
        String result = "";

        result += "HTMLTemplateContent {\n";
        result += "  contentType     = text/html\n";
        result += "  templateName    = <null>\n";
        result += "  inlineResources = {}\n";
        result += "}";

        assertEquals(result, content.toString());

        // with template, contentType, and inlineResources
        result = "";

        result += "HTMLTemplateContent {\n";
        result += "  contentType     = text/xhtml\n";
        result += "  templateName    = mytemplate.vm\n";
        result += "  inlineResources = {\n";
        result += "                      [1/1] img = images/\n";
        result += "                    }\n";
        result += "}";

        content.setContentType("text/xhtml");
        content.setTemplate("mytemplate.vm");
        content.addInlineResource("img", "images/");

        assertEquals(result, content.toString());
    }

    @Test
    public void simpleHtml_vm() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.vm"), true, false);
        assert_TextHtml_mytemplate_noPullTools("velocity");
    }

    @Test
    public void simpleHtml_ftl() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.ftl"), true, false);
        assert_TextHtml_mytemplate_noPullTools("freemarker");
    }

    @Test
    public void simpleHtml_vm_contentType() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.vm", "text/plain"), true, false);
        assert_TextPlain_mytemplate_noPullTools("velocity");
    }

    @Test
    public void simpleHtml_ftl_contentType() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.ftl", "text/plain"), true, false);
        assert_TextPlain_mytemplate_noPullTools("freemarker");
    }

    @Test
    public void simpleHtml_vm_pull() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.vm"), true, true);
        builder.setAttribute("hello", "world");
        assert_TextHtml_mytemplate_pullTools_and_vars("velocity");
    }

    @Test
    public void simpleHtml_ftl_pull() throws Exception {
        initContent(new HTMLTemplateContent("mail/mytemplate.ftl"), true, true);
        builder.setAttribute("hello", "world");
        assert_TextHtml_mytemplate_pullTools_and_vars("freemarker");
    }

    @Test
    public void complexHtml_noResourceLoader() throws Exception {
        initContent(new HTMLTemplateContent("mail/complexhtml.vm"), true, true);
        content.addInlineResource("image", "/");

        try {
            getMessageAsText();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no resourceLoader"));
        }
    }

    @Test
    public void complexHtml_vm() throws Exception {
        initContent(new HTMLTemplateContent("mail/complexhtml.vm"), true, true);
        content.setResourceLoader(factory);
        content.addInlineResource("image", "/");

        assert_TextHtml_complex("velocity");
    }

    @Test
    public void complexHtml_ftl() throws Exception {
        initContent(new HTMLTemplateContent("mail/complexhtml.ftl"), true, true);
        content.setResourceLoader(factory);
        content.addInlineResource("image", "/");

        assert_TextHtml_complex("freemarker");
    }

    @Test
    public void complexHtml_imageNotFound() throws Exception {
        initContent(new HTMLTemplateContent("mail/imageNotFound.vm"), true, true);
        content.setResourceLoader(factory);
        content.addInlineResource("image", "/");

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(e, exception("Could not find resource \"/notExist.gif\""));
        }
    }

    @Test
    public void complexHtml_streamOnlyResource() throws Exception {
        initContent(new HTMLTemplateContent("mail/streamOnlyResource.vm"), true, true);
        content.setResourceLoader(factory);
        content.addInlineResource("image", "/asStream");

        assert_TextHtml_streamOnlyResource();
    }

    @Test
    public void complexHtml_dup_fileNames() throws Exception {
        initContent(new HTMLTemplateContent("mail/dup_fileNames.vm"), true, true);
        content.setResourceLoader(factory);
        content.addInlineResource("image1", "/");
        content.addInlineResource("image2", "/mailres");

        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: multipart/related;"));
        assertThat(eml, containsRegex("Content-Type: text/html; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("cid:"));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=java.gif" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=java1.gif" + REGEX_EOL)); // dup names
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=emptyfile" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=emptyfile1" + REGEX_EOL)); // dup names, no ext
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

    private void assert_TextHtml_mytemplate_pullTools_and_vars(String extra) throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: text/html; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("Constant\\[MailConstant\\]"));
        assertThat(eml, containsRegex("com.alibaba.citrus.util.StringUtil@"));
        assertThat(eml, containsRegex("world" + REGEX_EOL));
        assertThat(eml, containsRegex(extra + REGEX_EOL));
    }

    private void assert_TextHtml_complex(String extra) throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: multipart/related;"));
        assertThat(eml, containsRegex("Content-Type: text/html; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("1  2  3  4  5  6  7  8  9  10"));
        assertThat(eml, containsRegex("Content-Type: image/gif"));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=java.gif"));
        assertThat(eml, containsRegex("Content-Type: image/jpeg"));
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=bible.jpg"));
        assertThat(eml, containsRegex(extra + REGEX_EOL));

        // 有且仅有4个content type
        assertEquals(4, StringUtil.countMatches(eml, "Content-Type:"));

        // url1和url2指向同一个image，和url3不同
        Pattern pattern = Pattern.compile("<img src=\"cid:([^\"]+)\"/>");
        Matcher matcher = pattern.matcher(eml);

        assertTrue(matcher.find());
        String cid1 = matcher.group(1);

        assertTrue(matcher.find());
        String cid2 = matcher.group(1);

        assertTrue(matcher.find());
        String cid3 = matcher.group(1);

        assertFalse(matcher.find());
        assertEquals(cid1, cid2);
        assertThat(cid1, not(equalTo(cid3)));
    }

    private void assert_TextHtml_streamOnlyResource() throws Exception {
        String eml = getMessageAsText();

        assertThat(eml, containsRegex("Content-Type: multipart/related;"));
        assertThat(eml, containsRegex("Content-Type: text/html; charset=UTF-8" + REGEX_EOL));
        assertThat(eml, containsRegex("Content-Transfer-Encoding: 8bit" + REGEX_EOL));
        assertThat(eml, containsRegex("cid:"));
        assertThat(eml, containsRegex("Content-Type: application/octet-stream")); // 不支持url，所以取不到content type
        assertThat(eml, containsRegex("Content-Disposition: inline; filename=java.gif"));

        // 有且仅有3个content type
        assertEquals(3, StringUtil.countMatches(eml, "Content-Type:"));
    }

    @Override
    protected HTMLTemplateContent createContent() {
        return new HTMLTemplateContent();
    }
}
