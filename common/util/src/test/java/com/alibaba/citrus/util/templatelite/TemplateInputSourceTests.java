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
package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Test;

import com.alibaba.citrus.util.templatelite.Template.InputSource;

public class TemplateInputSourceTests extends AbstractTemplateTests {
    private InputSource inputSource;

    @Test
    public void charsetDetection() throws IOException {
        String content;

        // -----------------
        content = "";
        content += "\n";
        content += "  ## comment\n";
        content += "  \\#@charset hello\n"; // 这行导致charset检查结束
        content += "#@otherparam value value\n";
        content += " #@charset GB18030\n";

        assertEquals("default", detectCharset(content));

        // -----------------
        content = "";
        content += "\n"; // 忽略空行
        content += "  \r\n"; // 忽略空行
        content += "  ## comment\n"; // 忽略注释
        content += "#@otherparam value value\n"; // 忽略其它参数
        content += " #@charset GB18030\n"; // 匹配charset

        assertEquals("GB18030", detectCharset(content));

        // -----------------
        content = "";
        content += "##@charset 8859_1\n"; // 忽略注释
        content += " #@charset GB18030##comment\n"; // 匹配charset##comment

        assertEquals("GB18030", detectCharset(content));

        // -----------------
        content = "";
        content += "######\n"; // 无内容

        assertEquals("default", detectCharset(content));

        // -----------------
        content = repeat("#", 1024 * 4) + "\n";
        content += " #@charset GB18030\n"; // 超过了readlimit 

        assertEquals("default", detectCharset(content));
    }

    private String detectCharset(String content) throws IOException {
        return Template.InputSource.detectCharset(
                new BufferedInputStream(new ByteArrayInputStream(content.getBytes("ISO-8859-1"))), "default");
    }

    @Test
    public void getReader() throws IOException {
        File f = new File(srcdir, "test05_param_gbk.txt");

        // file as input source
        inputSource = new InputSource(f);
        assertReader("GBK", f, f.toURI().toString());

        // file: url as input source
        inputSource = new InputSource(f.toURL());
        assertReader("GBK", f, f.toURL().toExternalForm());

        // url as input source
        URL jar = copyFileToJar("test05_param_gbk.txt", "gbk.txt", "test.jar");
        inputSource = new InputSource(jar);
        assertReader("GBK", null, jar.toExternalForm());

        // stream as input source
        inputSource = new InputSource(new ByteArrayInputStream("#@charset UTF-8\n\nhello".getBytes("UTF-8")),
                "utf8.txt");
        assertReader("UTF-8", null, "utf8.txt");

        // reader as input source
        inputSource = new InputSource(new StringReader("#@charset UTF-8\n\nhello"), "utf8.txt");
        assertReader(null, null, "utf8.txt");
    }

    private void assertReader(String charset, File source, String systemId) throws IOException {
        Reader reader = null;

        try {
            reader = inputSource.getReader();

            assertNotNull(reader);

            if (charset != null) {
                assertTrue(reader instanceof InputStreamReader);
                assertEquals(charset, Charset.forName(((InputStreamReader) reader).getEncoding()).name()); // canonical name
            }

            assertEquals(source, inputSource.source);
            assertEquals(systemId, inputSource.systemId);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Test
    public void fileNotExist() throws Exception {
        try {
            loadTemplate("notexist.txt", 0, 0, 0);
            fail();
        } catch (TemplateParseException e) {
            assertThat(e, exception(FileNotFoundException.class));
        }
    }

    @Test
    public void reloadTemplate() throws Exception {
        source = "temp.txt";

        // template from test07_reload_1.txt
        URL jarUrl = copyFileToJar("test07_reload_1.txt", source, "temp.jar");
        File destFile = new File(destdir, source);

        Template[] templates = new Template[] { new Template(destFile), //
                new Template(destFile.toURI().toURL()), //
                new Template(jarUrl), //
                new Template(destFile.toURI().toURL().openStream(), "temp.txt"), //
                new Template(new InputStreamReader(destFile.toURI().toURL().openStream()), "temp.txt"), //
        };

        boolean[] reloadable = new boolean[] { true, true, false, false, false };

        for (int i = 0; i < templates.length; i++) {
            Template t = templates[i];
            assertEquals(reloadable[i], t.source.source != null);
            assertTemplate(t, null, 2, 0, 0, null);

            assertEquals("abc\n${abc}", t.renderToString(new FallbackTextWriter<StringBuilder>()));
        }

        // 由于文件系统的timestamp实际上是以秒计的，所以必须等待1s以上，文件的lastModified才会变化。
        Thread.sleep(1001);

        // template from test07_reload_2.txt
        copyFileToJar("test07_reload_2.txt", source, "temp.jar");

        for (int i = 0; i < templates.length; i++) {
            Template t = templates[i];

            if (reloadable[i]) {
                assertEquals("xyz\n${xyz}", t.renderToString(new FallbackTextWriter<StringBuilder>()));
                assertTemplate(t, null, 2, 0, 0, null);
            } else {
                assertEquals("abc\n${abc}", t.renderToString(new FallbackTextWriter<StringBuilder>()));
                assertTemplate(t, null, 2, 0, 0, null);
            }
        }
    }
}
