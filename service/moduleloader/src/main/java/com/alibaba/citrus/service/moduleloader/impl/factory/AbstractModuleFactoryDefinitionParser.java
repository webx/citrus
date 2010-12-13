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
package com.alibaba.citrus.service.moduleloader.impl.factory;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.moduleloader.impl.ModuleKey;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.springext.util.SpringExtUtil;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;

public abstract class AbstractModuleFactoryDefinitionParser<T> extends AbstractNamedBeanDefinitionParser<T> {
    protected final Logger log = LoggerFactory.getLogger(getBeanClass(null));

    /**
     * 解析明确定义的beans，这些beans将覆盖自动搜索而得的module脚本。
     */
    protected final Map<String, ParsingModuleInfo> parseSpecificBeans(Element element, ParserContext parserContext,
                                                                      AbstractBeanDefinition containingBd,
                                                                      ElementSelector beanSelector) {
        Map<String, ParsingModuleInfo> specificBeans = createHashMap();

        for (Element subElement : subElements(element, beanSelector)) {
            BeanDefinitionHolder bdh = (BeanDefinitionHolder) parserContext.getDelegate().parsePropertySubElement(
                    subElement, containingBd);

            String itemName = parseItemName(parserContext, subElement, bdh.getBeanDefinition());

            specificBeans.put(itemName, new ParsingModuleInfo(bdh.getBeanDefinition(), itemName));
        }

        log.debug("Found {} specificly defined modules", specificBeans.size());

        return specificBeans;
    }

    protected abstract String parseItemName(ParserContext parserContext, Element element, BeanDefinition bd);

    protected BeanDefinitionDefaults getBeanDefinitionDefaults(Element element, ParserContext parserContext) {
        BeanDefinitionDefaults defaults = new BeanDefinitionDefaults();
        String defaultLazyInit = trimToNull(element.getAttribute("defaultLazyInit"));
        String defaultAutowireMode = trimToNull(element.getAttribute("defaultAutowireMode"));

        if (defaultLazyInit == null) {
            defaults.setLazyInit(getDefaultLazyInit());
        } else {
            defaults.setLazyInit(Boolean.parseBoolean(defaultLazyInit));
        }

        if (defaultAutowireMode == null) {
            defaultAutowireMode = getDefaultAutowireMode();
        }

        defaults.setAutowireMode(parserContext.getDelegate().getAutowireMode(defaultAutowireMode));

        return defaults;
    }

    protected boolean getDefaultLazyInit() {
        return false;
    }

    protected String getDefaultAutowireMode() {
        return "no";
    }

    protected void postProcessItems(Element element, ParserContext parserContext, BeanDefinitionBuilder builder,
                                    Map<String, ParsingModuleInfo> items, String tags) {
        // 注册所有明确定义的beans
        for (ParsingModuleInfo item : items.values()) {
            if (item.bd != null) {
                assertNotNull(item.key, "Specificly-defined module could not be found in %s: %s", tags, item.itemName);

                item.beanName = SpringExtUtil.generateBeanName(item.getBaseBeanName(), parserContext.getRegistry());
                parserContext.getRegistry().registerBeanDefinition(item.beanName, item.bd);
            }
        }

        // 设置ModuleFactory.setModules()
        List<Object> moduleList = createManagedList(element, parserContext);

        log.debug("Defined {} modules with {}", items.size(), getBeanClass(null).getSimpleName());

        for (ParsingModuleInfo item : items.values()) {
            if (item.beanName != null) {
                BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(ModuleInfo.class);

                bdb.addConstructorArgValue(item.key);
                bdb.addConstructorArgValue(item.beanName);
                bdb.addConstructorArgValue(item.itemName); // className or script resourceName

                moduleList.add(bdb.getBeanDefinition());

                log.debug("Defined module {} with {}", new Object[] { item.key, item.itemName });
            }
        }

        builder.addPropertyValue("modules", moduleList);
    }

