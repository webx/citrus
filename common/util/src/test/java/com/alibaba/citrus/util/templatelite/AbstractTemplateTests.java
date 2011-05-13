package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.util.io.ByteArrayInputStream;
import com.alibaba.citrus.util.templatelite.Template.Location;

public abstract class AbstractTemplateTests {
    protected final File srcdir = new File(TestEnvStatic.srcdir, "templates");
    protected String source;
    protected Template template;
    protected TemplateParseException parseError;
    protected TemplateRuntimeException runtimeError;

    protected void loadTemplateFailure(byte[] content, String systemId) {
        try {
            loadTemplate(content, systemId, -1, -1, -1);
            fail();
        } catch (TemplateParseException e) {
            this.parseError = e;
        }
    }

    protected void loadTemplate(String file, int nodesCount, int templatesCount, int paramsCount) {
        source = file;
        template = new Template(new File(srcdir, file));

        assertTemplate(template, null, nodesCount, templatesCount, paramsCount, null);
    }

    protected void loadTemplate(byte[] content, String systemId, int nodesCount, int templatesCount, int paramsCount) {
        source = systemId;
        template = new Template(new ByteArrayInputStream(content), systemId);

        assertTemplate(template, null, nodesCount, templatesCount, paramsCount, null);
    }

    protected void assertTemplate(Template template, String name, int nodesCount, int templatesCount, int paramsCount,
                                  String location) {
        assertEquals(name, template.getName());
        assertLocation(template.location, location);

        String str = template.toString();

        if (name == null) {
            name = "(template)";
        }

        assertThat(str, startsWith("#" + name + " with " + nodesCount + " nodes at "));
        assertLocation(str, location);

        assertEquals(nodesCount, template.nodes.length);
        assertEquals(templatesCount, template.subtemplates.size());
        assertEquals(paramsCount, template.params.size());
    }

    protected void assertLocation(String str, String location) {
        if (location != null) {
            assertThat(str, containsString(source + ": " + location));
        } else {
            assertThat(str, containsString(source));
        }
    }

    protected void assertLocation(Location l, String location) {
        if (location != null) {
            assertThat(l.toString(), endsWith(source + ": " + location));
        } else {
            assertThat(l.toString(), endsWith(source));
        }
    }

    protected URL copyFileToJar(String src, String dest, String destJar) throws IOException {
        File destfile = new File(destdir, dest);
        File destJarFile = new File(destdir, destJar);

        io(new FileInputStream(new File(srcdir, src)), new FileOutputStream(destfile), true, true);

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destJarFile));

        jos.putNextEntry(new ZipEntry(dest));
        io(new FileInputStream(new File(srcdir, src)), jos, true, false);

        jos.closeEntry();
        jos.close();

        return new URL("jar:" + destJarFile.toURL() + "!/" + dest);
    }

    protected void acceptFailure(Object visitor) {
        try {
            template.accept(visitor);
            fail();
        } catch (TemplateRuntimeException e) {
            runtimeError = e;
        }
    }
}
