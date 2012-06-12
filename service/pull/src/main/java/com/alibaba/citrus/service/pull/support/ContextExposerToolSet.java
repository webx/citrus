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

package com.alibaba.citrus.service.pull.support;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.citrus.service.pull.ToolSetFactory;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Element;

/**
 * 将spring context中的指定bean转换成pull tool。
 *
 * @author Michael Zhou
 */
public class ContextExposerToolSet implements ToolSetFactory, ApplicationContextAware, InitializingBean {
    private ApplicationContext  context;
    private Map<String, String> toolNamesAndBeanNames;

    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    public void setBeanNames(Map<String, String> toolNamesAndBeanNames) {
        this.toolNamesAndBeanNames = toolNamesAndBeanNames;
    }

    public void afterPropertiesSet() {
        assertNotNull(context, "no context");

        if (toolNamesAndBeanNames == null) {
            toolNamesAndBeanNames = createHashMap();
        }

        // beanName默认等于toolName
        for (Entry<String, String> entry : toolNamesAndBeanNames.entrySet()) {
            String toolName = assertNotNull(trimToNull(entry.getKey()), "missing tool name");
            String beanName = trimToNull(entry.getValue());

            if (beanName == null) {
                beanName = toolName;
                entry.setValue(beanName);
            }
        }
    }

    public boolean isSingleton() {
        return false;
    }

    public Iterable<String> getToolNames() {
        return toolNamesAndBeanNames.keySet();
    }

    public Object createTool(String name) throws Exception {
        String beanName = assertNotNull(toolNamesAndBeanNames.get(name), "no beanName for tool: %s", name);
        return context.getBean(beanName);
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<ContextExposerToolSet> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Map<Object, Object> names = createManagedMap(element, parserContext);
            ElementSelector toolSelector = and(sameNs(element), name("tool"));

            for (Element subElement : subElements(element, toolSelector)) {
                String toolName = assertNotNull(trimToNull(subElement.getAttribute("id")), "no tool id");
                String beanName = trimToNull(subElement.getAttribute("beanName"));

                names.put(toolName, beanName);
            }

            builder.addPropertyValue("beanNames", names);
        }
    }
}
