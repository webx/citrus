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
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class TemplateVisitTests {
    private Template template;

    @Test
    public void render_methodNotFound_forText() throws Exception {
        class Visitor {
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering Text with", "characters",
                            "Visitor.visitText(String)"));
        }
    }

    @Test
    public void render_methodNotFound_forPlaceholder() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering ${title} at ",
                            "test6_real_case.txt: Line 3 Column 12", "Visitor.visitTitle(String...)"));
        }
    }

    @Test
    public void render_methodNotFound_forPlaceholder2() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle(Object out) throws IOException {
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering ${title} at ",
                            "test6_real_case.txt: Line 3 Column 12", "Visitor.visitTitle(String...)"));
        }
    }

    @Test
    public void render_methodNotFound_forPlaceholder3() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle(String s, Object o) throws IOException {
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering ${title} at ",
                            "test6_real_case.txt: Line 3 Column 12", "Visitor.visitTitle(String...)"));
        }
    }

    @Test
    public void render_methodNotFound_forTemplate() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle() throws IOException {
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering #items", " at ",
                            "test6_real_case.txt: Line 8 Column 7", "Visitor.visitItems(Template...)"));
        }
    }

    @Test
    public void render_methodNotFound_forTemplate2() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle() throws IOException {
            }

            public void visitItems() throws IOException {
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering #items", " at ",
                            "test6_real_case.txt: Line 8 Column 7", "Visitor.visitItems(Template...)"));
        }
    }

    @Test
    public void render_methodNotFound_forTemplate3() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle() throws IOException {
            }

            public void visitItems(Template tpl, Object o) throws IOException {
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering #items", " at ",
                            "test6_real_case.txt: Line 8 Column 7", "Visitor.visitItems(Template...)"));
        }
    }

    @Test
    public void render_visitorThrowsException() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle() throws IOException {
                throw new IllegalArgumentException();
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Error rendering ${title} at ",
                            "test6_real_case.txt: Line 3 Column 12"));
        }
    }

    @Test
    public void render_visitorThrowsIOException() throws Exception {
        @SuppressWarnings("unused")
        class Visitor {
            public void visitText(String text) {
            }

            public void visitTitle() throws IOException {
                throw new IOException();
            }
        }

        loadTemplate(null);

        try {
            template.accept(new Visitor());
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(IOException.class, "Error rendering ${title} at ",
                            "test6_real_case.txt: Line 3 Column 12"));
        }
    }

    @Test
    public void render_fallbackVisitor() throws Exception {
        loadTemplate("test6_real_case.txt");

        String expected = "";
        expected += "<html>\n";
        expected += "  <head>\n";
        expected += "    <title>${title}</title>\n";
        expected += "  </head>\n";
        expected += "  <body>\n";
        expected += "    <ul>\n";
        expected += "        <li>${content}</li>\n";
        expected += "        <li>${content}</li>\n";
        expected += "    </ul>\n";
        expected += "  </body>\n";
        expected += "</html>\n";

        String result = template.toString(new FallbackTextWriter<StringBuilder>());

        assertEquals(expected, result);
    }

    @Test
    public void render_fallbackVisitor2() throws Exception {
        loadTemplate("test6_real_case_2.txt");

        String expected = "";
        expected += "<html>\n";
        expected += "  <head>\n";
        expected += "    <title>${title}</title>\n";
        expected += "  </head>\n";
        expected += "  <body>\n";
        expected += "    <ul>\n";
        expected += "        <li>${content}</li>\n";
        expected += "    </ul>\n";
        expected += "  </body>\n";
        expected += "</html>\n";

        String result = template.toString(new FallbackTextWriter<StringBuilder>());

        assertEquals(expected, result);
    }

    @Test
    public void render_fallbackVisitor3() throws Exception {
        loadTemplate("test6_real_case_2.txt");

        String expected = "";
        expected += "<html>\n";
        expected += "  <head>\n";
        expected += "    <title>hello, world</title>\n";
        expected += "  </head>\n";
        expected += "  <body>\n";
        expected += "    <ul>\n";
        expected += "        <li></li>\n";
        expected += "    </ul>\n";
        expected += "  </body>\n";
        expected += "</html>\n";

        FallbackTextWriter<StringBuilder> visitor = new FallbackTextWriter<StringBuilder>();

        visitor.context().put("title", "hello, world");
        visitor.context().put("content", null);

        String result = template.toString(visitor);

        assertEquals(expected, result);
    }

    @Test
    public void render_appendable() throws Exception {
        @SuppressWarnings("unused")
        class Visitor extends TextWriter<StringBuilder> {
            private int count;

            public void visitTitle() throws IOException {
                out().append("myTitle");
            }

            public void visitItems(Template tpl) throws IOException {
                for (count = 1; count < 6; count++) {
                    tpl.accept(this);
                }
            }

            public void visitContent() throws IOException {
                out().append("count " + count);
            }
        }

        render(new Visitor());
    }

    @Test
    public void render_placeholder_stringArrayParams() throws Exception {
        @SuppressWarnings("unused")
        class Visitor extends TextWriter<StringBuilder> {
            private int count;

            public void visitTitle() throws IOException {
                out().append("myTitle");
            }

            public void visitItems(Template tpl) throws IOException {
                for (count = 1; count < 6; count++) {
                    tpl.accept(this);
                }
            }

            public void visitContent(String[] params) throws IOException {
                out().append("count " + count + " - ").append(formatGMT(params[0]));

                if (params.length > 1) {
                    out().append(" ").append(formatGMT(params[1]));
                }
            }
        }

        render(new Visitor(), " - 1970-01-01", null);
    }

    @Test
    public void render_placeholder_stringParams() throws Exception {
        @SuppressWarnings("unused")
        class Visitor extends TextWriter<StringBuilder> {
            private int count;

            public void visitTitle() throws IOException {
                out().append("myTitle");
            }

            public void visitItems(Template tpl) throws IOException {
                for (count = 1; count < 6; count++) {
                    tpl.accept(this);
                }
            }

            public void visitContent(String p1, String p2) throws IOException {
                out().append("count " + count + " - ").append(formatGMT(p1));

                if (p2 != null) {
                    out().append(" ").append(formatGMT(p2));
                }
            }
        }

        render(new Visitor(), " - 1970-01-01", null);
    }

    @Test
    public void render_group() throws Exception {
        @SuppressWarnings("unused")
        class Visitor extends TextWriter<StringBuilder> {
            private int count;

            public void visitTitle() throws IOException {
                out().append("myTitle");
            }

            public void visitItems(Template tpl) throws IOException {
                for (count = 1; count < 6; count++) {
                    tpl.accept(this);
                }
            }

            public void visitContent(String p1, String p2) throws IOException {
                out().append("count " + count + " - ").append(formatGMT(p1));

                if (p2 != null) {
                    out().append(" ").append(formatGMT(p2));
                }
            }
        }

        render(new Visitor(), " - 1970-01-01", null);
        render(new Visitor(), " - 1970-01-01 00:00", "test6_real_case_2.txt");
    }

    @Test
    public void render_group2() throws Exception {
        @SuppressWarnings("unused")
        class Visitor extends TextWriter<StringBuilder> {
            private int count;

            public void visitTitle() throws IOException {
                out().append("myTitle");
            }

            public void visitItems(Template tpl, Template tpl2) throws IOException {
                for (count = 1; count < 6; count++) {
                    tpl2.accept(this);
                }
            }

            public void visitContent(String p1, String p2) throws IOException {
                out().append("count " + count + " - ").append(formatGMT(p1));

                if (p2 != null) {
                    out().append(" ").append(formatGMT(p2));
                }
            }
        }

        render(new Visitor(), " - 1970-01-01 00:00", null);

        try {
            render(new Visitor(), " - 1970-01-01 00:00", "test6_real_case_2.txt");
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering #items", " at ",
                            "test6_real_case_2.txt: Line 8 Column 7", "Visitor.visitItems(Template...)"));
        }
    }

    @Test
    public void render_group3() throws Exception {
        @SuppressWarnings("unused")
        class Visitor extends TextWriter<StringBuilder> {
            private int count;

            public void visitTitle() throws IOException {
                out().append("myTitle");
            }

            public void visitItems(Template[] tpls) throws IOException {
                for (count = 1; count < 6; count++) {
                    tpls[1].accept(this);
                }
            }

            public void visitContent(String p1, String p2) throws IOException {
                out().append("count " + count + " - ").append(formatGMT(p1));

                if (p2 != null) {
                    out().append(" ").append(formatGMT(p2));
                }
            }
        }

        render(new Visitor(), " - 1970-01-01 00:00", null);

        try {
            render(new Visitor(), " - 1970-01-01 00:00", "test6_real_case_2.txt");
            fail();
        } catch (TemplateRuntimeException e) {
            assertThat(
                    e,
                    exception(NoSuchMethodException.class, "Error rendering #items", " at ",
                            "test6_real_case_2.txt: Line 8 Column 7", "Visitor.visitItems(Template...)"));
        }
    }

    private String formatGMT(String format) {
        DateFormat fmt = new SimpleDateFormat(format, Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return fmt.format(new Date(0));
    }

    private void render(TextWriter<StringBuilder> visitor) throws Exception {
        render(visitor, "", null);
    }

    private void render(TextWriter<StringBuilder> visitor, String contentExtra, String fileName) throws Exception {
        loadTemplate(fileName);

        String expected = "";
        expected += "<html>\n";
        expected += "  <head>\n";
        expected += "    <title>myTitle</title>\n";
        expected += "  </head>\n";
        expected += "  <body>\n";
        expected += "    <ul>\n";
        expected += "        <li>count 1" + contentExtra + "</li>\n";
        expected += "        <li>count 2" + contentExtra + "</li>\n";
        expected += "        <li>count 3" + contentExtra + "</li>\n";
        expected += "        <li>count 4" + contentExtra + "</li>\n";
        expected += "        <li>count 5" + contentExtra + "</li>\n";
        expected += "    </ul>\n";
        expected += "  </body>\n";
        expected += "</html>\n";

        String result = template.toString(visitor);

        assertEquals(expected, result);
    }

    protected void loadTemplate(String fileName) throws Exception {
        template = new Template(new File(srcdir, defaultIfEmpty(fileName, "test6_real_case.txt")));
    }
}
