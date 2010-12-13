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

import java.util.Locale;

import org.junit.Test;

import com.alibaba.citrus.util.i18n.LocaleUtil;

public class FallbackTemplateTests extends AbstractMappingRuleTests {
    @Override
    protected String getMappedName_forCacheTest() {
        return mappingRules.getMappedName("fallback.template", "aaa/bbb/myOtherModule.vm");
    }

    @Override
    protected boolean isProductionModeSensible() {
        return true;
    }

    @Test
    public void testFallbackTemplateMappingRule() {
        String result;

        // Exact match
        result = mappingRules.getMappedName("fallback.template", "aaa/bbb/myOtherModule.vm");
        assertEquals("myprefix/aaa/bbb/myOtherModule.vm", result);
        assertSame(result, mappingRules.getMappedName("fallback.template", "aaa/bbb/myOtherModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Exact match：空后缀
        result = mappingRules.getMappedName("fallback.template", "aaa/bbb/myOtherModule");
        assertEquals("myprefix/aaa/bbb/myOtherModule", result);//不查找其他扩展名，因此找不到
        assertSame(result, mappingRules.getMappedName("fallback.template", "aaa/bbb/myOtherModule")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 1 输入后缀
        result = mappingRules.getMappedName("fallback.template", "aaa/bbb/nonexistModule.vm");
        assertEquals("myprefix/aaa/bbb/default.vm", result);//不存在返回default，输入扩展名是vm，返回也是vm
        assertSame(result, mappingRules.getMappedName("fallback.template", "aaa/bbb/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 1：空后缀
        result = mappingRules.getMappedName("fallback.template", "aaa/bbb/nonexistModule");
        assertEquals("myprefix/aaa/bbb/default", result);//不存在返回default，输入扩展名是空，返回也是空
        assertSame(result, mappingRules.getMappedName("fallback.template", "aaa/bbb/nonexistModule")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 2 输入后缀
        result = mappingRules.getMappedName("fallback.template", "aaa/nonexistPackage/nonexistModule.vm");
        assertEquals("myprefix/aaa/default.vm", result);//不存在返回default，输入扩展名是vm，返回也是vm
        assertSame(result, mappingRules.getMappedName("fallback.template", "aaa/nonexistPackage/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 2：空后缀
        result = mappingRules.getMappedName("fallback.template", "aaa/nonexistPackage/nonexistModule");
        assertEquals("myprefix/aaa/default", result);//不存在返回default，输入扩展名是空，返回也是空
        assertSame(result, mappingRules.getMappedName("fallback.template", "aaa/nonexistPackage/nonexistModule")); // 由于cache存在，所以第二次应立即返回

        // not found 输入后缀
        result = mappingRules.getMappedName("fallback.template", "nonexistPackage1/nonexistPackage2/nonexistModule.vm");
        assertEquals("myprefix/nonexistPackage1/nonexistPackage2/nonexistModule.vm", result);//全部不存在返回输入的，输入扩展名是vm，返回也是vm
        assertSame(result,
                mappingRules.getMappedName("fallback.template", "nonexistPackage1/nonexistPackage2/nonexistModule.vm")); // 由于cache存在，所以第二次应立即返回
    }

    @Test
    public void testFallbackTemplateMappingRule_HybridExtensions_SearchExts() {
        String result;

        result = mappingRules.getMappedName("fallback.template.searchExts", "aaa/bbb/myOtherModule.jsp");
        assertEquals("myprefix/aaa/bbb/myOtherModule.jsp", result);
        assertSame(result, mappingRules.getMappedName("fallback.template.searchExts", "aaa/bbb/myOtherModule.jsp")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 1
        result = mappingRules.getMappedName("fallback.template.searchExts", "aaa/bbb/nonexistModule.jsp");
        assertEquals("myprefix/aaa/bbb/default.jsp", result);
        assertSame(result, mappingRules.getMappedName("fallback.template.searchExts", "aaa/bbb/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 2
        result = mappingRules.getMappedName("fallback.template.searchExts", "aaa/nonexistPackage/nonexistModule.jsp");
        assertEquals("myprefix/aaa/default.jsp", result);
        assertSame(result,
                mappingRules.getMappedName("fallback.template.searchExts", "aaa/nonexistPackage/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回

        result = mappingRules.getMappedName("fallback.template.searchExts", "ccc/nonexistPackage/nonexistModule.jsp");
        assertEquals("myprefix/ccc/default.jsp", result);
        assertSame(result,
                mappingRules.getMappedName("fallback.template.searchExts", "ccc/nonexistPackage/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回

        // not found
        result = mappingRules.getMappedName("fallback.template.searchExts",
                "nonexistPackage1/nonexistPackage2/nonexistModule.jsp");
        assertEquals("myprefix/nonexistPackage1/nonexistPackage2/nonexistModule.jsp", result);
        assertSame(result, mappingRules.getMappedName("fallback.template.searchExts",
                "nonexistPackage1/nonexistPackage2/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回
    }

    @Test
    public void testFallbackTemplateMappingRule_HybridExtensions_SearchExts_Locale() {
        String result;

        LocaleUtil.setContext(Locale.TAIWAN);

        result = mappingRules.getMappedName("fallback.template.searchExts.local", "aaa/bbb/myOtherModule.jsp");
        assertEquals("myprefix.locale/aaa/bbb/myOtherModule.jsp", result);//实际是myOtherModule.vm
        assertSame(result,
                mappingRules.getMappedName("fallback.template.searchExts.local", "aaa/bbb/myOtherModule.jsp")); // 由于cache存在，所以第二次应立即返回

        LocaleUtil.setContext(Locale.CHINA);
        assertSame(result,
                mappingRules.getMappedName("fallback.template.searchExts.local", "aaa/bbb/myOtherModule.jsp")); // 虽然locale不同，但zh和zh_TW是在一起被搜索的，还是应该从cache中取值

        LocaleUtil.setContext(Locale.TAIWAN);
        // Fallback level 1
        result = mappingRules.getMappedName("fallback.template.searchExts.local", "aaa/bbb/nonexistModule.jsp");
        assertEquals("myprefix.locale/aaa/bbb/default.jsp", result);//实际是default.jsp
        assertSame(result,
                mappingRules.getMappedName("fallback.template.searchExts.local", "aaa/bbb/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回

        // Fallback level 2
        result = mappingRules.getMappedName("fallback.template.searchExts.local",
                "aaa/nonexistPackage/nonexistModule.jsp");
        assertEquals("myprefix.locale/aaa/default.jsp", result);//实际是default.jsp
        assertSame(result, mappingRules.getMappedName("fallback.template.searchExts.local",
                "aaa/nonexistPackage/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回

        result = mappingRules.getMappedName("fallback.template.searchExts.local",
                "ccc/nonexistPackage/nonexistModule.do");
        assertEquals("myprefix.locale/ccc/default.do", result);//实际是default_zh.vm
        assertSame(result, mappingRules.getMappedName("fallback.template.searchExts.local",
                "ccc/nonexistPackage/nonexistModule.do")); // 由于cache存在，所以第二次应立即返回

        // not found
        result = mappingRules.getMappedName("fallback.template.searchExts.local",
                "nonexistPackage1/nonexistPackage2/nonexistModule.jsp");
        assertEquals("myprefix.locale/nonexistPackage1/nonexistPackage2/nonexistModule.jsp", result);
        assertSame(result, mappingRules.getMappedName("fallback.template.searchExts.local",
                "nonexistPackage1/nonexistPackage2/nonexistModule.jsp")); // 由于cache存在，所以第二次应立即返回

        LocaleUtil.resetContext();
    }
}
