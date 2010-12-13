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

import static com.alibaba.citrus.service.resource.ResourceLoadingService.*;
import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLister;
import com.alibaba.citrus.service.resource.loader.FileResourceLoader.SearchPath;

public class FileLoaderTests extends AbstractResourceLoaderTests<FileResourceLoader> {
    @Test
    public void searchPath_wrong() {
        try {
            new SearchPath(null, true);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("path"));
        }

        // relpath.init(null)
        SearchPath path = new SearchPath("abc", true);

        try {
            path.init(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(
                    e,
                    exception("Could not get basedir for search path: ", "relpath=abc, basedir=null.  ",
                            "Please set basedir explictly at file-loader or use absolute path instead"));
        }
    }

    @Test
    public void searchPath_toString() {
        // relpath without basedir
        SearchPath path = new SearchPath("abc", true);
        assertEquals("relpath=abc, basedir=null", path.toString());

        // relpath with basedir
        path.init(srcdir.getAbsolutePath());
        assertThat(path.toString(), containsAll("relpath=abc, basedir=", "config"));

        // abspath
        path = new SearchPath("abc", false);
        assertEquals("abspath=abc", path.toString());

        // abspath.init(srcdir) - no effect
        path.init(srcdir.getAbsolutePath());
        assertEquals("abspath=abc", path.toString());
    }

