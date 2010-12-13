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

public class ExtensionTests extends AbstractMappingRuleTests {
    @Override
    protected String getMappedName_forCacheTest() {
        return mappingRules.getMappedName("extension", ".jhtml");
    }

    @Override
    protected boolean isProductionModeSensible() {
        return false;
    }

    @Test
    public void testExtensionMappingRule() {
        assertEquals(".jsp", mappingRules.getMappedName("extension", ".jhtml"));

        // 小写后缀
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.jhtml"));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.php"));
        assertEquals("/aaa/bbb/ccc.vm", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.vhtml"));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc."));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc"));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.noext"));
        assertEquals("/aaa/bbb/ccc.abc", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.abc"));
        assertEquals("/aaa/bbb.bak/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb.bak/ccc"));
        assertEquals("/aaa/bbb/ccc/", mappingRules.getMappedName("extension", "/aaa/bbb/ccc/"));

        // 小写后缀，无默认值
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.jhtml"));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.php"));
        assertEquals("/aaa/bbb/ccc.vm", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.vhtml"));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc."));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc"));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.noext"));
        assertEquals("/aaa/bbb/ccc.abc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.abc"));
        assertEquals("/aaa/bbb.bak/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb.bak/ccc"));
        assertEquals("/aaa/bbb/ccc/", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc/"));

        // 大写后缀
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.JHTML"));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.PHP"));
        assertEquals("/aaa/bbb/ccc.vm", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.VHTML"));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc."));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb/ccc"));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.NOEXT"));
        assertEquals("/aaa/bbb/ccc.ABC", mappingRules.getMappedName("extension", "/aaa/bbb/ccc.ABC"));
        assertEquals("/aaa/bbb.bak/ccc.jsp", mappingRules.getMappedName("extension", "/aaa/bbb.bak/ccc"));

        // 大写后缀，无默认值
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.JHTML"));
        assertEquals("/aaa/bbb/ccc.jsp", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.PHP"));
        assertEquals("/aaa/bbb/ccc.vm", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.VHTML"));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc."));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc"));
        assertEquals("/aaa/bbb/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.NOEXT"));
        assertEquals("/aaa/bbb/ccc.ABC", mappingRules.getMappedName("extension.nodef", "/aaa/bbb/ccc.ABC"));
        assertEquals("/aaa/bbb.bak/ccc", mappingRules.getMappedName("extension.nodef", "/aaa/bbb.bak/ccc"));
    }
}
