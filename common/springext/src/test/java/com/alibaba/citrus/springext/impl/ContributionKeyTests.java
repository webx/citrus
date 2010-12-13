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
package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.springext.ContributionType.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.test.runner.TestNameAware;

@RunWith(TestNameAware.class)
public class ContributionKeyTests {
    private ContributionKey key;

    @Test
    public void create() {
        try {
            new ContributionKey(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("name"));
        }

        try {
            new ContributionKey("name", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("type"));
        }

        new ContributionKey("name", BEAN_DEFINITION_PARSER);
    }

    @Test
    public void getNameAndType() {
        key = new ContributionKey("myname", BEAN_DEFINITION_PARSER);

        assertEquals("myname", key.getName());
        assertEquals(BEAN_DEFINITION_PARSER, key.getType());
    }

    @Test
    public void hashCodeAndEquals() {
        assertHashCodeAndEquals(new ContributionKey("myname", BEAN_DEFINITION_PARSER), new ContributionKey("myname",
                BEAN_DEFINITION_PARSER), true);

        assertHashCodeAndEquals(new ContributionKey("myname", BEAN_DEFINITION_DECORATOR), new ContributionKey("myname",
                BEAN_DEFINITION_DECORATOR), true);

        assertHashCodeAndEquals(new ContributionKey("myname", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE),
                new ContributionKey("myname", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE), true);

        assertHashCodeAndEquals(new ContributionKey("myname", BEAN_DEFINITION_PARSER), new ContributionKey("myname",
                BEAN_DEFINITION_DECORATOR), false);

        assertHashCodeAndEquals(new ContributionKey("myname", BEAN_DEFINITION_DECORATOR), new ContributionKey("myname",
                BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE), false);

        assertHashCodeAndEquals(new ContributionKey("myname", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE),
                new ContributionKey("myname", BEAN_DEFINITION_PARSER), false);
    }

    private void assertHashCodeAndEquals(ContributionKey key1, ContributionKey key2, boolean equals) {
        assertNotSame(key1, key2);
        assertEquals(key1, key1);
        assertEquals(key2, key2);

        if (equals) {
            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        } else {
            assertFalse(key1.equals(key2));
            assertFalse(key1.hashCode() == key2.hashCode());
        }
    }

    @Test
    public void compare() {
        List<ContributionKey> keys = createArrayList();

        keys.add(new ContributionKey("ccc", BEAN_DEFINITION_PARSER));
        keys.add(new ContributionKey("bbb", BEAN_DEFINITION_PARSER));
        keys.add(new ContributionKey("aaa", BEAN_DEFINITION_PARSER));

        keys.add(new ContributionKey("aaa", BEAN_DEFINITION_DECORATOR));
        keys.add(new ContributionKey("bbb", BEAN_DEFINITION_DECORATOR));
        keys.add(new ContributionKey("ccc", BEAN_DEFINITION_DECORATOR));

        keys.add(new ContributionKey("ccc", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE));
        keys.add(new ContributionKey("aaa", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE));
        keys.add(new ContributionKey("bbb", BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE));

        Collections.sort(keys);
        Iterator<ContributionKey> i = keys.iterator();

        assertEquals("ContributionKey[aaa, BEAN_DEFINITION_PARSER]", i.next().toString());
        assertEquals("ContributionKey[aaa, BEAN_DEFINITION_DECORATOR]", i.next().toString());
        assertEquals("ContributionKey[aaa, BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE]", i.next().toString());

        assertEquals("ContributionKey[bbb, BEAN_DEFINITION_PARSER]", i.next().toString());
        assertEquals("ContributionKey[bbb, BEAN_DEFINITION_DECORATOR]", i.next().toString());
        assertEquals("ContributionKey[bbb, BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE]", i.next().toString());

        assertEquals("ContributionKey[ccc, BEAN_DEFINITION_PARSER]", i.next().toString());
        assertEquals("ContributionKey[ccc, BEAN_DEFINITION_DECORATOR]", i.next().toString());
        assertEquals("ContributionKey[ccc, BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE]", i.next().toString());

        assertFalse(i.hasNext());
    }

    @Test
    public void toString_() {
        assertEquals("ContributionKey[myname, BEAN_DEFINITION_PARSER]", new ContributionKey("myname",
                BEAN_DEFINITION_PARSER).toString());
        assertEquals("ContributionKey[myname, BEAN_DEFINITION_DECORATOR]", new ContributionKey("myname",
                BEAN_DEFINITION_DECORATOR).toString());
        assertEquals("ContributionKey[myname, BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE]", new ContributionKey("myname",
                BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE).toString());
    }
}
