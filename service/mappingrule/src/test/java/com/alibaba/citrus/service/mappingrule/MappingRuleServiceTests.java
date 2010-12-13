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

import static com.alibaba.citrus.test.TestUtil.*;
import static junit.framework.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.mappingrule.support.AbstractMappingRule;

public class MappingRuleServiceTests extends AbstractMappingRuleServiceTests {
    @Test
    public void testParent() throws Exception {
        ApplicationContext ctx = createBeanFactory("services.xml", createBeanFactory("services-parent.xml"));

        // 默认名称，默认parent
        mappingRules = (MappingRuleService) ctx.getBean("mappingRuleService");
        assertEquals("test.ext0", mappingRules.getMappedName("extension0", "test"));

        // 指定名称，同名parent
        mappingRules = (MappingRuleService) ctx.getBean("extension1");
        assertEquals("test.ext1", mappingRules.getMappedName("extension1", "test"));

        // 指定名称，默认parent
        mappingRules = (MappingRuleService) ctx.getBean("extension2");
        assertEquals("test.ext0", mappingRules.getMappedName("extension0", "test"));

        // 指定名称，指定parent
        mappingRules = (MappingRuleService) ctx.getBean("extension3");
        assertEquals("test.ext1", mappingRules.getMappedName("extension1", "test"));
    }

    /**
     * 测试异常操作，包括不存在的rule，输入空名称等
     */
    @Test
    public void testExceptionOperation() {
        String msg = "";

        MappingRuleService nullmapping = (MappingRuleService) factory.getBean("nullMapping");

        try {
            nullmapping.getMappedName("noexists", "noexists");
        } catch (MappingRuleException e) {
            msg = e.getMessage();
        }

        assertEquals("Failed to get mapping rule of \"noexists\"", msg);

        String result = mappingRules.getMappedName("direct.module", null);
        assertTrue(result == null);

        result = mappingRules.getMappedName("direct.module", "");
        assertTrue(result == null);
    }

    @Test
    public void cacheNull() throws Exception {
        NullRule r = new NullRule();

        r.setCacheEnabled(true);
        r.afterPropertiesSet();

        assertNull(r.getMappedName("test"));

        Map<?, ?> cache = getFieldValue(r, "cache", Map.class);
        assertEquals("", cache.get("test"));
    }

    public static class NullRule extends AbstractMappingRule {
        @Override
        protected String doMapping(String name) {
            return null;
        }
    }
}
