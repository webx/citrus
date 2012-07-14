/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class RequestURIFilterTests {
    private RequestURIFilter filter;

    @Test
    public void patterns() throws Exception {
        Pattern[] patterns;

        // no patterns - default value
        patterns = getPatterns(null);
        assertNull(patterns);

        // set empty patterns
        patterns = getPatterns(" ");
        assertNull(patterns);

        patterns = getPatterns(" \r\n, ");
        assertNull(patterns);

        // with patterns
        patterns = getPatterns("/aa , *.jpg");
        assertEquals(2, patterns.length);

        patterns = getPatterns("/aa  *.jpg");
        assertEquals(2, patterns.length);

        patterns = getPatterns("/aa\r\n*.jpg");
        assertEquals(2, patterns.length);

        patterns = getPatterns("/aa  *.jpg !/aa/*.jpg");
        assertEquals(3, patterns.length);
    }

    private Pattern[] getPatterns(String s) {
        filter = new RequestURIFilter(s);
        return getFieldValue(filter, "patterns", Pattern[].class);
    }

    @Test
    public void matches() throws Exception {
        filter = new RequestURIFilter("/aa , /bb/test*, *.jpg");

        assertMatches(true, "/aa/bb");
        assertMatches(false, "/aabb");
        assertMatches(true, "/bb/testcc/dd");
        assertMatches(true, "/bb/test/cc/dd");
        assertMatches(false, "/cc/aa/bb");

        assertMatches(true, "/cc/test.jpg");
        assertMatches(true, "/cc/aa/bb/test.jpg");

        assertMatches(false, "/cc/aa/bb/test.htm");
    }

    @Test
    public void matches_withExcludes() throws Exception {
        filter = new RequestURIFilter("/path, *.jpg, !/path/*.jpg");

        assertMatches(true, "/path/test");
        assertMatches(true, "/test.jpg");
        assertMatches(false, "/path/test.jpg");
    }

    private void assertMatches(boolean matches, String path) throws Exception {
        assertEquals(matches, filter.matches(path));
    }

    @Test
    public void toString_() {
        filter = new RequestURIFilter(null);
        assertEquals("FilterOf[]", filter.toString());

        filter = new RequestURIFilter("/aa , *.jpg, !/aa/*.jpg");
        assertEquals("FilterOf [\n" + //
                     "  [1/3] /aa\n" + //
                     "  [2/3] *.jpg\n" + //
                     "  [3/3] !/aa/*.jpg\n" + //
                     "]", filter.toString());
    }
}
