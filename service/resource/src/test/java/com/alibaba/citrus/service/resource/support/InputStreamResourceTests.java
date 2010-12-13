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

import java.io.InputStream;

import org.junit.Test;

/**
 * ≤‚ ‘<code>InputStreamResource</code>°£
 * 
 * @author Michael Zhou
 */
public class InputStreamResourceTests extends AbstractResourceTests {
    @Test
    public void nullFile() throws Exception {
        try {
            new InputStreamResource(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("stream"));
        }
    }

    @Test
    public void existFile() throws Exception {
        InputStream stream = existsFile.toURI().toURL().openStream();
        resource = new InputStreamResource(stream);

        assertNull(resource.getURL());
        assertNull(resource.getFile());
        assertEquals("test", readText(resource.getInputStream(), null, true));
        assertTrue(resource.exists());
        assertEquals(0, resource.lastModified());

        assertThat(resource.toString(), containsAll("InputStreamResource[InputStream: ", "]"));

        assertHashAndEquals(resource, new InputStreamResource(stream));
    }
}
