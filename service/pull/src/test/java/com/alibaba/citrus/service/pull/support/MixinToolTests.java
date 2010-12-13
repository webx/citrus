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
package com.alibaba.citrus.service.pull.support;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.service.pull.AbstractPullServiceConfigTests;
import com.alibaba.citrus.service.pull.PullService;

public class MixinToolTests extends AbstractPullServiceConfigTests {
    private Map<String, Object> tools;

    @Test
    public void tool() throws Exception {
        prepareWebEnvironment(null);

        pullService = (PullService) factory.getBean("pullService");
        tools = pullService.getTools();

        Object mixin = tools.get("myUtils");

        assertNotNull(mixin);

        Method stringUtil_isEmpty = mixin.getClass().getMethod("isEmpty", String.class);

        Method stringEscapeUtil_escapeHtml = mixin.getClass().getMethod("escapeHtml", String.class);
        Method stringEscapeUtil_escapeHtml_appendable = mixin.getClass().getMethod("escapeHtml", String.class,
                Appendable.class);

        assertTrue((Boolean) stringUtil_isEmpty.invoke(mixin, ""));
        assertFalse((Boolean) stringUtil_isEmpty.invoke(mixin, "test"));

        assertEquals("&lt;&gt;", stringEscapeUtil_escapeHtml.invoke(mixin, "<>"));

        StringBuilder buf = new StringBuilder();
        stringEscapeUtil_escapeHtml_appendable.invoke(mixin, "<>", buf);
        assertEquals("&lt;&gt;", buf.toString());

        try {
            mixin.getClass().getMethod("escapeXml", String.class); // not included
            fail();
        } catch (NoSuchMethodException e) {
        }

        Method stringEscapeUtil_escapeUrl = mixin.getClass().getMethod("escapeUrl", String.class);

        assertEquals("+", stringEscapeUtil_escapeUrl.invoke(mixin, " "));

        try {
            mixin.getClass().getMethod("escapeURL", String.class); // renamed
            fail();
        } catch (NoSuchMethodException e) {
        }

    }
}
