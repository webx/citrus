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
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.MappingRuleException;
import com.alibaba.citrus.service.mappingrule.support.AbstractModuleMappingRule;
import com.alibaba.citrus.service.mappingrule.support.AbstractModuleMappingRuleDefinitionParser;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.util.StringUtil;

/**
 * 向上搜索的模块映射规则。
 * <ol>
 * <li>将<code>"/"</code>替换成<code>"."</code>。</li>
 * <li>除去文件名后缀。</li>
 * <li>将最后一个单词首字母改成大写，以符合模块命名的规则。</li>
 * <li>调用module loader service检查模块是否存在，如果不存在，则查找上一级模块名，一直找到根目录。</li>
 * <li>如果全找不到，则查找默认名称，并确保该名称所代表的模块存在。</li>
 * <li>如果存在，则返回之，否则返回第一个不匹配的normalized模块名（即精确匹配）</li>
 * </ol>
 * <p>
 * 例如：将模板名：<code>"about/directions/driving.vm"</code>映射到screen
 * module，将顺次搜索以下module：
 * </p>
 * <ol>
 * <li><code>&quot;about.directions.Driving&quot;</code></li>
 * <li><code>&quot;about.directions.Default&quot;</code></li>
 * <li><code>&quot;about.Default&quot;</code></li>
 * <li><code>&quot;Default&quot;</code></li>
 * <li><code>&quot;DefaultScreen&quot;</code>（即配置文件中指定的默认module名）</li>
 * </ol>
 * <p>
 * 注意，如果上例中<code>DefaultScreen</code>不存在或未指定默认值，则返回最初的结果：
 * <code>about.directions.Driving</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class FallbackModuleMappingRule extends AbstractModuleMappingRule {
    public static final String DEFAULT_NAME = "Default";
    public static final boolean DEFAULT_MATCH_LAST_NAME = false;

    private String defaultName;
    private boolean matchLastName;

    public void setDefaultName(String defaultName) {
        this.defaultName = trimToNull(defaultName);
    }

    public void setMatchLastName(boolean matchLastName) {
        this.matchLastName = matchLastName;
    }

    @Override
    protected void initMappingRule() {
        assertNotNull(getModuleLoaderService(), "moduleLoaderService");
        assertNotNull(getModuleType(), "moduleType");
    }

    @Override
    public String doMapping(String name) {
        FallbackIterator iter = new FallbackModuleIterator(name, defaultName, matchLastName);

        String moduleName = null;
        String firstModuleName = iter.getNext(); // 保存第一个精确的匹配，万一找不到，就返回这个值

        while (iter.hasNext()) {
            moduleName = iter.next();

            log.debug("Looking for module: " + moduleName);

            try {
                if (getModuleLoaderService().getModuleQuiet(getModuleType(), moduleName) != null) {
                    return moduleName;
                } // else 继续查找
            } catch (ModuleLoaderException e) {
                throw new MappingRuleException(e);
            }
        }

        return firstModuleName;
    }

    static class FallbackModuleIterator extends FallbackIterator {
        public FallbackModuleIterator(String name, String finalName, boolean matchLastName) {
            super(name, DEFAULT_NAME, finalName, matchLastName);
        }

        @Override
        protected void invalidName(String name) {
            throwInvalidNameException(name);
        }

        @Override
        protected String normalizeLastName(String lastName) {
            return normalizeClassName(lastName);
        }

        @Override
        protected String generateFullName(List<String> names) {
            return StringUtil.join(names, AbstractModuleMappingRule.MODULE_NAME_SEPARATOR);
        }
    }

    public static class DefinitionParser extends AbstractModuleMappingRuleDefinitionParser<FallbackModuleMappingRule> {
        @Override
        protected void doParseModuleMappingRule(Element element, ParserContext parserContext,
                                                BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "defaultName", "matchLastName");
        }
    }
}
