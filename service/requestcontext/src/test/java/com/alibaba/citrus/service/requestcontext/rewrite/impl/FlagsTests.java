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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * ≤‚ ‘<code>Flags</code>°£
 * 
 * @author Michael Zhou
 */
public class FlagsTests {
    private Flags flags;

    @Before
    public void init() {
        flags = new Flags("C", "redirect=301", "QSA", "last=");
    }

    @Test
    public void emptyFlags() {
        assertArrayEquals(new String[0], new Flags().getFlags());
        assertArrayEquals(new String[0], new Flags((String[]) null).getFlags());
    }

    @Test
    public void isEmpty() {
        assertTrue(new Flags().isEmpty());
        assertTrue(new Flags((String[]) null).isEmpty());

        assertFalse(flags.isEmpty());
    }

    @Test
    public void hasFlags() {
        assertTrue(flags.hasFlags("C", "chain"));
        assertTrue(flags.hasFlags("R", "redirect"));
        assertTrue(flags.hasFlags("QSA", "qsappend"));
        assertTrue(flags.hasFlags("L", "last"));

        assertFalse(flags.hasFlags("notExist"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasFlags_iae() {
        flags.hasFlags((String[]) null);
    }

    @Test
    public void getFlagValue() {
        assertEquals("", flags.getFlagValue("C", "chain"));
        assertEquals("301", flags.getFlagValue("R", "redirect"));
        assertEquals("", flags.getFlagValue("QSA", "qsappend"));
        assertEquals("", flags.getFlagValue("L", "last"));

        assertEquals(null, flags.getFlagValue("notExist"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getFlagValue_iae() {
        flags.getFlagValue((String[]) null);
    }

    @Test
    public void _toString() {
        assertEquals("[C, redirect=301, QSA, last=]", flags.toString());
    }
}
