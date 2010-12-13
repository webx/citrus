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

public class FallbackModuleTests extends AbstractMappingRuleTests {
    @Override
    protected String getMappedName_forCacheTest() {
        return mappingRules.getMappedName("fallback.module", "aaa/bbb/myOtherModule.vm");
    }

    @Override
    protected boolean isProductionModeSensible() {
        return true;
    }

    @Test
    public void testFallbackModuleMappingRule() {
        String result;

        // Exact match
        result = mappingRules.getMappedName("fallback.module", "aaa/bbb/myOtherModule.vm");
        assertEquals("aaa.bbb.MyOtherModule", result);
        assertSame(result, mappingRules.getMappedName("fallback.module", "aaa/bbb/myOtherModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 1
        result = mappingRules.getMappedName("fallback.module", "aaa/bbb/nonexistModule.vm");
        assertEquals("aaa.bbb.Default", result);
        assertSame(result, mappingRules.getMappedName("fallback.module", "aaa/bbb/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 2
        result = mappingRules.getMappedName("fallback.module", "aaa/nonexistPackage/nonexistModule.vm");
        assertEquals("aaa.Default", result);
        assertSame(result, mappingRules.getMappedName("fallback.module", "aaa/nonexistPackage/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback to default
        result = mappingRules.getMappedName("fallback.module", "nonexistPackage1/nonexistPackage2/nonexistModule.vm");
        assertEquals("MyDefaultModule", result);
        assertSame(result,
                mappingRules.getMappedName("fallback.module", "nonexistPackage1/nonexistPackage2/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回
    }

    @Test
    public void testFallbackModuleMappingRuleNoDefault() {
        String result;

        // Exact match
        result = mappingRules.getMappedName("fallback.module.nodefault", "aaa/bbb/myOtherModule.vm");
        assertEquals("aaa.bbb.MyOtherModule", result);
        assertSame(result, mappingRules.getMappedName("fallback.module.nodefault", "aaa/bbb/myOtherModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 1
        result = mappingRules.getMappedName("fallback.module.nodefault", "aaa/bbb/nonexistModule.vm");
        assertEquals("aaa.bbb.Default", result);
        assertSame(result, mappingRules.getMappedName("fallback.module.nodefault", "aaa/bbb/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 2
        result = mappingRules.getMappedName("fallback.module.nodefault", "aaa/nonexistPackage/nonexistModule.vm");
        assertEquals("aaa.Default", result);
        assertSame(result,
                mappingRules.getMappedName("fallback.module.nodefault", "aaa/nonexistPackage/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback to default - failed - so return the original string(normalized)
        result = mappingRules.getMappedName("fallback.module.nodefault",
                "nonexistPackage1/nonexistPackage2/nonexistModule.vm");
        assertEquals("nonexistPackage1.nonexistPackage2.NonexistModule", result);
        assertSame(result, mappingRules.getMappedName("fallback.module.nodefault",
                "nonexistPackage1/nonexistPackage2/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回
    }
}
