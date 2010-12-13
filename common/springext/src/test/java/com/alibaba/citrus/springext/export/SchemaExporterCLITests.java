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
package com.alibaba.citrus.springext.export;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import com.alibaba.citrus.test.TestUtil;
import com.alibaba.citrus.util.io.StreamUtil;

public class SchemaExporterCLITests extends SchemaExporterTests {
    @Override
    protected SchemaExporter createExporter() {
        return new SchemaExporterCLI(cps);
    }

    @Test
    public void test9_save() throws IOException {
        ((SchemaExporterCLI) exporter).saveTo(new File(destdir, "schemas1"), null); // 未指定prefix

        assertFile(new File(destdir, "schemas1/my-plugins.xsd"), "schemaLocation=\"my/plugins/plugin1.xsd\"");
        assertFile(new File(destdir, "schemas1/my/plugins/plugin1.xsd"));
        assertFile(new File(destdir, "schemas1/my-services.xsd"), "schemaLocation=\"my/services/service1.xsd\"");
        assertFile(new File(destdir, "schemas1/my-services-1.0.xsd"), "schemaLocation=\"my/services/service1-1.0.xsd\"");
        assertFile(new File(destdir, "schemas1/my-services-2.0.xsd"));
        assertFile(new File(destdir, "schemas1/my/services/service1.xsd"));
        assertFile(new File(destdir, "schemas1/my/services/service1-1.0.xsd"));
        assertFile(new File(destdir, "schemas1/my/services/service2.xsd"));
        assertFile(new File(destdir, "schemas1/my/services/service2-2.0.xsd"));
        assertFile(new File(destdir, "schemas1/my/services/service3.xsd"));
    }

    @Test
    public void complete_save() throws IOException {
        SchemaExporterCLI exporter = new SchemaExporterCLI();

        exporter.saveTo(new File(destdir, "schemas2")); // 一个参数的版本，将使用basedir作为prefix

        String prefix = new File(destdir, "schemas2").toURI().toString();

        // from configuration points and contributions
        assertFile(new File(destdir, "schemas2/services.xsd"), prefix + "services/container.xsd");
        assertFile(new File(destdir, "schemas2/services/container.xsd"), prefix
                + "www.springframework.org/schema/beans/spring-beans.xsd");

        assertFile(new File(destdir, "schemas2/services-tools.xsd"), prefix + "services/tools/dateformat.xsd");
        assertFile(new File(destdir, "schemas2/services/tools/dateformat.xsd"), prefix
                + "www.springframework.org/schema/beans/spring-beans.xsd");

        // from spring，注意版本号的匹配
        assertFile(new File(destdir, "schemas2/www.springframework.org/schema/aop/spring-aop-2.5.xsd"), prefix
                + "www.springframework.org/schema/beans/spring-beans-2.5.xsd");

        assertFile(new File(destdir, "schemas2/www.springframework.org/schema/aop/spring-aop-2.0.xsd"), prefix
                + "www.springframework.org/schema/beans/spring-beans-2.0.xsd");

        assertFile(new File(destdir, "schemas2/www.springframework.org/schema/aop/spring-aop.xsd"), prefix
                + "www.springframework.org/schema/beans/spring-beans.xsd");

        assertFile(new File(destdir, "schemas2/www.springframework.org/schema/beans/spring-beans-2.5.xsd"));
        assertFile(new File(destdir, "schemas2/www.springframework.org/schema/beans/spring-beans-2.0.xsd"));
        assertFile(new File(destdir, "schemas2/www.springframework.org/schema/beans/spring-beans.xsd"));

        // from others, invalid xml doc (content is "dummy")
        assertFile(new File(destdir, "schemas2/www.alibaba.com/schema/tests.xsd"));

    }

    private void assertFile(File file, String... strs) throws IOException {
        assertTrue("File does not exist: " + file.getAbsolutePath(), file.exists());
        assertTrue("File is empty: " + file.getAbsolutePath(), file.length() > 0);

        if (!isEmptyArray(strs)) {
            String content = StreamUtil.readText(new FileInputStream(file), "UTF-8", true);

            assertThat(content, TestUtil.containsAll(strs));
        }
    }
}
