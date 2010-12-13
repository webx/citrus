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

import static org.junit.Assert.*;

import java.util.ResourceBundle;

import org.junit.Test;

/**
 * ≤‚ ‘<code>MessageUtil</code>°£
 * 
 * @author Michael Zhou
 */
public class MessageUtilTests {
    @Test
    public void formatMessage() {
        assertNull(MessageUtil.formatMessage(null, (Object[]) null));

        String message = "message {0}, {1}, {2}, {3}, {4}";

        assertSame(message, MessageUtil.formatMessage(message, (Object[]) null));
        assertSame(message, MessageUtil.formatMessage(message, new Object[0]));

        assertEquals("message aa, {1}, {2}, {3}, {4}", MessageUtil.formatMessage(message, "aa"));
        assertEquals("message aa, bb, {2}, {3}, {4}", MessageUtil.formatMessage(message, "aa", "bb"));
        assertEquals("message aa, bb, cc, {3}, {4}", MessageUtil.formatMessage(message, "aa", "bb", "cc"));
        assertEquals("message aa, bb, cc, dd, {4}", MessageUtil.formatMessage(message, "aa", "bb", "cc", "dd"));
        assertEquals("message aa, bb, cc, dd, ee", MessageUtil.formatMessage(message, "aa", "bb", "cc", "dd", "ee"));
    }

    @Test
    public void getMessage() {
        ResourceBundle bundle = ResourceBundle.getBundle(MessageUtilTests.class.getName());
        String key = "key";
        String notFoundKey = "notFound";

        assertNull(MessageUtil.getMessage(null, null, (Object[]) null));
        assertNull(MessageUtil.getMessage(bundle, null, (Object[]) null));
        assertSame(key, MessageUtil.getMessage(null, key, (Object[]) null));
        assertSame(notFoundKey, MessageUtil.getMessage(bundle, notFoundKey, (Object[]) null));

        assertEquals("message aa, {1}, {2}, {3}, {4}", MessageUtil.getMessage(bundle, key, "aa"));
        assertEquals("message aa, bb, {2}, {3}, {4}", MessageUtil.getMessage(bundle, key, "aa", "bb"));
        assertEquals("message aa, bb, cc, {3}, {4}", MessageUtil.getMessage(bundle, key, "aa", "bb", "cc"));
        assertEquals("message aa, bb, cc, dd, {4}", MessageUtil.getMessage(bundle, key, "aa", "bb", "cc", "dd"));
        assertEquals("message aa, bb, cc, dd, ee", MessageUtil.getMessage(bundle, key, "aa", "bb", "cc", "dd", "ee"));
    }
}
