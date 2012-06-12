/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.mappingrule;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.citrus.service.mappingrule.impl.MappingRuleServiceImpl;
import com.alibaba.citrus.service.mappingrule.impl.rule.DirectModuleMappingRule;
import com.alibaba.citrus.service.mappingrule.impl.rule.FallbackModuleMappingRule;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

@RunWith(Parameterized.class)
public class MappingRuleServiceSkipValidationTests {
    private final boolean                  skipValidation;
    private       ApplicationContext       factory;
    private       MappingRuleServiceImpl   mappingRuleService;
    private       Map<String, MappingRule> rules;

    public MappingRuleServiceSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "services-skip-validation.xml")));
        mappingRuleService = (MappingRuleServiceImpl) factory.getBean("mappingRuleService");
        rules = getFieldValue(mappingRuleService, "rules", Map.class);
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void rules() {
        DirectModuleMappingRule rule1 = (DirectModuleMappingRule) rules.get("direct.module");
        assertEquals(false, rule1.isCacheEnabled());

        FallbackModuleMappingRule rule2 = (FallbackModuleMappingRule) rules.get("fallback.module");
        assertEquals(true, rule2.isCacheEnabled());
    }
}
