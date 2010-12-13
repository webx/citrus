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
package com.alibaba.citrus.service.resource.support;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.BeforeClass;

import com.alibaba.citrus.service.resource.Resource;

public abstract class AbstractResourceTests {
    protected static File existsFile;
    protected static File notExistsFile;
    protected static File directory;
    protected static File jarFile;
    protected static URL jarURL;
    protected Resource resource;

    @BeforeClass
    public static void init() throws Exception {
        existsFile = new File(srcdir, "test.txt");
        notExistsFile = new File(srcdir, "nonexist.txt");
        directory = srcdir;

        jarFile = new File(destdir, "test.jar");
        jarURL = new URL("jar:" + jarFile.toURI().toURL() + "!/test.txt");

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile));
        ZipEntry entry = new ZipEntry("test.txt");
        entry.setTime(existsFile.lastModified());
        jos.putNextEntry(entry);
        io(new FileInputStream(existsFile), jos, true, true);
    }

    protected void assertHashAndEquals(Resource r1, Resource r2) {
        assertNotSame(r1, r2);

        assertEquals(r1.hashCode(), r2.hashCode());
        assertEquals(r1, r2);
    }
}
