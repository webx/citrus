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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.support.AbstractModuleMappingRule;
import com.alibaba.citrus.service.mappingrule.support.AbstractModuleMappingRuleDefinitionParser;
import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.StringUtil;

/**
 * 最简单模块映射规则。
 * <ol>
 * <li>将<code>"/"</code>替换成<code>"."</code>。</li>
 * <li>除去文件名后缀。</li>
 * <li>将最后一个单词首字母改成大写，以符合模块命名的规则。</li>
 * <li>该rule默认不cache结果。</li>
 * </ol>
 * <p>
 * 例如：将模板名：<code>"about/directions/driving.vm"</code>映射到screen module：
 * <code>"about.directions.Driving"</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class DirectModuleMappingRule extends AbstractModuleMappingRule {
    @Override
    protected boolean isCacheEnabledByDefault() {
        return false;
    }

    @Override
    public String doMapping(String name) {
        String[] parts = StringUtil.split(name, NAME_SEPARATOR);

        if (ArrayUtil.isEmptyArray(parts)) {
            return throwInvalidNameException(name);
        }

        parts[parts.length - 1] = normalizeClassName(parts[parts.length - 1]);

        if (parts[parts.length - 1] == null) {
            return throwInvalidNameException(name);
        }

        return StringUtil.join(parts, MODULE_NAME_SEPARATOR);
    }

    public static class DefinitionParser extends AbstractModuleMappingRuleDefinitionParser<DirectModuleMappingRule> {
        @Override
        protected void doParseModuleMappingRule(Element element, ParserContext parserContext,
                                                BeanDefinitionBuilder builder) {
        }
    }
}
