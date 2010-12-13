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
package com.alibaba.citrus.service.pull.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.util.StringUtil;

public class PullServiceDefinitionParser extends AbstractNamedBeanDefinitionParser<PullServiceImpl> implements
        ContributionAware {
    private final static Logger log = LoggerFactory.getLogger(PullServiceDefinitionParser.class);
    private final static Pattern ID_PATTERN = Pattern.compile("\\w+");
    private final static String[] CLASS_NAME_SUFFIXES = { "Tool", "ToolFactory", "ToolSet", "ToolSetFactory" };
    private final static String[] ELEMENT_NAME_SUFFIXES = { "-tool", "-tool-factory", "-tool-set", "-tool-set-factory" };
    private ConfigurationPoint pullToolsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.pullToolsConfigurationPoint = getSiblingConfigurationPoint("services/pull/factories", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        Map<Object, Object> factories = createManagedMap(element, parserContext);

        for (Element subElement : subElements(element)) {
            doParseTool(subElement, parserContext, builder, factories);
        }

        log.debug("Totally {} tools are registered.", factories.size());

        builder.addPropertyValue("toolFactories", factories);

        // parent
        String parentRef = trimToNull(element.getAttribute("parentRef"));

        if (parentRef != null) {
            builder.addPropertyValue("parent", new RuntimeBeanReference(parentRef));
        }
    }

    private void doParseTool(Element element, ParserContext parserContext, BeanDefinitionBuilder builder,
                             Map<Object, Object> factories) {
        BeanDefinitionHolder bean = parseConfigurationPointBean(element, pullToolsConfigurationPoint, parserContext,
                builder);

        if (bean == null) {
            return;
        }

        String refName = trimToNull(element.getAttribute("ref"));
        String toolName = getToolName(bean, refName, element); // 根据id, ref, element name或class name取得tool name

        assertNotNull(toolName, "missing id for tool: %s", bean);

        assertTrue(!factories.containsKey(toolName), "duplicated tool or tool-set ID: %s", toolName);
        factories.put(toolName, bean);

        if (log.isDebugEnabled()) {
            String targetBeanClass = bean.getBeanDefinition().getBeanClassName();

            if (refName != null) {
                log.debug("Registered tool reference: {}={}", toolName, refName);
            } else {
                log.debug("Registered tool: {}={}", toolName, targetBeanClass);
            }
        }
    }

    private String getToolName(BeanDefinitionHolder bean, String refName, Element element) {
        // 依次尝试：

        // 1. id attribute
        String toolName = bean.getBeanName();

        if (!ID_PATTERN.matcher(toolName).matches()) {
            toolName = null;
        }

        // 2. 取refName
        if (toolName == null) {
            toolName = refName;
        }

        // 3. 取class attribute
        if (toolName == null) {
            String classAttr = trimToNull(element.getAttribute("class"));

            if (classAttr != null) {
                toolName = className2ToolName(classAttr);
            }
        }

        // 4. 根据element名称生成
        String elementName = element.getLocalName();

        if (toolName == null && elementName != null && !elementName.equals("factory")) {
            toolName = dropSuffix(elementName, ELEMENT_NAME_SUFFIXES);
            toolName = trimToNull(StringUtil.toCamelCase(toolName.replace('-', '_')));
        }

        // 5. 根据className生成
        if (toolName == null) {
            BeanDefinition bd = bean.getBeanDefinition();
            String className = bd.getBeanClassName();

            if (className != null) {
                toolName = className2ToolName(className);
            }
        }

        return toolName;
    }

    private String className2ToolName(String className) {
        String toolName;
        toolName = className.substring(className.lastIndexOf(".") + 1);
        toolName = dropSuffix(toolName, CLASS_NAME_SUFFIXES);
        toolName = StringUtil.toCamelCase(toolName.substring(toolName.lastIndexOf("$") + 1));
        return toolName;
    }

    private String dropSuffix(String name, String[] suffixes) {
        String tmp = EMPTY_STRING;

        for (String suffix : suffixes) {
            if (name.endsWith(suffix)) {
                tmp = name.substring(0, name.length() - suffix.length());
            }
        }

        return tmp.length() > 0 ? tmp : name;
    }

    @Override
    protected String getDefaultName() {
        return "pullService";
    }
}
