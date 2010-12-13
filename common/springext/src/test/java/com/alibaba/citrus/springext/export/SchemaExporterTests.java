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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;
import com.alibaba.citrus.test.TestEnvStatic;

public class SchemaExporterTests {
    protected SchemaExporter exporter;
    protected ConfigurationPointsImpl cps;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() {
        cps = new ConfigurationPointsImpl(null, "TEST-INF/test9/cps");
        exporter = createExporter();
    }

    protected SchemaExporter createExporter() {
        return new SchemaExporter(cps);
    }

    @Test
    public void test9_tree() throws IOException {
        String tree = exporter.getRootEntry().tree();

        System.out.println("--");
        System.out.println(tree);

        String result = "\n";
        result += "+---my-plugins.xsd\n";
        result += "+---my-services.xsd\n";
        result += "+---my-services-1.0.xsd\n";
        result += "+---my-services-2.0.xsd\n";
        result += "\\---my/\n";
        result += "    +---plugins/\n";
        result += "    |   \\---plugin1.xsd\n";
        result += "    \\---services/\n";
        result += "        +---service1.xsd\n";
        result += "        +---service1-1.0.xsd\n";
        result += "        +---service2.xsd\n";
        result += "        +---service2-2.0.xsd\n";
        result += "        \\---service3.xsd\n";

        assertEquals(result, tree);

        tree = exporter.getEntry("my/plugins/").tree();

        System.out.println("--");
        System.out.println(tree);

        result = "plugins/\n";
        result += "\\---plugin1.xsd\n";

        assertEquals(result, tree);
    }

    @Test
    public void test9_getEntry() {
        SchemaExporter.Entry entry;

        entry = exporter.getEntry("my/plugins/");
        assertEquals("my/plugins/", entry.getPath());
        assertEquals("plugins/", entry.getName());
        assertTrue(entry.isDirectory());
        assertFalse(entry.isRoot());
        assertEquals(1, entry.getSubEntries().size());

        entry = exporter.getEntry("my/services/service1.xsd");
        assertEquals("my/services/service1.xsd", entry.getPath());
        assertEquals("service1.xsd", entry.getName());
        assertFalse(entry.isDirectory());
        assertFalse(entry.isRoot());
        assertEquals(0, entry.getSubEntries().size());
    }

    @Test
    public void test9_containsSchemaWithTargetNamespace() {
        SchemaExporter.Entry entry;

        entry = exporter.getRootEntry();
        assertTrue(entry.containsSchemaWithTargetNamespace());

        entry = exporter.getEntry("my-plugins.xsd");
        assertTrue(entry.containsSchemaWithTargetNamespace());

        entry = exporter.getEntry("my/plugins/");
        assertFalse(entry.containsSchemaWithTargetNamespace());

        entry = exporter.getEntry("my/services/service1.xsd");
        assertFalse(entry.containsSchemaWithTargetNamespace());
    }
}
