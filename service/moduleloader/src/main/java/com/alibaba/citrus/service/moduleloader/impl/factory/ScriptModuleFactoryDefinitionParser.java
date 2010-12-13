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
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.internal.regex.PathNameWildcardCompiler.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scripting.config.LangNamespaceUtils;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * 解析script-modules。
 * 
 * @author Michael Zhou
 */
public class ScriptModuleFactoryDefinitionParser extends AbstractModuleFactoryDefinitionParser<ScriptModuleFactory> {
    private static final String LANG_URI = "http://www.springframework.org/schema/lang";
    private static final String SCRIPT_SOURCE_ATTRIBUTE = "script-source";

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Map<String, ParsingModuleInfo> scripts = parseSpecificBeans(element, parserContext,
                builder.getRawBeanDefinition(), ns(LANG_URI));

        ElementSelector searchFolders = and(sameNs(element), name("search-folders"));
        ElementSelector searchFiles = and(sameNs(element), name("search-files"));

        for (Element subElement : subElements(element)) {
            String prefix = null;
            String typeName = null;
            String moduleName = null;
            Pattern scriptNamePattern = null;
            String scriptResourceName = null;
            String language = null;

            if (searchFolders.accept(subElement)) {
                String folderName = assertNotNull(normalizePathName(subElement.getAttribute("folders")),
                        "no folder name provided for search-folders");

                // 取prefix
                prefix = getPrefix(folderName);

                if (prefix != null) {
                    folderName = folderName.substring(prefix.length() + 1);
                }

                // folderName不以/开始
                if (folderName.startsWith("/")) {
                    folderName = folderName.substring(1);
                }

                scriptNamePattern = compilePathName(folderName);
                typeName = assertNotNull(trimToNull(subElement.getAttribute("type")), "no type name provided");
                language = trimToNull(subElement.getAttribute("language"));
                scriptResourceName = folderName + "/**/*.*";

                log.trace("Searching in folders: {}, moduleType={}, language={}", new Object[] { folderName, typeName,
                        (language == null ? "auto" : language) });
            } else if (searchFiles.accept(subElement)) {
                String fileName = assertNotNull(normalizePathName(subElement.getAttribute("files")),
                        "no script file name provided for search-files");

                // fileName不以/结尾
                assertTrue(!fileName.endsWith("/"), "invalid script file name: %s", fileName);

                // 取prefix
                prefix = getPrefix(fileName);

                if (prefix != null) {
                    fileName = fileName.substring(prefix.length() + 1);
                }

                // fileName不以/开始
                if (fileName.startsWith("/")) {
                    fileName = fileName.substring(1);
                }

                scriptNamePattern = compilePathName(fileName);
                typeName = assertNotNull(trimToNull(subElement.getAttribute("type")), "no type name provided");
                moduleName = assertNotNull(trimToNull(subElement.getAttribute("name")), "no module name provided");
                language = trimToNull(subElement.getAttribute("language"));
                scriptResourceName = fileName;

                log.trace("Searching for script files: {}, moduleType={}, moduleName={}, language={}", new Object[] {
                        fileName, typeName, moduleName, (language == null ? "auto" : language) });
            }

            if (scriptResourceName != null) {
                scriptResourceName = prefix == null ? scriptResourceName : prefix + ":" + scriptResourceName;

                // 扫描scripts
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(parserContext
                        .getReaderContext().getResourceLoader());
                int found = 0;

                try {
                    Resource[] resources = resolver.getResources(scriptResourceName.replace('?', '*'));
                    BeanDefinitionDefaults defaults = getBeanDefinitionDefaults(subElement, parserContext);
                    ParsingModuleMatcher matcher = new ParsingModuleMatcher(scripts, scriptNamePattern, typeName,
                            moduleName) {
                        @Override
                        protected String getName(String name, String itemName) {
                            String ext = getExt(itemName);

                            if (ext != null && name.endsWith("." + ext)) {
                                return name.substring(0, name.length() - ext.length() - 1);
                            }

                            return name;
                        }
                    };

                    for (Resource resource : resources) {
                        if (resource.isReadable()) {
                            URI uri = resource.getURI();

                            if (uri == null) {
                                continue;
                            }

                            String resourceName = uri.normalize().toString();

                            if (matcher.doMatch(resourceName)) {
                                BeanDefinition scriptBean = createScriptBean(subElement, parserContext, resourceName,
                                        language, defaults);
                                String beanName = matcher.generateBeanName(resourceName, parserContext.getRegistry());

                                parserContext.getRegistry().registerBeanDefinition(beanName, scriptBean);
                                found++;
                            }
                        }
                    }
                } catch (IOException e) {
                    parserContext.getReaderContext().error("Failed to scan resources: " + scriptResourceName,
                            subElement, e);
                    return;
                }

                log.debug("Found {} module scripts with pattern: {}", found, scriptResourceName);
            }
        }

        postProcessItems(element, parserContext, builder, scripts, "search-folders or search-files");
    }

    private String getPrefix(String name) {
        String prefix = null;
        int index = name.indexOf(":");

        if (index >= 0) {
            prefix = name.substring(0, index);
            assertTrue(!prefix.contains("*") && !prefix.contains("?"), "invalid folder or file name: %s", name);
        }

        return prefix;
    }

    private BeanDefinition createScriptBean(Element element, ParserContext parserContext, String resource,
                                            String language, BeanDefinitionDefaults defaults) {
        LangNamespaceUtils.registerScriptFactoryPostProcessorIfNecessary(parserContext.getRegistry());

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(getScriptFactoryClassName(resource,
                language));
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        builder.addConstructorArgValue(resource);

        AbstractBeanDefinition bd = builder.getBeanDefinition();

        bd.applyDefaults(defaults);

        return bd;
    }

    private String getScriptFactoryClassName(String resource, String language) {
        if (language == null) {
            language = getExt(resource);
        }

        assertNotNull(language, "Could not determine the script language: %s", resource);

        language = language.toLowerCase();

        if ("groovy".equals(language)) {
            return "org.springframework.scripting.groovy.GroovyScriptFactory";
        } else if ("jruby".equals(language) || "ruby".equals(language)) {
            return "org.springframework.scripting.jruby.JRubyScriptFactory";
        } else if ("bsh".equals(language)) {
            return "org.springframework.scripting.bsh.BshScriptFactory";
        } else {
            throw new IllegalArgumentException("Unsupported script language: " + language);
        }
    }

    private String getExt(String name) {
        name = name.substring(name.lastIndexOf("/") + 1);

        int index = name.lastIndexOf(".");
        String ext = null;

        if (index > 0) {
            ext = name.substring(index + 1);
        }

        return ext;
    }

    /**
     * 对script-modules，默认lazy-init为true，这是为了防止创建抽象类，导致初始化失败。（class-
     * modules不存在这样的问题。）
     */
    @Override
    protected boolean getDefaultLazyInit() {
        return true;
    }

    @Override
    protected String parseItemName(ParserContext parserContext, Element element, BeanDefinition bd) {
        String resourceName = assertNotNull(trimToNull(element.getAttribute(SCRIPT_SOURCE_ATTRIBUTE)),
                "Missing Attribute: %s", SCRIPT_SOURCE_ATTRIBUTE);

        Resource resource = parserContext.getReaderContext().getResourceLoader().getResource(resourceName);

        try {
            return resource.getURI().normalize().toString();
        } catch (IOException e) {
            parserContext.getReaderContext().error("Failed to get resource: " + resourceName, element, e);
            return null;
        }
    }

    @Override
    protected String getDefaultName() {
        return "scriptModuleFactory";
    }
}
