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

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.support.AbstractTemplateMappingRule;
import com.alibaba.citrus.service.mappingrule.support.AbstractTemplateMappingRuleDefinitionParser;
import com.alibaba.citrus.util.StringUtil;

/**
 * 向上搜索的模板映射规则。
 * <ol>
 * <li>在模板名前加上template prefix（如果有的话）。</li>
 * <li>如果模板名未指定后缀，则不加上后缀。</li>
 * <li>调用template service检查模板是否存在，如果不存在，则查找上一级模块名，一直找到根目录。</li>
 * <li>如果全找不到，则返回第一个不匹配的normalized模块名（即精确匹配），否则返回匹配的模板名。</li>
 * </ol>
 * <p>
 * 例如：将模板名：<code>"about/directions/driving.vm"</code>映射到layout
 * template，将顺次搜索以下模板：
 * </p>
 * <ol>
 * <li><code>&quot;layout/about/directions/driving.vm&quot;</code></li>
 * <li><code>&quot;layout/about/directions/default.vm&quot;</code></li>
 * <li><code>&quot;layout/about/default.vm&quot;</code></li>
 * <li><code>&quot;layout/default.vm&quot;</code></li>
 * </ol>
 * 
 * @author Michael Zhou
 */
public class FallbackTemplateMappingRule extends AbstractTemplateMappingRule {
    public static final String DEFAULT_NAME = "default";
    public static final boolean DEFAULT_MATCH_LAST_NAME = false;

    private boolean matchLastName;

    @Override
    protected void initMappingRule() throws Exception {
        assertNotNull(getTemplateService(), "templateService");
        assertNotNull(getTemplatePrefix(), "templatePrefix");
    }

    @Override
    public String doMapping(String name) {
        FallbackTemplateIterator iter = new FallbackTemplateIterator(name, getTemplatePrefix(), matchLastName);

        // 保存第一个精确的匹配，万一找不到，就返回这个值
        String firstTemplateName = iter.getNext();

        while (iter.hasNext()) {
            String fullTemplateName = iter.next();

            if (getTemplateService().exists(fullTemplateName)) {
                return fullTemplateName;
            }
        }

        return firstTemplateName;
    }

    static class FallbackTemplateIterator extends FallbackIterator {
        private String templatePrefix;
        private String firstExt = EMPTY_STRING;

        public FallbackTemplateIterator(String name, String templatePrefix, boolean matchLastName) {
            super(name, DEFAULT_NAME, null, matchLastName);

            List<String> names = getNames();

            if (names != null && !names.isEmpty()) {
                String n = names.get(names.size() - 1);

                if (!StringUtil.isEmpty(n)) {
                    int extIndex = n.lastIndexOf(EXTENSION_SEPARATOR);

                    if (extIndex != -1) {
                        firstExt = n.substring(extIndex + 1, n.length());
                    }
                }
            }

            this.templatePrefix = trimToNull(templatePrefix);

            init();
        }

        @Override
        protected void invalidName(String name) {
            throwInvalidNameException(name);
        }

        @Override
        protected String normalizeLastName(String lastName) {
            return lastName;
        }

        @Override
        protected String generateFullName(List<String> names) {
            String fullName = StringUtil.join(names, TEMPLATE_NAME_SEPARATOR);

            if (templatePrefix != null) {
                fullName = templatePrefix + TEMPLATE_NAME_SEPARATOR + fullName;
            }

            String n = names.get(names.size() - 1);
            int extIndex = n.lastIndexOf(EXTENSION_SEPARATOR);

            if (extIndex == -1 && !StringUtil.isEmpty(firstExt)) {
                fullName = fullName + EXTENSION_SEPARATOR + firstExt;
            }

            return fullName;
        }
    }

    public static class DefinitionParser extends
            AbstractTemplateMappingRuleDefinitionParser<FallbackTemplateMappingRule> {
        @Override
        protected void doParseTemplateMappingRule(Element element, ParserContext parserContext,
                                                  BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "matchLastName");
        }
    }
}
