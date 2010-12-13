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
package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.net.URL;

import org.junit.Test;

public class WebxUtilTests {
    @Test
    public void getRevision() {
        String revision = WebxUtil.getRevision();
        assertNotNull(revision);
        assertTrue("Please submit WebxUtil.java to subversion and set a property on the file: "
                + "svn ps svn:keywords Revision WebxUtil.java", revision.matches("Revision: \\d+"));
    }

    @Test
    public void getVersion() throws Exception {
        String revision = WebxUtil.getRevision();

        assertEquals(revision, getVersion(null));
        assertEquals(revision, getVersion(new URL("file://localhost/abc.jar")));
        assertEquals(revision, getVersion(new URL("file://localhost/abc-test.jar")));
        assertEquals(revision, getVersion(new URL("file://localhost/abc1.2.3.test.jar")));

        assertEquals(revision, getVersion(new URL("file://localhost/abc-1.2.3.test")));
        assertEquals(revision, getVersion(new URL("file://localhost/abc-1.2.3.test.jar")));
        assertEquals(revision, getVersion(new URL("file://localhost/a/b/c-d-1.2.3.test.jar")));
        assertEquals(revision, getVersion(new URL("file://localhost/a/webx/c-d-1.2.3.test.jar")));

        assertEquals("1.2.3", getVersion(new URL("file://localhost/webxabc-1.2.3.test")));
        assertEquals("1.2.3.test", getVersion(new URL("file://localhost/WEBXabc-1.2.3.test.jar")));
        assertEquals("1.2.3.test", getVersion(new URL("file://localhost/a/b/WEBX-d-1.2.3.test.jar")));

        assertEquals("1.2.3", getVersion(new URL("jar:file://localhost/webxabc-1.2.3.test!/aaa/bbb/ccc.class")));
        assertEquals("1.2.3.test",
                getVersion(new URL("jar:file://localhost/WEBXabc-1.2.3.test.jar!/aaa/bbb/ccc.class")));
        assertEquals("1.2.3.test", getVersion(new URL(
                "jar:file://localhost/a/b/WEBX-d-1.2.3.test.jar!/aaa/bbb/ccc.class")));
    }

    private String getVersion(URL url) throws Exception {
        Method getVersion = getAccessibleMethod(WebxUtil.class, "getVersion", new Class<?>[] { URL.class });
        return (String) getVersion.invoke(null, url);
    }
}
