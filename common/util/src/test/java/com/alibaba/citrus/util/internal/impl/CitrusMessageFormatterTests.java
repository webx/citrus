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
package com.alibaba.citrus.util.internal.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.WrongPrefixMessages;
import com.alibaba.citrus.util.internal.MessageFormatterTests;

/**
 * ≤‚ ‘<code>CitrusMessageFormatter</code>°£
 * 
 * @author Michael Zhou
 */
public class CitrusMessageFormatterTests extends MessageFormatterTests {
    private MyCitrusMessages msgs;

    @Before
    public void initCitrusMessages() {
        msgs = new MyCitrusMessages();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameConvertions_wrongPrefix() {
        new WrongPrefixMessages();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameConvertions_wrongSuffix() {
        new WrongSuffix();
    }

    @Test
    public void format() {
        assertEquals("hello, citrus", msgs.format("a"));
    }

    static class MyCitrusMessages extends CitrusMessageFormatter<String> {
    }
}

class WrongSuffix extends CitrusMessageFormatter<String> {
}
