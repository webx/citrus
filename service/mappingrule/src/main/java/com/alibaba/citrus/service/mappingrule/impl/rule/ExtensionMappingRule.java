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

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.support.AbstractMappingRule;
import com.alibaba.citrus.service.mappingrule.support.AbstractMappingRuleDefinitionParser;
import com.alibaba.citrus.util.FileUtil.FileNameAndExtension;
import com.alibaba.citrus.util.StringUtil;

/**
 * 转换文件名后缀的映射规则。
 * <p>
 * 该rule默认不cache结果。
 * </p>
 * 
 * @author Michael Zhou
 */
public class ExtensionMappingRule extends AbstractMappingRule {
    private Map<String, String> extensionMappings;

    public void setExtensionMappings(Map<String, String> extensions) {
        this.extensionMappings = extensions;
    }

    @Override
    protected void initMappingRule() throws Exception {
        if (extensionMappings == null) {
            extensionMappings = createHashMap();
        }
    }

    @Override
    protected boolean isCacheEnabledByDefault() {
        return false;
    }

    @Override
    public String doMapping(String name) {
        FileNameAndExtension names = getFileNameAndExtension(name, true);
        String extension = names.getExtension(); // 可能为null

        if (extension == null) {
            extension = EMPTY_STRING;
        } else {
            extension = extension.toLowerCase();
        }

        // 如果映射规则存在，则替换后缀
        if (extensionMappings.containsKey(extension)) {
            String mapToExtension = extensionMappings.get(extension);

            name = names.getFileName(); // 总不为null

            // 如果以/结尾，就不加后缀。
            if (name.length() == 0 || !StringUtil.contains(NAME_SEPARATOR, name.charAt(name.length() - 1))) {
                // 如果获取的映射后缀不为空，则加上后缀
                if (!StringUtil.isEmpty(mapToExtension)) {
                    name = name + EXTENSION_SEPARATOR + mapToExtension;
                }
            }
        } else {
            // 当后缀不在映射规则中，且后缀为空，则至返回名称的前部
            if (StringUtil.isEmpty(extension)) {
                name = names.getFileName();
            }
        }

        return name;
    }

    public static class DefinitionParser extends AbstractMappingRuleDefinitionParser<ExtensionMappingRule> {
        @Override
        protected void doParseMappingRule(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Map<Object, Object> extensionMappings = createManagedMap(element, parserContext);

            for (Element subElement : subElements(element, and(sameNs(element), name("mapping")))) {
                // 一律转换成小写，支持空字符串，表示无后缀
                String from = trimToEmpty(subElement.getAttribute("extension")).toLowerCase();
                String to = trimToEmpty(subElement.getAttribute("to")).toLowerCase();

                extensionMappings.put(from, to);
            }

            builder.addPropertyValue("extensionMappings", extensionMappings);
        }
    }
}