    @Test
    public void searchPath_default() throws Exception {
        try {
            createLoader(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(
                    e,
                    exception("Could not get basedir for search path: ", "relpath=/, basedir=null.  ",
                            "Please set basedir explictly at file-loader or use absolute path instead"));
        }

        // basedir
        createLoader(srcdir.getAbsolutePath(), null);

        assertEquals(1, loader.getPaths().length);
        assertThat(loader.getPaths()[0].toString(), containsAll("relpath=/, basedir=", "config"));

        // configURL
        createLoader(null, new File(srcdir, "aaa/config.xml").toURI().toURL());

        assertEquals(1, loader.getPaths().length);
        assertThat(loader.getPaths()[0].toString(), containsAll("relpath=/, basedir=", "config/aaa"));
    }

    @Test
    public void basedir_noConfigURL() throws Exception {
        createLoader(srcdir.getAbsolutePath(), // srcdir
                null, //
                new SearchPath("WEB-INF", true), // srcdir/WEB-INF
                new SearchPath(new File(srcdir, "aaa").getAbsolutePath(), false)); // srcdir/aaa

        assertEquals(2, loader.getPaths().length);
        assertThat(loader.getPaths()[0].toString(), containsAll("relpath=WEB-INF, basedir=", "config"));
        assertThat(loader.getPaths()[1].toString(), containsAll("abspath=", "config/aaa"));
    }

    @Test
    public void basedir_abs() throws Exception {
        createLoader(srcdir.getAbsolutePath(), // srcdir
                new File(srcdir, "aaa/config.xml").toURI().toURL(), // srcdir/aaa
                new SearchPath("WEB-INF", true), // srcdir/WEB-INF
                new SearchPath(new File(srcdir, "aaa").getAbsolutePath(), false)); // srcdir/aaa

        assertEquals(2, loader.getPaths().length);
        assertThat(loader.getPaths()[0].toString(), containsAll("relpath=WEB-INF, basedir=", "config"));
        assertThat(loader.getPaths()[1].toString(), containsAll("abspath=", "config/aaa"));
    }

    @Test
    public void basedir_rel() throws Exception {
        createLoader("..", // srcdir/aaa/.. 
                new File(srcdir, "aaa/config.xml").toURI().toURL(), // srcdir/aaa
                new SearchPath("WEB-INF", true), // srcdir/WEB-INF
                new SearchPath(new File(srcdir, "aaa").getAbsolutePath(), false)); // srcdir/aaa

        assertEquals(2, loader.getPaths().length);
        assertThat(loader.getPaths()[0].toString(), containsAll("relpath=WEB-INF, basedir=", "config"));
        assertThat(loader.getPaths()[1].toString(), containsAll("abspath=", "config/aaa"));
    }

    @Test
    public void basedir_configURLonly() throws Exception {
        createLoader(null, //
                new File(srcdir, "aaa/config.xml").toURI().toURL(), // basedir=srcdir/aaa 
                new SearchPath("../WEB-INF", true), // srcdir/aaa/../WEB-INF
                new SearchPath(new File(srcdir, "aaa/bbb").getAbsolutePath(), false)); // srcdir/aaa/bbb

        assertEquals(2, loader.getPaths().length);
        assertThat(loader.getPaths()[0].toString(), containsAll("relpath=../WEB-INF, basedir=", "config/aaa"));
        assertThat(loader.getPaths()[1].toString(), containsAll("abspath=", "config/aaa/bbb"));
    }

    @Test
    public void search() throws Exception {
        createLoader(srcdir.getAbsolutePath(), null, //
                new SearchPath("WEB-INF", true), // srcdir/WEB-INF
                new SearchPath(new File(srcdir, "WEB-INF/aaa").getAbsolutePath(), false)); // srcdir/aaa

        // getResource
        assertResourceLoader("/myapp/", "WEB-INF", true); // dir
        assertResourceLoader("/myapp/web.xml", "WEB-INF/web.xml", true);
        assertResourceLoader("/myapp/bbb/abc.txt", "WEB-INF/aaa/bbb/abc.txt", true);

        // list
        assertResourceLister("/myapp/", "WEB-INF", true, "aaa/", "resources.xml", "web.xml");
        assertResourceLister("/myapp/web.xml", null, false);
        assertResourceLister("/myapp/bbb/abc.txt", null, false);
    }

    @Test
    public void search2() throws Exception {
        createLoader(null, new File(srcdir, "aaa/config.xml").toURI().toURL(), // basedir=srcdir/aaa 
                new SearchPath("../WEB-INF", true), // srcdir/aaa/../WEB-INF
                new SearchPath(new File(srcdir, "WEB-INF/aaa/bbb").getAbsolutePath(), false)); // srcdir/aaa/bbb

        // getResource
        assertResourceLoader("/myapp/", "WEB-INF", true); // dir
        assertResourceLoader("/myapp/web.xml", "WEB-INF/web.xml", true);
        assertResourceLoader("/myapp/abc.txt", "WEB-INF/aaa/bbb/abc.txt", true);

        // list
        assertResourceLister("/myapp/", "WEB-INF", true, "aaa/", "resources.xml", "web.xml");
        assertResourceLister("/myapp/web.xml", null, false);
        assertResourceLister("/myapp/abc.txt", null, false);
    }

    @Test
    public void search3() throws Exception {
        createLoader(srcdir.getAbsolutePath(), null);

        // getResource
        assertResourceLoader("/myapp/WEB-INF", "WEB-INF", true); // dir
        assertResourceLoader("/myapp/WEB-INF/web.xml", "WEB-INF/web.xml", true);
        assertResourceLoader("/myapp/notfound.txt", null, false);

        // list
        assertResourceLister("/myapp/WEB-INF", "WEB-INF", true, "aaa/", "resources.xml", "web.xml");
        assertResourceLister("/myapp/WEB-INF/web.xml", null, false);
        assertResourceLister("/myapp/notfound.txt", null, false);
    }

    @Test
    public void search4() throws Exception {
        createLoader(srcdir.getAbsolutePath(), null, //
                new SearchPath("WEB-INF", true), // srcdir/WEB-INF
                new SearchPath(new File(srcdir, "WEB-INF/aaa").getAbsolutePath(), false), // srcdir/WEB-INF/aaa
                new SearchPath(new File(srcdir, "WEB-INF/aaa/ccc").getAbsolutePath(), false)); // srcdir/WEB-INF/aaa/ccc

        // getResource
        //----------------------------------------------------------------------------------
        // (1) without FOR_CREATE option
        assertResourceLoader("/myapp/def.txt", "WEB-INF/aaa/ccc/def.txt", true);

        // (2) with FOR_CREATE option
        assertResourceLoader("/myapp/def.txt", "WEB-INF/aaa/ccc/def.txt", true, FOR_CREATE);

        // (3) without FOR_CREATE option
        Resource resource = loader.getResource(new MyContext("/myapp/ghi.txt"), null);
        Assert.assertNull(resource);

        // (4) with FOR_CREATE option
        resource = loader.getResource(new MyContext("/myapp/ghi.txt"), FOR_CREATE);
        Assert.assertNotNull(resource);
        assertTrue(!resource.exists());
    }

    @Test
    public void empty_dir() throws Exception {
        new File(destdir, "emptyDir").mkdirs();
        createLoader(destdir.getAbsolutePath(), null);

        String[] names = ((ResourceLister) loader).list(new MyContext("/myapp/emptyDir"), null);
        assertArrayEquals(new String[0], names);
    }

    @Test
    public void _toString() {
        createLoader(srcdir.getAbsolutePath(), null, //
                new SearchPath("WEB-INF", true), // srcdir/WEB-INF
                new SearchPath(new File(srcdir, "aaa").getAbsolutePath(), false)); // srcdir/aaa

        assertThat(loader.toString(),
                containsAll("FileResourceLoader [", "relpath=WEB-INF, basedir=", "abspath=", "config/aaa", "]"));
    }

    private void createLoader(String basedir, URL configURL, SearchPath... paths) {
        loader = new FileResourceLoader();

        if (basedir != null) {
            loader.setBasedir(basedir);
        }

        if (configURL != null) {
            loader.setConfigFileURL(configURL);
        }

        loader.setPaths(paths);
        loader.init(null);
    }

    @Override
    protected String getPrefix() {
        return "myapp";
    }
}