    /**
     * 处理每一个匹配。
     */
    protected static class ParsingModuleMatcher {
        private final Map<String, ParsingModuleInfo> items;
        private final Pattern itemPattern;
        private final String typeName;
        private final String moduleName;
        private List<TypeFilter> includeFilters;

        public ParsingModuleMatcher(Map<String, ParsingModuleInfo> items, Pattern itemPattern, String typeName,
                                    String moduleName) {
            this(items, itemPattern, typeName, moduleName, null);
        }

        public ParsingModuleMatcher(Map<String, ParsingModuleInfo> items, Pattern itemPattern, String typeName,
                                    String moduleName, List<TypeFilter> includeFilters) {
            this.items = items;
            this.itemPattern = itemPattern;
            this.typeName = typeName;
            this.moduleName = moduleName;
            this.includeFilters = includeFilters;
        }

        protected boolean doMatch(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            String itemName = metadataReader.getAnnotationMetadata().getClassName();

            Matcher matcher = itemPattern.matcher(itemName);

            if (!matcher.find()) {
                return false;
            }

            //根据用户提供的Filter来过滤当前扫瞄的元信息（属于二次过滤）
            boolean matched = true;
            if (this.includeFilters != null) {
                matched = this.includeFilters.size() < 1;

                for (TypeFilter filter : this.includeFilters) {

                    if (matched) {
                        break;
                    }

                    try {
                        matched = filter.match(metadataReader, metadataReaderFactory);
                    } catch (Exception e) {
                        matched = false;
                    }
                }
            }

            if (!matched) {
                return false;
            }

            return this.match(itemName, matcher);

        }

        protected final boolean doMatch(String itemName) {
            Matcher matcher = itemPattern.matcher(itemName);

            if (!matcher.find()) {
                return false;
            }

            return match(itemName, matcher);
        }

        private boolean match(String itemName, Matcher matcher) {
            MatchResultSubstitution subs = new MatchResultSubstitution(matcher);

            String type = trimToNull(subs.substitute(typeName));
            String name;

            if (type == null) {
                return false;
            }

            if (moduleName == null) {
                int startIndex = subs.getMatch().end() + 1;

                if (itemName.length() > startIndex) {
                    name = itemName.substring(startIndex);
                } else {
                    return false;
                }
            } else {
                name = subs.substitute(moduleName);
            }

            name = getName(name, itemName);

            if (items.containsKey(itemName)) {
                ParsingModuleInfo item = items.get(itemName);

                // 对于特别创建的bean，补充其type和name信息。
                if (item.bd != null && item.key == null) {
                    item.key = new ModuleKey(type, name);
                }

                // 忽略重复匹配的类
                return false;
            } else {
                items.put(itemName, new ParsingModuleInfo(type, name, itemName));
                return true;
            }
        }

        protected String getName(String name, String itemName) {
            return name;
        }

        protected final String generateBeanName(String itemName, BeanDefinitionRegistry registry) {
            ParsingModuleInfo item = assertNotNull(items.get(itemName), "missing %s", itemName);

            item.beanName = SpringExtUtil.generateBeanName(item.getBaseBeanName(), registry);

            return item.beanName;
        }
    }

    /**
     * 代表一个module的解析时信息。
     */
    protected static class ParsingModuleInfo {
        final BeanDefinition bd;
        final String itemName;
        String beanName;
        ModuleKey key;

        public ParsingModuleInfo(String moduleType, String moduleName, String itemName) {
            this.bd = null;
            this.key = new ModuleKey(moduleType, moduleName);
            this.itemName = itemName;
        }

        public ParsingModuleInfo(BeanDefinition bd, String itemName) {
            this.bd = bd;
            this.itemName = itemName;
        }

        public String getBaseBeanName() {
            return "module." + key.getModuleType() + "." + key.getModuleName();
        }
    }
}
