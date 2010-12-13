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
package com.alibaba.citrus.service.requestcontext.session.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;

import com.alibaba.citrus.service.requestcontext.session.SessionConfig.StoreMappingsConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.StoresConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionStore;
import com.alibaba.citrus.service.requestcontext.session.impl.SessionRequestContextFactoryImpl.AttributePattern;
import com.alibaba.citrus.test.TestEnvStatic;

/**
 * 测试store mapping的逻辑。
 * 
 * @author Michael Zhou
 */
public class StoreMappingTests {
    private StoresConfig stores;
    private StoreMappingsConfig mappings;
    private List<AttributePattern> patterns;

    {
        TestEnvStatic.init();
    }

    @Before
    public void init() throws Exception {
        stores = newStores("s1", "s2", "s3", "s4", "s5", "s6");
        mappings = newStoreMappings();
        patterns = createArrayList();
    }

    @Test
    public void duplicatedDefaultStores() throws Exception {
        patterns.add(AttributePattern.getDefaultPattern("s1"));
        patterns.add(AttributePattern.getDefaultPattern("s2"));

        try {
            setPatterns();
            fail();
        } catch (InvocationTargetException e) {
            assertThat(e, exception(IllegalArgumentException.class, "More than one stores mapped to *", "s1", "s2"));
        }
    }

    @Test
    public void undefinedStores() throws Exception {
        patterns.add(AttributePattern.getDefaultPattern("s0"));

        try {
            setPatterns();
            fail();
        } catch (InvocationTargetException e) {
            assertThat(e,
                    exception(IllegalArgumentException.class, "Undefined Session Store", "match=\"*\"", "store=\"s0\""));
        }
    }

    @Test
    public void match() throws Exception {
        patterns.add(AttributePattern.getDefaultPattern("s1"));
        patterns.add(AttributePattern.getRegexPattern("s5", "^.*c$"));
        patterns.add(AttributePattern.getRegexPattern("s4", "^a.*"));
        patterns.add(AttributePattern.getExactPattern("s2", "abc"));
        patterns.add(AttributePattern.getExactPattern("s3", "abcdef"));

        setPatterns();

        // null/empty attrName
        try {
            mappings.getStoreNameForAttribute(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("attrName"));
        }

        try {
            mappings.getStoreNameForAttribute("");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("attrName"));
        }

        // 默认匹配
        assertEquals("s1", mappings.getStoreNameForAttribute("nomatch"));

        // 匹配较长的，备选：s1, s4, s3
        assertEquals("s3", mappings.getStoreNameForAttribute("abcdef"));

        // 匹配较精确的，备选：s1, s5, s4, s2 
        assertEquals("s2", mappings.getStoreNameForAttribute("abc"));

        // 匹配排在前的，备选：s1, s5, s4 
        assertEquals("s5", mappings.getStoreNameForAttribute("acc"));
    }

    @Test
    public void getExactMatchedAttributeNames() throws Exception {
        patterns.add(AttributePattern.getDefaultPattern("s1"));
        patterns.add(AttributePattern.getRegexPattern("s5", "^.*c$"));
        patterns.add(AttributePattern.getRegexPattern("s4", "^a.*"));
        patterns.add(AttributePattern.getExactPattern("s2", "abc"));
        patterns.add(AttributePattern.getExactPattern("s3", "abcdef"));
        patterns.add(AttributePattern.getExactPattern("s3", "abcd"));

        setPatterns();

        try {
            mappings.getExactMatchedAttributeNames(null); // null
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no storeName"));
        }

        try {
            mappings.getExactMatchedAttributeNames("  "); // blank
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no storeName"));
        }

        assertNull(mappings.getExactMatchedAttributeNames("s1")); // default
        assertNull(mappings.getExactMatchedAttributeNames("s4")); // regexp
        assertNull(mappings.getExactMatchedAttributeNames("s5")); // regexp

        assertArrayEquals(new String[] { "abc" }, mappings.getExactMatchedAttributeNames("s2"));
        assertArrayEquals(new String[] { "abcdef", "abcd" }, mappings.getExactMatchedAttributeNames("s3")); // multi-matches
    }

    private void setPatterns() throws Exception {
        new BeanWrapperImpl(mappings).setPropertyValue("patterns", patterns);
        invokeMethod(mappings, "init", new Class<?>[] { StoresConfig.class }, new Object[] { stores }, null);
    }

    private static StoresConfig newStores(String... names) throws Exception {
        Class<?> cls = Class.forName(SessionRequestContextFactoryImpl.class.getName() + "$StoresConfigImpl");
        StoresConfig stores = (StoresConfig) BeanUtils.instantiateClass(cls);

        Map<String, SessionStore> storeNames = createLinkedHashMap();

        for (String name : names) {
            storeNames.put(name, createMock(SessionStore.class));
        }

        new BeanWrapperImpl(stores).setPropertyValue("stores", storeNames);

        return stores;
    }

    private static StoreMappingsConfig newStoreMappings() throws Exception {
        Class<?> cls = Class.forName(SessionRequestContextFactoryImpl.class.getName() + "$StoreMappingsConfigImpl");
        return (StoreMappingsConfig) BeanUtils.instantiateClass(cls);
    }
}
