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
package com.alibaba.citrus.util.internal.templatelite;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Test;

import com.alibaba.citrus.util.internal.templatelite.Template.Location;
import com.alibaba.citrus.util.internal.templatelite.Template.Placeholder;
import com.alibaba.citrus.util.internal.templatelite.Template.TemplateGroup;
import com.alibaba.citrus.util.internal.templatelite.Template.Text;

public class TemplateParserTests {
    private String source;
    private Template template;
    private Template[] templates;

    @Test
    public void fileNotExist() throws Exception {
        try {
            loadTemplate("notexist.txt", 0);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception(FileNotFoundException.class));
        }
    }

    @Test
    public void reloadTemplate() throws Exception {
        source = "temp.txt";

        // template from test1_simple.txt
        copyFile("test1_simple.txt", source, "temp.jar");

        File destFile = new File(destdir, source);
        File destJarFile = new File(destdir, "temp.jar");

        URL jarUrl = new URL("jar:" + destJarFile.toURI() + "!/temp.txt");

        Template[] templates = new Template[] { new Template(destFile), //
                new Template(destFile, "UTF-8"), //
                new Template(destFile.toURI().toURL()), //
                new Template(destFile.toURI().toURL(), "UTF-8"), //
                new Template(jarUrl), //
                new Template(jarUrl, "UTF-8"), //
                new Template(destFile.toURI().toURL().openStream(), null, "temp.txt"), //
                new Template(destFile.toURI().toURL().openStream(), "UTF-8", "temp.txt"), //
                new Template(new InputStreamReader(destFile.toURI().toURL().openStream()), "temp.txt"), //
        };

        boolean[] reloadable = new boolean[] { true, true, true, true, false, false, false, false, false };

        for (int i = 0; i < templates.length; i++) {
            Template t = templates[i];
            assertEquals(reloadable[i], t.source.source != null);
            assertTempalte(t, null, 1, null);

            assertEquals("hello,\n  world\n", t.toString(new FallbackTextWriter<StringBuilder>()));
        }

        Thread.sleep(1001); // 由于文件系统的timestamp实际上是以秒计的，所以必须等待1s以上，文件的lastModified才会变化。

        // template from test2_simple_placeholders.txt
        copyFile("test2_simple_placeholders.txt", source, "temp.jar");

        for (int i = 0; i < templates.length; i++) {
            Template t = templates[i];

            if (reloadable[i]) {
                assertEquals("a${123}${a123}${abc}${abc}${a}${a}${a}b\n",
                        t.toString(new FallbackTextWriter<StringBuilder>()));
                assertTempalte(t, null, 8, null);
            } else {
                assertEquals("hello,\n  world\n", t.toString(new FallbackTextWriter<StringBuilder>()));
                assertTempalte(t, null, 1, null);
            }
        }
    }

    private void copyFile(String src, String dest, String destJar) throws IOException {
        File destfile = new File(destdir, dest);
        File destJarFile = new File(destdir, destJar);

        io(new FileInputStream(new File(srcdir, src)), new FileOutputStream(destfile), true, true);

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destJarFile));

        jos.putNextEntry(new ZipEntry(dest));
        io(new FileInputStream(new File(srcdir, src)), jos, true, false);

        jos.closeEntry();
        jos.close();
    }

    @Test
    public void test1_simple() throws Exception {
        loadTemplate("test1_simple.txt", 1);

        assertText("hello,\n  world\n", template.nodes[0]);
    }

    @Test
    public void test2_simple_placeholders() throws Exception {
        loadTemplate("test2_simple_placeholders.txt", 8);

        int i = 0;
        assertText("a${123}", template.nodes[i++]);
        assertPlaceholder(template.nodes[i++], "a123", "Line 1 Column 8");
        assertText("${abc}", template.nodes[i++]);
        assertPlaceholder(template.nodes[i++], "abc", "Line 1 Column 22");
        assertPlaceholder(template.nodes[i++], "a", "Line 1 Column 30", new String[] { "123" }, "123");
        assertPlaceholder(template.nodes[i++], "a", "Line 1 Column 38");
        assertPlaceholder(template.nodes[i++], "a", "Line 1 Column 45", new String[] { "1", "2", "3" }, "1,2,3");
        assertText("b\n", template.nodes[i++]);
    }

    @Test
    public void test3_simple_subtmp() throws Exception {
        loadTemplate("test3_simple_subtmp.txt", 7);

        int i = 0;
        assertText("a\\#abc#123\n", template.nodes[i++]);
        assertTempalte(template.nodes[i++], "abc", 0, "Line 2 Column 3");
        assertText("\n", template.nodes[i++]);
        assertTempalte(template.nodes[i++], "def", 0, "Line 4 Column 3");
        assertText("a ", template.nodes[i++]);
        assertTempalte(template.nodes[i++], "ghi", 0, "Line 6 Column 3");
        assertText(" b\n", template.nodes[i++]);
    }

    @Test
    public void test4_miss_end() throws Exception {
        try {
            loadTemplate("test4_miss_end.txt", 0);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception("Unclosed tags: #def, #abc at", "Line 5"));
        }
    }

    @Test
    public void test4_miss_end_unknown_source() throws Exception {
        try {
            template = new Template(new FileInputStream(new File(srcdir, "test4_miss_end.txt")), null, null);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception("Unclosed tags: #def, #abc at [unknown source]: Line 5"));
        }
    }

    @Test
    public void test5_unmatched_end() throws Exception {
        try {
            loadTemplate("test5_unmatched_end.txt", 0);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception("Unmatched #end tag at", "Line 7 Column 1"));
        }
    }

    @Test
    public void test6_real_case() throws Exception {
        loadTemplate("test6_real_case.txt", 5);

        int i = 0;
        assertText("<html>\n" //
                + "  <head>\n" //
                + "    <title>", template.nodes[i++]);

        assertPlaceholder(template.nodes[i++], "title", "Line 3 Column 12");

        assertText("</title>\n" //
                + "  </head>\n" //
                + "  <body>\n" //
                + "    <ul>\n", template.nodes[i++]);

        assertTempalteGroup(template.nodes[i++], "items", 2, "Line 8 Column 7");

        assertText("    </ul>\n" //
                + "  </body>\n" //
                + "</html>\n", template.nodes[i++]);

        // -------------------------------
        setTemplateGroup(template.nodes[3]);
        setTempalte(templates[0]);

        i = 0;
        assertText("        <li>", template.nodes[i++]);
        assertPlaceholder(template.nodes[i++], "content", "Line 9 Column 13", new String[] { "yyyy-MM-dd" },
                "yyyy-MM-dd");
        assertText("</li>\n", template.nodes[i++]);

        // -------------------------------
        setTempalte(templates[1]);

        i = 0;
        assertText("        <li>", template.nodes[i++]);
        assertPlaceholder(template.nodes[i++], "content", "Line 11 Column 13", new String[] { "yyyy-MM-dd", "HH:mm" },
                "yyyy-MM-dd,HH:mm");
        assertText("</li>\n", template.nodes[i++]);
    }

    @Test
    public void test7_end_with_group() throws Exception {
        try {
            loadTemplate("test7_end_with_group.txt", -1);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception("Unexpected [] after #end tag at ", "Line 2 Column 6"));
        }
    }

    @Test
    public void test8_subtmp_group() throws Exception {
        loadTemplate("test8_subtmp_group.txt", 1);

        // root
        assertTempalte(template.nodes[0], "aaa", 1, "Line 1 Column 1");
        setTempalte(template.nodes[0]);

        // aaa
        assertTempalteGroup(template.nodes[0], "bbb", 2, "Line 2 Column 3");
        setTemplateGroup(template.nodes[0]);

        // bbb[0]
        assertTempalte(templates[0], "bbb", 0, "Line 2 Column 3");

        // bbb[1]
        assertTempalte(templates[1], "bbb", 1, "Line 3 Column 3");
        setTempalte(templates[1]);

        assertTempalteGroup(template.nodes[0], "ccc", 1, "Line 4 Column 5");
        setTemplateGroup(template.nodes[0]);

        // ccc[0]
        assertTempalte(templates[0], "ccc", 1, "Line 4 Column 5");
        setTempalte(templates[0]);

        assertTempalte(template.nodes[0], "ccc", 0, "Line 5 Column 7");
    }

    @Test
    public void test9_keywords() throws Exception {
        try {
            loadTemplate("test9_keywords.txt", 0);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception("Reserved name: text at", "Line 2 Column 15"));
        }
    }

    @Test
    public void test10_keywords_2() throws Exception {
        try {
            loadTemplate("test10_keywords_2.txt", 0);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception("Reserved name: placeholder at", "Line 2 Column 15"));
        }
    }

    private void loadTemplate(String file, int nodesCount) {
        source = file;
        template = new Template(new File(srcdir, file));

        assertTempalte(template, null, nodesCount, null);
    }

    private void setTempalte(Object node) {
        template = (Template) node;
    }

    private void setTemplateGroup(Object node) {
        templates = ((TemplateGroup) node).templates;
    }

    private void assertText(String text, Object node) {
        Text t = (Text) node;

        assertEquals(text, t.text);

        String str = t.toString();

        assertThat(str, containsAll("Text with ", "characters: "));
    }

    private void assertTempalte(Object node, String name, int nodeCount, String location) {
        Template template = (Template) node;

        assertEquals(name, template.getName());
        assertLocation(template.location, location);

        String str = template.toString();

        if (name == null) {
            name = "(template)";
        }

        assertThat(str, startsWith("#" + name + " with " + nodeCount + " nodes at "));
        assertLocation(str, location);
    }

    private void assertTempalteGroup(Object node, String name, int templateCount, String location) {
        TemplateGroup group = (TemplateGroup) node;

        assertEquals(name, group.name);
        assertEquals(templateCount, group.templates.length);
        assertLocation(group.location, location);

        String str = group.toString();

        assertThat(str, startsWith("#" + name + "[] with " + templateCount + " templates at "));
        assertLocation(str, location);
    }

    private void assertPlaceholder(Object node, String name, String location) {
        assertPlaceholder(node, name, location, new String[0], null);
    }

    private void assertPlaceholder(Object node, String name, String location, String[] params, String paramsString) {
        Placeholder placeholder = (Placeholder) node;

        assertEquals(name, placeholder.name);
        assertEquals(paramsString, placeholder.paramsString);
        assertArrayEquals(params, placeholder.params);
        assertLocation(placeholder.location, location);

        String str = placeholder.toString();

        if (paramsString == null) {
            assertThat(str, startsWith("${" + name + "}"));
        } else {
            assertThat(str, startsWith("${" + name + ":" + paramsString + "}"));
        }

        assertLocation(str, location);
    }

    private void assertLocation(String str, String location) {
        if (location != null) {
            assertThat(str, containsString(source + ": " + location));
        } else {
            assertThat(str, containsString(source));
        }
    }

    private void assertLocation(Location l, String location) {
        if (location != null) {
            assertThat(l.toString(), endsWith(source + ": " + location));
        } else {
            assertThat(l.toString(), endsWith(source));
        }
    }
}
