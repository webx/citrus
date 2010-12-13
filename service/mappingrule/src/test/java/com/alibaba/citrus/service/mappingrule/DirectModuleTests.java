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

public class DirectModuleTests extends AbstractMappingRuleTests {
    @Override
    protected String getMappedName_forCacheTest() {
        return mappingRules.getMappedName("direct.module", "aaa/bbb/myOtherModule.vm");
    }

    @Override
    protected boolean isProductionModeSensible() {
        return false;
    }

    @Test
    public void testDirectModuleMappingRule() {
        String result;

        result = mappingRules.getMappedName("direct.module", "aaa/bbb/myOtherModule.vm");
        assertEquals("aaa.bbb.MyOtherModule", result);
        assertNotSame(result, mappingRules.getMappedName("direct.module", "aaa/bbb/myOtherModule.vm")); // ²»cache

        result = mappingRules.getMappedName("direct.module", "aaa,bbb,myOtherModule.vm");
        assertEquals("aaa.bbb.MyOtherModule", result);
        assertNotSame(result, mappingRules.getMappedName("direct.module", "aaa,bbb,myOtherModule.vm")); // ²»cache

        result = mappingRules.getMappedName("direct.module", "aaa,bbb,nonexistModule.vm");
        assertEquals("aaa.bbb.NonexistModule", result);
        assertNotSame(result, mappingRules.getMappedName("direct.module", "aaa,bbb,nonexistModule.vm")); // ²»cache
    }
}
