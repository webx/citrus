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
package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.resource.ResourceLister;

public class WebappLoaderTests extends AbstractResourceLoaderTests<WebappResourceLoader> {
    @BeforeClass
    public static void initClass() throws Exception {
        initServlet();
    }

    @Before
    public void init() throws Exception {
        loader = new WebappResourceLoader();
        loader.setServletContext(servletContext);
        loader.init(null);
    }

    @Test
    public void noServletContext() throws Exception {
        loader = new WebappResourceLoader();
        loader.init(null);

        assertResourceLoader("/webroot", "", false);
        assertResourceLoader("/webroot/test.txt", "test.txt", false);
        assertResourceLoader("/webroot/WEB-INF/", "WEB-INF", false);
        assertResourceLoader("/webroot/WEB-INF/web.xml", "WEB-INF/web.xml", false);

        assertResourceLister("/webroot/WEB-INF/", "WEB-INF", false);
    }

    @Test
    public void getResource() throws Exception {
        assertResourceLoader("/webroot", "", true);
        assertResourceLoader("/webroot/test.txt", "test.txt", true);
        assertResourceLoader("/webroot/WEB-INF/", "WEB-INF", true);
        assertResourceLoader("/webroot/WEB-INF/web.xml", "WEB-INF/web.xml", true);

        assertResourceLoader("/webroot/notexist.txt", "notexist.txt", false);
    }

    @Test
    public void list() throws Exception {
        assertResourceLister("/webroot", "", true, "WEB-INF/", "appcontext/", "beans.xml", "filter/", "loader/",
                "logback.xml", "myfolder/", "resources-root.xml", "test.txt");
        assertResourceLister("/webroot/test.txt", null, false);
        assertResourceLister("/webroot/WEB-INF/", "WEB-INF", true, "aaa/", "resources.xml", "web.xml");
        assertResourceLister("/webroot/WEB-INF/web.xml", null, false);

        assertResourceLister("/webroot/notexist.txt", null, false);
    }

    @Test
    public void list_emptyDir() throws Exception {
        try {
            File emptyDir = new File(destdir, "emptyDir");
            emptyDir.mkdirs();
            nextGetResourceURL.set(emptyDir.toURI().toURL());

            String[] names = ((ResourceLister) loader).list(new MyContext("/webroot/any"), null);
            assertArrayEquals(new String[0], names);
        } finally {
            nextGetResourceURL.remove();
        }
    }

    @Test
    public void _toString() {
        assertThat(loader.toString(), containsAll("WebappResourceLoader[", "]"));
    }

    @Override
    protected String getPrefix() {
        return "webroot";
    }
}
