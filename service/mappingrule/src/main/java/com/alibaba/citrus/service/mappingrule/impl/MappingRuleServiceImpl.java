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
package com.alibaba.citrus.service.mappingrule.impl;

import static com.alibaba.citrus.util.Assert.*;
import static java.util.Collections.*;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.mappingrule.MappingRule;
import com.alibaba.citrus.service.mappingrule.MappingRuleNotFoundException;
import com.alibaba.citrus.service.mappingrule.MappingRuleService;

public class MappingRuleServiceImpl extends AbstractService<MappingRuleService> implements MappingRuleService,
        ApplicationContextAware {
    private final static String DEFAULT_BEAN_NAME = "mappingRuleService";
    private ApplicationContext factory;
    private MappingRuleService parent;
    private Map<String, MappingRule> rules;

    public void setApplicationContext(ApplicationContext factory) {
        this.factory = factory;
    }

    public MappingRuleService getParent() {
        return parent;
    }

    public void setParent(MappingRuleService parent) {
        this.parent = parent;
    }

    public void setRules(Map<String, MappingRule> rules) {
        this.rules = rules;
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(factory, "beanFactory");

        // 取得parent mapping rules，依次尝试：
        // 1. 在配置文件中明确设置parentRef
        // 2. parent context中同名的对象
        // 3. parent context中默认名称的对象
        if (parent == null && factory.getParent() != null) {
            String parentBeanName = null;

            if (factory.getParent().containsBean(getBeanName())) {
                parentBeanName = getBeanName();
            } else if (factory.getParent().containsBean(DEFAULT_BEAN_NAME)) {
                parentBeanName = DEFAULT_BEAN_NAME;
            }

            if (parentBeanName != null) {
                parent = (MappingRuleService) factory.getParent().getBean(parentBeanName);
            }
        }

        if (rules == null) {
            rules = emptyMap();
        }
    }

    public String getMappedName(String ruleType, String name) {
        MappingRule rule = rules.get(ruleType);

        if (rule == null) {
            if (parent == null) {
                throw new MappingRuleNotFoundException("Failed to get mapping rule of \"" + ruleType + "\"");
            } else {
                return parent.getMappedName(ruleType, name);
            }
        }

        return rule.getMappedName(name);
    }
}
