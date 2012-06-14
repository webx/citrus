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

package com.alibaba.citrus.service.requestcontext.locale;

import static org.junit.Assert.*;

import com.alibaba.citrus.service.requestcontext.locale.impl.SetLocaleOverrider;
import org.junit.Before;
import org.junit.Test;

public class SetLocaleOverriderTests {
    private SetLocaleOverrider overrider;

    @Before
    public void init() {
        overrider = new SetLocaleOverrider();
    }

    @Test
    public void setRequestUriPatternName() {
        try {
            overrider.setUri(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        overrider.setUri("*.json");
        assertTrue(overrider.getRequestUriPattern().matcher("/a/b/c.json").find());
    }

    @Test
    public void setInputCharset() {
        overrider.setInputCharset(null);
        assertNull(overrider.getInputCharset());

        overrider.setInputCharset(" ");
        assertNull(overrider.getInputCharset());

        overrider.setInputCharset(" GBK ");
        assertEquals("GBK", overrider.getInputCharset());
    }

    @Test
    public void setOutputCharset() {
        overrider.setOutputCharset(null);
        assertNull(overrider.getOutputCharset());

        overrider.setOutputCharset(" ");
        assertNull(overrider.getOutputCharset());

        overrider.setOutputCharset(" GBK ");
        assertEquals("GBK", overrider.getOutputCharset());
    }

    @Test
    public void _toString() {
        overrider.setUri("*.json");
        overrider.setInputCharset("input");
        overrider.setOutputCharset("output");

        assertEquals("Override[uri=*.json, inputCharset=input, outputCharset=output]", overrider.toString());
    }
}
