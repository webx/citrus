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

package com.alibaba.citrus.util.i18n;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.util.i18n.LocaleInfo.UnknownCharset;
import org.junit.Test;

public class UnknownCharsetTests {
    private UnknownCharset charset;

    @Test
    public void constructor() {
        try {
            new UnknownCharset(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("charset name"));
        }
    }

    @Test
    public void name() {
        charset = new UnknownCharset("test");
        assertEquals("test", charset.name());
    }

    @Test
    public void toString_() {
        charset = new UnknownCharset("test");
        assertEquals("test", charset.name());
    }

    @Test
    public void newEncoder() {
        charset = new UnknownCharset("test");

        try {
            charset.newEncoder();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("Could not create encoder for unknown charset: test"));
        }
    }

    @Test
    public void newDecoder() {
        charset = new UnknownCharset("test");

        try {
            charset.newDecoder();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("Could not create decoder for unknown charset: test"));
        }
    }
}
