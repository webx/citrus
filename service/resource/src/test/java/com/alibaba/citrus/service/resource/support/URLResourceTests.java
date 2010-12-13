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
 * 测试<code>URLResource</code>。
 * 
 * @author Michael Zhou
 */
public class URLResourceTests extends AbstractResourceTests {
    @Test
    public void nullFile() throws Exception {
        try {
            new URLResource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("url"));
        }
    }

    @Test
    public void existFile() throws Exception {
        resource = new URLResource(existsFile.toURI().toURL());

        assertEquals(existsFile.toURI().toURL(), resource.getURL());
        assertEquals(existsFile, resource.getFile());
        assertEquals("test", readText(resource.getInputStream(), null, true));
        assertTrue(resource.exists());
        assertEquals(existsFile.lastModified(), resource.lastModified());

        assertThat(resource.toString(), containsAll("URLResource[file: ", "test.txt]"));

        assertHashAndEquals(resource, new URLResource(existsFile.toURI().toURL()));
    }

    @Test
    public void nonFileURL() throws Exception {
        resource = new URLResource(jarURL);

        assertEquals(jarURL, resource.getURL());
        assertEquals(null, resource.getFile());
        assertEquals("test", readText(resource.getInputStream(), null, true));
        assertTrue(resource.exists());

        // == jarFile's lastModified，从URL中取得的时间戳只能精确到秒
        assertEquals(jarFile.lastModified() / 1000, resource.lastModified() / 1000);

        assertThat(resource.toString(), containsAll("URLResource[URL: ", "test.jar!/test.txt]"));

        assertHashAndEquals(resource, new URLResource(jarURL));
    }

    @Test
    public void nonExistFile() throws Exception {
        resource = new URLResource(notExistsFile.toURI().toURL());

        assertEquals(notExistsFile.toURI().toURL(), resource.getURL());
        assertEquals(notExistsFile, resource.getFile());
        assertNull(resource.getInputStream());
        assertFalse(resource.exists());
        assertEquals(0, resource.lastModified());

        assertThat(resource.toString(), containsAll("URLResource[file or directory does not exist: ", "nonexist.txt]"));

        assertHashAndEquals(resource, new URLResource(notExistsFile.toURI().toURL()));
    }

    @Test
    public void directory() throws Exception {
        resource = new URLResource(directory.toURI().toURL());

        assertEquals(directory.toURI().toURL(), resource.getURL());
        assertEquals(directory, resource.getFile());
        assertNull(resource.getInputStream());
        assertTrue(resource.exists());
        assertEquals(directory.lastModified(), resource.lastModified());

        assertThat(resource.toString(), containsAll("URLResource[directory: ", "config]"));

        assertHashAndEquals(resource, new URLResource(directory.toURI().toURL()));
    }
}
