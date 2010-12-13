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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.util.FileUtil.FileNameAndExtension;

public class FileUtilTests {
    @Test
    public void normalizeAbsolutePath() {
        // Illegal path
        assertIllegalAbsolutePath("/..");
        assertIllegalAbsolutePath("/../");
        assertIllegalAbsolutePath("\\aaa\\bbb\\ccc\\..\\..\\..\\..");
        assertIllegalAbsolutePath("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..");
        assertIllegalAbsolutePath("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\..");

        assertIllegalAbsolutePath("..");
        assertIllegalAbsolutePath("../");

        assertIllegalAbsolutePath("aaa\\bbb\\ccc\\..\\..\\..\\..");
        assertIllegalAbsolutePath("aaa\\bbb\\ccc\\..\\..\\..\\..\\..");
        assertIllegalAbsolutePath("aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\..");

        // 空值
        assertEquals("", FileUtil.normalizeAbsolutePath(null));
        assertEquals("", FileUtil.normalizeAbsolutePath(""));
        assertEquals("", FileUtil.normalizeAbsolutePath("  "));

        // Absolute path
        assertEquals("/", FileUtil.normalizeAbsolutePath("\\\\"));
        assertEquals("", FileUtil.normalizeAbsolutePath("\\\\path\\subpath\\..\\.."));

        assertEquals("/path", FileUtil.normalizeAbsolutePath("\\\\path\\subpath\\.\\.."));
        assertEquals("/path", FileUtil.normalizeAbsolutePath("\\\\path"));
        assertEquals("/path/subpath", FileUtil.normalizeAbsolutePath("\\\\path\\subpath\\."));
        assertEquals("/path/subpath/", FileUtil.normalizeAbsolutePath("\\\\path\\subpath\\.\\"));

        assertEquals("/", FileUtil.normalizeAbsolutePath("/"));
        assertEquals("", FileUtil.normalizeAbsolutePath("/aaa/.."));
        assertEquals("/aaa/bbb", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\.\\.."));
        assertEquals("/aaa/bbb/", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("/aaa/", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("/aaa/ddd/", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\..\\..\\\\ddd//"));
        assertEquals("/", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\..\\..\\..\\"));

        // Relative path
        assertEquals("/", FileUtil.normalizeAbsolutePath("aaa/../"));
        assertEquals("/aaa/bbb", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\.\\.."));
        assertEquals("/aaa/bbb/", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("/aaa/", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("/aaa", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\..\\.."));
        assertEquals("/", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\..\\..\\..\\"));

        // remove trailing slash
        assertEquals("/aaa/bbb", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\.\\..", true));
        assertEquals("/aaa/bbb", FileUtil.normalizeAbsolutePath("\\aaa\\bbb\\ccc\\.\\..\\", true));
        assertEquals("/aaa", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\..\\..\\", true));
        assertEquals("/aaa/ddd", FileUtil.normalizeAbsolutePath("aaa\\bbb\\ccc\\..\\..\\\\ddd//", true));
        assertEquals("", FileUtil.normalizeAbsolutePath("\\", true));
        assertEquals("", FileUtil.normalizeAbsolutePath("/a/../", true));
    }

    private void assertIllegalAbsolutePath(String path) {
        try {
            FileUtil.normalizeAbsolutePath(path);
        } catch (IllegalPathException e) {
            assertThat(e, exception(path));
        }
    }

    @Test
    public void normalizeRelativePath() {
        // Illegal path
        assertEquals("..", FileUtil.normalizeRelativePath("/.."));
        assertEquals("../", FileUtil.normalizeRelativePath("/../"));
        assertEquals("..", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\..\\..\\..\\.."));
        assertEquals("../..", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\.."));
        assertEquals("../..", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\.."));

        assertEquals("..", FileUtil.normalizeRelativePath(".."));
        assertEquals("../", FileUtil.normalizeRelativePath("../"));

        assertEquals("..", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\..\\.."));
        assertEquals("../..", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\..\\..\\.."));
        assertEquals("../..", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\.."));

        // 空值
        assertEquals("", FileUtil.normalizeRelativePath(null));
        assertEquals("", FileUtil.normalizeRelativePath(""));
        assertEquals("", FileUtil.normalizeRelativePath("  "));

        // Absolute path
        assertEquals("", FileUtil.normalizeRelativePath("\\\\"));
        assertEquals("", FileUtil.normalizeRelativePath("\\\\path\\subpath\\..\\.."));

        assertEquals("path", FileUtil.normalizeRelativePath("\\\\path\\subpath\\.\\.."));
        assertEquals("path", FileUtil.normalizeRelativePath("\\\\path"));
        assertEquals("path/subpath", FileUtil.normalizeRelativePath("\\\\path\\subpath\\."));
        assertEquals("path/subpath/", FileUtil.normalizeRelativePath("\\\\path\\subpath\\.\\"));

        assertEquals("", FileUtil.normalizeRelativePath("/"));
        assertEquals("", FileUtil.normalizeRelativePath("/aaa/.."));
        assertEquals("aaa/bbb", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\.\\.."));
        assertEquals("aaa/bbb/", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("aaa/", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("aaa/ddd/", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\..\\..\\\\ddd//"));
        assertEquals("", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\..\\..\\..\\"));

        // Relative path
        assertEquals("", FileUtil.normalizeRelativePath("aaa/../"));
        assertEquals("aaa/bbb", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\.\\.."));
        assertEquals("aaa/bbb/", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("aaa/", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("aaa", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\.."));
        assertEquals("", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\..\\"));

        // remove trailing slash
        assertEquals("aaa/bbb", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\.\\..", true));
        assertEquals("aaa/bbb", FileUtil.normalizeRelativePath("\\aaa\\bbb\\ccc\\.\\..\\", true));
        assertEquals("aaa", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\", true));
        assertEquals("aaa/ddd", FileUtil.normalizeRelativePath("aaa\\bbb\\ccc\\..\\..\\\\ddd//", true));
        assertEquals("", FileUtil.normalizeRelativePath("\\", true));
        assertEquals("", FileUtil.normalizeRelativePath("/a/../", true));
    }

    @Test
    public void normalizePath() {
        // Illegal path
        assertIllegalPath("/..");
        assertIllegalPath("/../");
        assertIllegalPath("\\aaa\\bbb\\ccc\\..\\..\\..\\..");
        assertIllegalPath("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..");
        assertIllegalPath("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\..");

        // 空值
        assertEquals("", FileUtil.normalizePath(null));
        assertEquals("", FileUtil.normalizePath(""));
        assertEquals("", FileUtil.normalizePath("  "));

        // Absolute path
        assertEquals("/", FileUtil.normalizePath("\\\\"));
        assertEquals("", FileUtil.normalizePath("\\\\path\\subpath\\..\\.."));

        assertEquals("/path", FileUtil.normalizePath("\\\\path\\subpath\\.\\.."));
        assertEquals("/path/", FileUtil.normalizePath("\\\\path/"));
        assertEquals("/path/subpath", FileUtil.normalizePath("\\\\path\\subpath\\."));
        assertEquals("/path/subpath/", FileUtil.normalizePath("\\\\path\\subpath\\.\\"));

        assertEquals("/", FileUtil.normalizePath("/"));
        assertEquals("", FileUtil.normalizePath("/aaa/.."));
        assertEquals("/aaa/bbb", FileUtil.normalizePath("\\aaa\\bbb\\ccc\\.\\.."));
        assertEquals("/aaa/bbb/", FileUtil.normalizePath("\\aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("/aaa/", FileUtil.normalizePath("\\aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("/aaa/ddd/", FileUtil.normalizePath("\\aaa\\bbb\\ccc\\..\\..\\\\ddd//"));
        assertEquals("/", FileUtil.normalizePath("\\aaa\\bbb\\ccc\\..\\..\\..\\"));

        // Relative path
        assertEquals("", FileUtil.normalizePath("aaa/../"));
        assertEquals("..", FileUtil.normalizePath(".."));
        assertEquals("../", FileUtil.normalizePath("../"));
        assertEquals("aaa/bbb", FileUtil.normalizePath("aaa\\bbb\\ccc\\.\\.."));
        assertEquals("aaa/bbb/", FileUtil.normalizePath("aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("aaa/", FileUtil.normalizePath("aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("aaa", FileUtil.normalizePath("aaa\\bbb\\ccc\\..\\.."));
        assertEquals("", FileUtil.normalizePath("aaa\\bbb\\ccc\\..\\..\\..\\"));
        assertEquals("..", FileUtil.normalizePath("aaa\\bbb\\ccc\\..\\..\\..\\.."));
        assertEquals("../..", FileUtil.normalizePath("aaa\\bbb\\ccc\\..\\..\\..\\..\\.."));
        assertEquals("../..", FileUtil.normalizePath("aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\.."));

        // Removing trailing slash
        assertEquals("", FileUtil.normalizePath("aaa/../", true));
        assertEquals("", FileUtil.normalizePath("", true));

        assertEquals("", FileUtil.normalizePath("/aaa/../", true));
        assertEquals("", FileUtil.normalizePath("/", true));

        assertEquals("/path", FileUtil.normalizePath("\\\\path\\subpath\\.\\..", true));
        assertEquals("path", FileUtil.normalizePath("path//", true));

        assertEquals("/path/subpath", FileUtil.normalizePath("\\\\path\\subpath\\.", true));
        assertEquals("/path/subpath", FileUtil.normalizePath("\\\\path\\subpath\\.\\", true));
        assertEquals("path/subpath", FileUtil.normalizePath("path\\subpath\\.\\", true));
    }

    private void assertIllegalPath(String path) {
        try {
            FileUtil.normalizePath(path);
        } catch (IllegalPathException e) {
            assertThat(e, exception(path));
        }
    }

    @Test
    public void getAbsolutePathBasedOn() {
        // Illegal path
        assertIllegalPathBasedOn("/..", null);
        assertIllegalPathBasedOn("/../", null);

        assertIllegalPathBasedOn("\\aaa\\bbb\\ccc\\..\\..\\..\\..", null);
        assertIllegalPathBasedOn("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..", null);
        assertIllegalPathBasedOn("\\aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\..", null);

        assertIllegalPathBasedOn("..", "/");
        assertIllegalPathBasedOn("../", "/");

        assertIllegalPathBasedOn("aaa\\bbb\\ccc\\..\\..\\..\\..", "/");
        assertIllegalPathBasedOn("aaa\\bbb\\ccc\\..\\..\\..\\..\\..", "/");
        assertIllegalPathBasedOn("aaa\\bbb\\ccc\\..\\..\\..\\..\\..\\ddd\\..", "/");

        assertIllegalPathBasedOn("aaa\\../..\\..", "/bbb/");
        assertIllegalPathBasedOn("aaa\\bbb\\ccc\\..\\..\\..\\..\\..", "/");
        assertIllegalPathBasedOn("..\\..\\..\\..\\..\\ddd\\..", "/aaa\\bbb\\ccc");

        // Empty path
        assertEquals("/", FileUtil.getAbsolutePathBasedOn("/", null));
        assertEquals("", FileUtil.getAbsolutePathBasedOn(null, null));
        assertEquals("", FileUtil.getAbsolutePathBasedOn(null, "."));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn(null, "/"));

        assertEquals("/", FileUtil.getAbsolutePathBasedOn(" / ", "  "));
        assertEquals("", FileUtil.getAbsolutePathBasedOn(" ", " "));
        assertEquals("", FileUtil.getAbsolutePathBasedOn(" ", " . "));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn(" ", " / "));

        // Absolute path
        assertEquals("", FileUtil.getAbsolutePathBasedOn("/", "/."));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn("/", "\\\\"));
        assertEquals("", FileUtil.getAbsolutePathBasedOn("/", "\\\\path\\subpath\\..\\.."));
        assertEquals("/path", FileUtil.getAbsolutePathBasedOn(null, "\\\\path\\subpath\\.\\.."));
        assertEquals("/path", FileUtil.getAbsolutePathBasedOn(null, "\\\\path"));
        assertEquals("/path/subpath", FileUtil.getAbsolutePathBasedOn(null, "\\\\path\\subpath\\."));
        assertEquals("/path/subpath/", FileUtil.getAbsolutePathBasedOn(null, "\\\\path\\subpath\\.\\"));

        assertEquals("/", FileUtil.getAbsolutePathBasedOn(null, "/"));
        assertEquals("", FileUtil.getAbsolutePathBasedOn(null, "/aaa/.."));

        assertEquals("/aaa/bbb", FileUtil.getAbsolutePathBasedOn(null, "\\aaa\\bbb\\ccc\\.\\.."));
        assertEquals("/aaa/bbb/", FileUtil.getAbsolutePathBasedOn(null, "\\aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("/aaa/", FileUtil.getAbsolutePathBasedOn(null, "\\aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn(null, "\\aaa\\bbb\\ccc\\..\\..\\..\\"));

        // Relative path
        assertEquals("/", FileUtil.getAbsolutePathBasedOn("/", ""));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn("/", "  "));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn("/", "aaa/../"));

        assertEquals("/aaa/bbb", FileUtil.getAbsolutePathBasedOn("/", "aaa\\bbb\\ccc\\.\\.."));
        assertEquals("/aaa/bbb/", FileUtil.getAbsolutePathBasedOn("/", "aaa\\bbb\\ccc\\.\\..\\"));
        assertEquals("/aaa/", FileUtil.getAbsolutePathBasedOn("/", "aaa\\bbb\\ccc\\..\\..\\"));
        assertEquals("/", FileUtil.getAbsolutePathBasedOn("/", "aaa\\bbb\\ccc\\..\\..\\..\\"));
        assertEquals("/aaa/", FileUtil.getAbsolutePathBasedOn("/aaa/bbb/ccc", "..\\..\\"));
        assertEquals("/aaa/bbb", FileUtil.getAbsolutePathBasedOn("/aaa/bbb", ""));
        assertEquals("/aaa/bbb/", FileUtil.getAbsolutePathBasedOn("/aaa/bbb", "./"));
    }

    private void assertIllegalPathBasedOn(String path, String basedir) {
        try {
            FileUtil.getAbsolutePathBasedOn(basedir, path);
        } catch (IllegalPathException e) {
            assertThat(e, exception(path));
        }
    }

    @Test
    public void getSystemDependentAbsolutePathBasedOn() {
        if (SystemUtil.getOsInfo().isWindows()) {
            getWindowsAbsPathBasedOn();
        } else {
            getUnixAbsPathBasedOn();
        }

        try {
            FileUtil.getSystemDependentAbsolutePathBasedOn(null, "test.txt");
            fail();
        } catch (IllegalPathException e) {
            assertThat(e, exception("Basedir is not absolute path: "));
        }

        try {
            FileUtil.getSystemDependentAbsolutePathBasedOn("aa/bb", "test.txt");
            fail();
        } catch (IllegalPathException e) {
            assertThat(e, exception("Basedir is not absolute path: "));
        }
    }

    private void getWindowsAbsPathBasedOn() {
        // path is absolute
        assertEquals("c:/aa/bb.txt", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "c:/aa/bb.txt"));
        assertEquals("c:/aa/bb/", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "c:/aa/bb/"));

        // path is relative
        assertEquals("c:/base/aa/bb.txt", FileUtil.getSystemDependentAbsolutePathBasedOn("c:/base", "aa/bb.txt"));
        assertEquals("c:/base/aa/bb/", FileUtil.getSystemDependentAbsolutePathBasedOn("c:/base", "aa/bb/"));
        assertEquals("c:/bb/", FileUtil.getSystemDependentAbsolutePathBasedOn("c:/base", "../bb/"));

        // path is empty
        assertEquals("c:/base", FileUtil.getSystemDependentAbsolutePathBasedOn("c:/base", ""));
        assertEquals("c:/base", FileUtil.getSystemDependentAbsolutePathBasedOn("c:/base/", null));
    }

    private void getUnixAbsPathBasedOn() {
        // path is absolute
        assertEquals("/aa/bb.txt", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "/aa/bb.txt"));
        assertEquals("/aa/bb/", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "/aa/bb/"));

        // path is relative
        assertEquals("/base/aa/bb.txt", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "aa/bb.txt"));
        assertEquals("/base/aa/bb/", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "aa/bb/"));
        assertEquals("/bb/", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", "../bb/"));

        // path is empty
        assertEquals("/base", FileUtil.getSystemDependentAbsolutePathBasedOn("/base", ""));
        assertEquals("/base", FileUtil.getSystemDependentAbsolutePathBasedOn("/base/", null));
    }

    @Test
    public void getRelativePath() {
        // Illegal path
        assertIllegalRelativePath("..", "/");
        assertIllegalRelativePath("/", "../");

        // empty path
        assertEquals("", FileUtil.getRelativePath("/", null));
        assertEquals("", FileUtil.getRelativePath(null, null));
        assertEquals("", FileUtil.getRelativePath(null, "."));
        assertEquals("", FileUtil.getRelativePath(".", "."));

        // 绝对路径
        assertEquals("../../aaa/bbb", FileUtil.getRelativePath("/ddd/eee", "/aaa/bbb"));
        assertEquals("../../aaa/bbb/", FileUtil.getRelativePath("/ddd/eee", "/aaa/bbb/"));

        // 相对路径
        assertEquals("aaa/bbb", FileUtil.getRelativePath("/ddd/eee", "aaa/bbb"));
        assertEquals("aaa/bbb/", FileUtil.getRelativePath("/ddd/eee", "aaa/bbb/"));
        assertEquals("", FileUtil.getRelativePath("/ddd/eee", ""));
        assertEquals("", FileUtil.getRelativePath("/ddd/eee", "./"));
    }

    private void assertIllegalRelativePath(String path, String basedir) {
        try {
            FileUtil.getRelativePath(basedir, path);
        } catch (IllegalPathException e) {
            assertThat(e, exception(path));
        }
    }

    @Test
    public void getExtension() {
        // null
        assertEquals(null, FileUtil.getExtension("  "));
        assertEquals(null, FileUtil.getExtension(null));
        assertEquals(null, FileUtil.getExtension("  ", "null"));
        assertEquals(null, FileUtil.getExtension(null, "null"));

        // no extension
        assertEquals(null, FileUtil.getExtension(" test. ", null));
        assertEquals(null, FileUtil.getExtension(" test. "));
        assertEquals("null", FileUtil.getExtension(" test. ", "null"));

        // simple
        assertEquals("htm", FileUtil.getExtension(" test.htm "));
        assertEquals("HTM", FileUtil.getExtension(" test.HTM "));

        // with path
        assertEquals(null, FileUtil.getExtension("/a.b/test ", null));
        assertEquals(null, FileUtil.getExtension("/a.b/test "));
        assertEquals("jsp", FileUtil.getExtension("/a.b/test.jsp "));
        assertEquals("Jsp", FileUtil.getExtension("/a.b/test.Jsp "));

        assertEquals(null, FileUtil.getExtension("/a.b\\test ", null));
        assertEquals(null, FileUtil.getExtension("/a.b\\test "));
        assertEquals("jsp", FileUtil.getExtension("/a.b\\test.jsp "));
        assertEquals("Jsp", FileUtil.getExtension("/a.b\\test.Jsp "));
    }

    @Test
    public void getExtension_toLowerCase() {
        // null
        assertEquals(null, FileUtil.getExtension("  ", true));
        assertEquals(null, FileUtil.getExtension(null, true));
        assertEquals(null, FileUtil.getExtension("  ", "null", true));
        assertEquals(null, FileUtil.getExtension(null, "null", true));

        // no extension
        assertEquals(null, FileUtil.getExtension(" test. ", null, true));
        assertEquals(null, FileUtil.getExtension(" test. ", true));
        assertEquals("null", FileUtil.getExtension(" test. ", "null", true));

        // simple
        assertEquals("htm", FileUtil.getExtension(" test.htm ", true));
        assertEquals("htm", FileUtil.getExtension(" test.HTM ", true));

        // with path
        assertEquals(null, FileUtil.getExtension("/a.b/test ", null, true));
        assertEquals(null, FileUtil.getExtension("/a.b/test ", true));
        assertEquals("jsp", FileUtil.getExtension("/a.b/test.jsp ", true));
        assertEquals("jsp", FileUtil.getExtension("/a.b/test.Jsp ", true));

        assertEquals(null, FileUtil.getExtension("/a.b\\test ", null, true));
        assertEquals(null, FileUtil.getExtension("/a.b\\test ", true));
        assertEquals("jsp", FileUtil.getExtension("/a.b\\test.jsp ", true));
        assertEquals("jsp", FileUtil.getExtension("/a.b\\test.Jsp ", true));
    }

    @Test
    public void normalizeExtension() {
        assertEquals(null, FileUtil.normalizeExtension("  "));
        assertEquals(null, FileUtil.normalizeExtension(null));

        assertEquals("exe", FileUtil.normalizeExtension(" .EXE "));
        assertEquals("jpg", FileUtil.normalizeExtension(" jpg"));
    }

    @Test
    public void getFileNameAndExtension() {
        assertExts("", null, FileUtil.getFileNameAndExtension(null), "");
        assertExts("", null, FileUtil.getFileNameAndExtension(""), "");
        assertExts("", null, FileUtil.getFileNameAndExtension("."), "");
        assertExts("aa", null, FileUtil.getFileNameAndExtension("aa."), "aa");
        assertExts("", "bb", FileUtil.getFileNameAndExtension(".bb"), ".bb");
        assertExts("aa/bb/", null, FileUtil.getFileNameAndExtension("aa/bb/"), "aa/bb/");

        assertExts("/aaa/bbb/ccc", "jsp", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.jsp"), "/aaa/bbb/ccc.jsp");
        assertExts("/aaa/bbb/ccc", "Jsp", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.Jsp"), "/aaa/bbb/ccc.Jsp");
        assertExts("/aaa/bbb/ccc", "vm", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.vm"), "/aaa/bbb/ccc.vm");
        assertExts("/aaa/bbb/ccc", null, FileUtil.getFileNameAndExtension("/aaa/bbb/ccc."), "/aaa/bbb/ccc");
        assertExts("/aaa/bbb/ccc", null, FileUtil.getFileNameAndExtension("/aaa/bbb/ccc"), "/aaa/bbb/ccc");
        assertExts("/aaa/bbb/ccc", "ABC", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.ABC"), "/aaa/bbb/ccc.ABC");
        assertExts("/aaa/bbb.bak/ccc", null, FileUtil.getFileNameAndExtension("/aaa/bbb.bak/ccc"), "/aaa/bbb.bak/ccc");
        assertExts("/aaa/bbb/ccc/", null, FileUtil.getFileNameAndExtension("/aaa/bbb/ccc/"), "/aaa/bbb/ccc/");
    }

    @Test
    public void getFileNameAndExtension_toLowerCase() {
        assertExts("", null, FileUtil.getFileNameAndExtension(null, true), "");
        assertExts("", null, FileUtil.getFileNameAndExtension("", true), "");
        assertExts("", null, FileUtil.getFileNameAndExtension(".", true), "");
        assertExts("aa", null, FileUtil.getFileNameAndExtension("aa.", true), "aa");
        assertExts("", "bb", FileUtil.getFileNameAndExtension(".bb", true), ".bb");
        assertExts("aa/bb/", null, FileUtil.getFileNameAndExtension("aa/bb/", true), "aa/bb/");

        assertExts("/aaa/bbb/ccc", "jsp", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.jsp", true),
                "/aaa/bbb/ccc.jsp");
        assertExts("/aaa/bbb/ccc", "jsp", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.Jsp", true),
                "/aaa/bbb/ccc.jsp");
        assertExts("/aaa/bbb/ccc", "vm", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.vm", true), "/aaa/bbb/ccc.vm");
        assertExts("/aaa/bbb/ccc", null, FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.", true), "/aaa/bbb/ccc");
        assertExts("/aaa/bbb/ccc", null, FileUtil.getFileNameAndExtension("/aaa/bbb/ccc", true), "/aaa/bbb/ccc");
        assertExts("/aaa/bbb/ccc", "abc", FileUtil.getFileNameAndExtension("/aaa/bbb/ccc.ABC", true),
                "/aaa/bbb/ccc.abc");
        assertExts("/aaa/bbb.bak/ccc", null, FileUtil.getFileNameAndExtension("/aaa/bbb.bak/ccc", true),
                "/aaa/bbb.bak/ccc");
        assertExts("/aaa/bbb/ccc/", null, FileUtil.getFileNameAndExtension("/aaa/bbb/ccc/", true), "/aaa/bbb/ccc/");
    }

    private void assertExts(String path, String ext, FileNameAndExtension result, String toString) {
        assertEquals(path, result.getFileName());
        assertEquals(ext, result.getExtension());
        assertEquals(toString, result.toString());
    }
}
