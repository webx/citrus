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
package com.alibaba.citrus.service.mappingrule;

import static junit.framework.Assert.*;

import org.junit.Test;

public class DirectTemplateTests extends AbstractMappingRuleTests {
    @Override
    protected String getMappedName_forCacheTest() {
        return mappingRules.getMappedName("direct.template", "aaa/bbb/myOtherModule.vm");
    }

    @Override
    protected boolean isProductionModeSensible() {
        return false;
    }

    @Test
    public void testDirectTemplateMappingRule() {
        String result;

        result = mappingRules.getMappedName("direct.template", "aaa/bbb/myOtherModule.vm");
        assertEquals("myprefix/aaa/bbb/myOtherModule.vm", result);
        assertNotSame(result, mappingRules.getMappedName("direct.template", "aaa/bbb/myOtherModule.vm")); // 不cache

        result = mappingRules.getMappedName("direct.template", "aaa,bbb,myOtherModule.vm");
        assertEquals("myprefix/aaa/bbb/myOtherModule.vm", result);
        assertNotSame(result, mappingRules.getMappedName("direct.template", "aaa,bbb,myOtherModule.vm")); // 不cache

        // 默认后缀
        result = mappingRules.getMappedName("direct.template", "aaa,bbb,myOtherModule");
        assertEquals("myprefix/aaa/bbb/myOtherModule", result);
        assertNotSame(result, mappingRules.getMappedName("direct.template", "aaa,bbb,myOtherModule")); // 不cache

        result = mappingRules.getMappedName("direct.template", "aaa,bbb,nonexistModule.vm");
        assertEquals("myprefix/aaa/bbb/nonexistModule.vm", result);
        assertNotSame(result, mappingRules.getMappedName("direct.template", "aaa/bbb/myOtherModule.vm")); // 不cache
    }
}
