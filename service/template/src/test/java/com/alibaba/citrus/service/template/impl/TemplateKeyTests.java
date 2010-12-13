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
package com.alibaba.citrus.service.template.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class TemplateKeyTests {
    private TemplateKey key;

    @Test
    public void templateName_empty() {
        try {
            newTemplateKey(" ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("illegal templateName:  "));
        }

        try {
            newTemplateKey(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("illegal templateName: null"));
        }
    }

    @Test
    public void templateName() {
        key = newTemplateKey(" a.vm ");
        assertEquals("/a.vm", key.getTemplateName());
        assertEquals("/a", key.getTemplateNameWithoutExtension());
        assertEquals("vm", key.getExtension());

        key = newTemplateKey(" a. ");
        assertEquals("/a", key.getTemplateName());
        assertEquals("/a", key.getTemplateNameWithoutExtension());
        assertEquals(null, key.getExtension());

        try {
            newTemplateKey(" . ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("illegal templateName:  . "));
        }
    }

    @Test
    public void strategyKeys() {
        key = newTemplateKey("a.vm");
        assertArrayEquals(new Object[] {}, key.getStrategyKeys());

        key = newTemplateKey("a.vm", 1, null, 3);
        assertArrayEquals(new Object[] { 1, null, 3 }, key.getStrategyKeys());
    }

    @Test
    public void equalsAndHashCode() {
        assertEqualsAndHashCode(true, newTemplateKey("a/b.vm"), newTemplateKey("a/b.vm"));
        assertEqualsAndHashCode(true, newTemplateKey("/a"), newTemplateKey("a."));
        assertEqualsAndHashCode(true, newTemplateKey("a.vm", 1, null, 3), newTemplateKey("/a.vm", 1, null, 3));

        assertEqualsAndHashCode(false, newTemplateKey("a/b.vm"), newTemplateKey("a/b.vm", (Object) null));
        assertEqualsAndHashCode(false, newTemplateKey("a.vm", 111), newTemplateKey("a.vm", 222));
        assertEqualsAndHashCode(false, newTemplateKey("a.vm", 1, 3), newTemplateKey("a.vm", 1, 2));
    }

    @Test
    public void toString_() {
        assertEquals("/a/b.vm[]", newTemplateKey("a/b.vm").toString());
        assertEquals("/a[]", newTemplateKey("a.").toString());
        assertEquals("/a/b.vm[1, 2, 3]", newTemplateKey("a/b.vm", 1, 2, 3).toString());
    }

    private void assertEqualsAndHashCode(boolean equals, TemplateKey key1, TemplateKey key2) {
        assertTrue(key1.hashCode() > 0);
        assertTrue(key2.hashCode() > 0);

        if (equals) {
            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        } else {
            assertThat(key1, not(equalTo(key2)));
            assertThat(key1.hashCode(), not(equalTo(key2.hashCode())));
        }
    }

    private TemplateKey newTemplateKey(String name, Object... objects) {
        return new TemplateKey(name, getStrategies(objects));
    }

    private TemplateSearchingStrategy[] getStrategies(Object... objects) {
        if (isEmptyArray(objects)) {
            return new TemplateSearchingStrategy[0];
        }

        TemplateSearchingStrategy[] strategies = new TemplateSearchingStrategy[objects.length];

        for (int i = 0; i < objects.length; i++) {
            strategies[i] = createMock(TemplateSearchingStrategy.class);
            expect(strategies[i].getKey(org.easymock.EasyMock.<String> anyObject())).andReturn(objects[i]);
            replay(strategies[i]);
        }

        return strategies;
    }
}
