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

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * ≤‚ ‘<code>FileResource</code>°£
 * 
 * @author Michael Zhou
 */
public class FileResourceTests extends AbstractResourceTests {
    @Test
    public void nullFile() throws Exception {
        try {
            new FileResource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("file"));
        }
    }

    @Test
    public void existFile() throws Exception {
        resource = new FileResource(existsFile);

        assertEquals(existsFile.toURI().toURL(), resource.getURL());
        assertSame(existsFile, resource.getFile());
        assertEquals("test", readText(resource.getInputStream(), null, true));
        assertTrue(resource.exists());
        assertEquals(existsFile.lastModified(), resource.lastModified());

        assertThat(resource.toString(), containsAll("FileResource[file: ", "test.txt]"));

        assertHashAndEquals(resource, new FileResource(existsFile));
    }

    @Test
    public void nonExistFile() throws Exception {
        resource = new FileResource(notExistsFile);

        assertEquals(notExistsFile.toURI().toURL(), resource.getURL());
        assertSame(notExistsFile, resource.getFile());
        assertNull(resource.getInputStream());
        assertFalse(resource.exists());
        assertEquals(0, resource.lastModified());

        assertThat(resource.toString(), containsAll("FileResource[file or directory does not exist: ", "nonexist.txt]"));

        assertHashAndEquals(resource, new FileResource(notExistsFile));
    }

    @Test
    public void directory() throws Exception {
        resource = new FileResource(directory);

        assertEquals(directory.toURI().toURL(), resource.getURL());
        assertSame(directory, resource.getFile());
        assertNull(resource.getInputStream());
        assertTrue(resource.exists());
        assertEquals(directory.lastModified(), resource.lastModified());

        assertThat(resource.toString(), containsAll("FileResource[directory: ", "config]"));

        assertHashAndEquals(resource, new FileResource(directory));
    }
}
