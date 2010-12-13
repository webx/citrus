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
package com.alibaba.citrus.service.template.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.service.template.TemplateService;

/**
 * 实现<code>TemplateService</code>。
 * 
 * @author Michael Zhou
 */
public class TemplateServiceImpl extends AbstractService<TemplateService> implements TemplateService,
        ProductionModeAware {
    private Map<String, TemplateEngine> engines; // engineName -> engine
    private Map<String, TemplateEngine> engineMappings; // ext -> engine
    private Map<String, String> engineNameMappings; // ext -> engineName
    private String defaultExtension;
    private boolean searchExtensions;
    private boolean searchLocalizedTemplates;
    private TemplateSearchingStrategy[] strategies;
    private Boolean cacheEnabled;
    private boolean productionMode = true;
    private Map<TemplateKey, TemplateMatchResult> matchedTemplates;

    public void setEngines(Map<String, TemplateEngine> engines) {
        this.engines = engines;
    }

    public void setEngineNameMappings(Map<String, String> engineNameMappings) {
        this.engineNameMappings = engineNameMappings;
    }

    public void setDefaultExtension(String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    public void setSearchExtensions(boolean searchExtensions) {
        this.searchExtensions = searchExtensions;
    }

    public void setSearchLocalizedTemplates(boolean searchLocalizedTemplates) {
        this.searchLocalizedTemplates = searchLocalizedTemplates;
    }

    public Boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    /**
     * 初始化service。
     */
    @Override
    protected void init() {
        if (engines == null) {
            engines = createHashMap();
        }

        if (engines.isEmpty()) {
            getLogger().warn("No Template Engine registered for TemplateService: id={}", getBeanName());
        }

        if (cacheEnabled == null) {
            cacheEnabled = productionMode; // 如果未指定cacheEnabled，则默认当productionMode时，打开cache。
        }

        if (cacheEnabled) {
            matchedTemplates = createConcurrentHashMap();
        }

        Set<String> remappedNames = createHashSet();
        engineMappings = createTreeMap();

        // 生成engineMappings
        if (engineNameMappings != null) {
            for (Map.Entry<String, String> entry : engineNameMappings.entrySet()) {
                String ext = entry.getKey();
                String engineName = entry.getValue();

                assertTrue(!isEmpty(ext) && !ext.startsWith("."), "Invalid extension: %s", ext);
                assertTrue(engines.containsKey(engineName), "TemplateEngine \"%s\" not defined.  Defined names: %s",
                        engineName, engines.keySet());

                remappedNames.add(engineName);
                engineMappings.put(ext, engines.get(engineName));

                getLogger().debug("Template Name \"*.{}\" mapped to Template Engine: {}", ext, engineName);
            }

            engineNameMappings = null;
        }

        // 对于没有指定mapping的engine，取得其默认后缀
        for (Map.Entry<String, TemplateEngine> entry : engines.entrySet()) {
            String engineName = entry.getKey();
            TemplateEngine engine = entry.getValue();

            if (!remappedNames.contains(engineName)) {
                String[] exts = engine.getDefaultExtensions();

                for (String ext : exts) {
                    ext = normalizeExtension(ext);

                    assertNotNull(ext, "default extensions for engine: %s", engine);

                    engineMappings.put(ext, engine);

                    getLogger().debug("Template Name \"*.{}\" mapped to Template Engine: {}", ext, engineName);
                }
            }
        }

        // searching strategies
        defaultExtension = normalizeExtension(defaultExtension);

        List<TemplateSearchingStrategy> strategyList = createLinkedList();

        if (defaultExtension != null) {
            strategyList.add(new DefaultExtensionStrategy(defaultExtension));
        }

        if (searchExtensions) {
            strategyList.add(new SearchExtensionsStrategy(getSupportedExtensions()));
        }

        if (searchLocalizedTemplates) {
            strategyList.add(new SearchLocalizedTemplatesStrategy());
        }

        strategies = strategyList.toArray(new TemplateSearchingStrategy[strategyList.size()]);
    }

    /**
     * 取得指定模板名后缀对应的engine。
     */
    public TemplateEngine getEngineOfName(String engineName) {
        return engines.get(engineName);
    }

    /**
     * 取得指定模板名后缀对应的engine。
     */
    public TemplateEngine getTemplateEngine(String extension) {
        if (extension == null) {
            return null; // prevent treemap from throwing npe
        }

        return engineMappings.get(extension);
    }

    /**
     * 取得所有被登记的文件名后缀。
     */
    public String[] getSupportedExtensions() {
        return engineMappings.keySet().toArray(new String[engineMappings.size()]);
    }

    /**
     * 判断模板是否存在。
     */
    public boolean exists(String templateName) {
        try {
            findTemplate(templateName);
            return true;
        } catch (TemplateNotFoundException e) {
            return false;
        }
    }

    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     */
    public String getText(String templateName, TemplateContext context) throws TemplateException, IOException {
        TemplateMatchResult result = findTemplate(templateName);
        TemplateEngine engine = assertNotNull(result.getEngine(), "templateEngine");

        return engine.getText(result.getTemplateName(), context);
    }

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, OutputStream ostream) throws TemplateException,
            IOException {
        TemplateMatchResult result = findTemplate(templateName);
        TemplateEngine engine = assertNotNull(result.getEngine(), "templateEngine");

        engine.writeTo(result.getTemplateName(), context, ostream);
    }

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, Writer writer) throws TemplateException,
            IOException {
        TemplateMatchResult result = findTemplate(templateName);
        TemplateEngine engine = assertNotNull(result.getEngine(), "templateEngine");

        engine.writeTo(result.getTemplateName(), context, writer);
    }

    /**
     * 查找指定名称的模板。
     */
    TemplateMatchResult findTemplate(String templateName) {
        assertInitialized();

        TemplateKey key = new TemplateKey(templateName, strategies);
        TemplateMatchResult result;

        if (cacheEnabled) {
            result = matchedTemplates.get(key);

            if (result != null) {
                return result;
            }
        }

        TemplateMatcher matcher = new TemplateMatcher(key) {
            private int i;

            @Override
            public boolean findTemplate() {
                boolean found = false;

                // 保存状态，假如没有匹配，则恢复状态
                String savedTemplateNameWithoutExtension = getTemplateNameWithoutExtension();
                String savedExtension = getExtension();
                TemplateEngine savedEngine = getEngine();
                int savedStrategyIndex = i;

                try {
                    if (i < strategies.length) {
                        found = strategies[i++].findTemplate(this);
                    } else {
                        found = findTemplateInTemplateEngine(this);
                    }
                } finally {
                    if (!found) {
                        // 恢复状态，以便尝试其它平级strategies
                        setTemplateNameWithoutExtension(savedTemplateNameWithoutExtension);
                        setExtension(savedExtension);
                        setEngine(savedEngine);
                        i = savedStrategyIndex;
                    }
                }

                return found;
            }
        };

        if (!matcher.findTemplate()) {
            throw new TemplateNotFoundException("Could not find template \"" + matcher.getOriginalTemplateName() + "\"");
        }

        if (cacheEnabled) {
            result = new TemplateMatchResultImpl(matcher.getTemplateName(), matcher.getEngine());
            matchedTemplates.put(key, result);
        } else {
            result = matcher;
        }

        return result;
    }

    /**
     * 查找模板的最终strategy结点。
     */
    private boolean findTemplateInTemplateEngine(TemplateMatcher matcher) {
        TemplateEngine engine = getTemplateEngine(matcher.getExtension());

        matcher.setEngine(engine);

        if (engine == null) {
            return false;
        }

        String templateName = matcher.getTemplateName();

        getLogger().trace("Searching for template \"{}\" using {}", templateName, engine);

        return engine.exists(templateName);
    }
}
