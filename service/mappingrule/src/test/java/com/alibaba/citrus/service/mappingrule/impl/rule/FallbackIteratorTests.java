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
package com.alibaba.citrus.service.mappingrule.impl.rule;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.alibaba.citrus.service.mappingrule.MappingRuleException;
import com.alibaba.citrus.service.mappingrule.impl.rule.FallbackModuleMappingRule.FallbackModuleIterator;
import com.alibaba.citrus.service.mappingrule.impl.rule.FallbackTemplateMappingRule.FallbackTemplateIterator;

/**
 * ≤‚ ‘<code>FallbackIterator</code>°£
 * 
 * @author Michael Zhou
 */
@RunWith(Parameterized.class)
public class FallbackIteratorTests {
    private FallbackIterator iter;
    private String[] results;
    private String lastName;

    public FallbackIteratorTests(FallbackIterator iter, String lastName, String[] results) {
        this.iter = iter;
        this.lastName = lastName;
        this.results = results;
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = createLinkedList();

        // module, with finalName, matchLastName=true
        add(data, new FallbackModuleIterator("aaa/bbb/myOtherModule.vm", "TemplateModule", true),
                "MyOtherModule", //
                "aaa.bbb.MyOtherModule", "aaa.bbb.Default", "aaa.MyOtherModule", "aaa.Default", "MyOtherModule",
                "Default", "TemplateModule");

        // module, no finalName, matchLastName=true
        add(data, new FallbackModuleIterator("aaa/bbb/myOtherModule.vm", null, true),
                "MyOtherModule", //
                "aaa.bbb.MyOtherModule", "aaa.bbb.Default", "aaa.MyOtherModule", "aaa.Default", "MyOtherModule",
                "Default");

        // module, with finalName, matchLastName=false
        add(data, new FallbackModuleIterator("aaa/bbb,myOtherModule.vm", "TemplateModule", false), "MyOtherModule", //
                "aaa.bbb.MyOtherModule", "aaa.bbb.Default", "aaa.Default", "Default", "TemplateModule");

        // module, no finalName, matchLastName=false
        add(data, new FallbackModuleIterator("aaa/bbb,myOtherModule.vm", null, false), "MyOtherModule", //
                "aaa.bbb.MyOtherModule", "aaa.bbb.Default", "aaa.Default", "Default");

        // template, no prefix, matchLastName=true
        add(data, new FallbackTemplateIterator("aaa,bbb,myOtherModule.vm", null, true),
                "myOtherModule.vm", //
                "aaa/bbb/myOtherModule.vm", "aaa/bbb/default.vm", "aaa/myOtherModule.vm", "aaa/default.vm",
                "myOtherModule.vm", "default.vm");

        // template, with prefix, matchLastName=true
        add(data, new FallbackTemplateIterator("aaa,bbb/myOtherModule.vm", "screen", true),
                "myOtherModule.vm", //
                "screen/aaa/bbb/myOtherModule.vm", "screen/aaa/bbb/default.vm", "screen/aaa/myOtherModule.vm",
                "screen/aaa/default.vm", "screen/myOtherModule.vm", "screen/default.vm");

        // template, no prefix, matchLastName=false
        add(data, new FallbackTemplateIterator("aaa,bbb,myOtherModule.vm", null, false), "myOtherModule.vm", //
                "aaa/bbb/myOtherModule.vm", "aaa/bbb/default.vm", "aaa/default.vm", "default.vm");

        // template, with prefix, matchLastName=false
        add(data, new FallbackTemplateIterator("aaa,bbb/myOtherModule.vm", "screen", false),
                "myOtherModule.vm", //
                "screen/aaa/bbb/myOtherModule.vm", "screen/aaa/bbb/default.vm", "screen/aaa/default.vm",
                "screen/default.vm");

        // template without ext, no prefix, matchLastName=true
        add(data, new FallbackTemplateIterator("aaa,bbb,myOtherModule", null, true),
                "myOtherModule", //
                "aaa/bbb/myOtherModule", "aaa/bbb/default", "aaa/myOtherModule", "aaa/default", "myOtherModule",
                "default");

        // template without ext, with prefix, matchLastName=true
        add(data, new FallbackTemplateIterator("aaa,bbb/myOtherModule", "screen", true),
                "myOtherModule", //
                "screen/aaa/bbb/myOtherModule", "screen/aaa/bbb/default", "screen/aaa/myOtherModule",
                "screen/aaa/default", "screen/myOtherModule", "screen/default");

        // template without ext, no prefix, matchLastName=false
        add(data, new FallbackTemplateIterator("aaa,bbb,myOtherModule", null, false), "myOtherModule", //
                "aaa/bbb/myOtherModule", "aaa/bbb/default", "aaa/default", "default");

        // template without ext, with prefix, matchLastName=false
        add(data, new FallbackTemplateIterator("aaa,bbb/myOtherModule", "screen", false), "myOtherModule", //
                "screen/aaa/bbb/myOtherModule", "screen/aaa/bbb/default", "screen/aaa/default", "screen/default");

        return data;
    }

    private static void add(List<Object[]> data, FallbackIterator iter, String lastName, String... results) {
        data.add(new Object[] { iter, lastName, results });
    }

    @Test
    public void checkIterator() {
        for (String result : results) {
            assertTrue(iter.hasNext());
            assertEquals(result, iter.getNext());
            assertEquals(result, iter.next());
        }

        assertFalse(iter.hasNext());

        try {
            iter.next();
            fail();
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void getLastName() {
        assertEquals(lastName, iter.getLastName());
    }

    @Test
    public void module_invalidName() {
        try {
            new FallbackModuleIterator(" ", null, true);
            fail();
        } catch (MappingRuleException e) {
            assertThat(e, exception("Failed to do mapping for name \" \""));
        }

        try {
            new FallbackModuleIterator(" ,/ ", null, true);
            fail();
        } catch (MappingRuleException e) {
            assertThat(e, exception("Failed to do mapping for name \" ,/ \""));
        }
    }
}
