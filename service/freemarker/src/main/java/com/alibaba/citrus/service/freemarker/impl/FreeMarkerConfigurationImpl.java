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
package com.alibaba.citrus.service.freemarker.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static freemarker.core.Configurable.*;
import static freemarker.template.Configuration.*;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.freemarker.FreeMarkerConfiguration;
import com.alibaba.citrus.service.freemarker.FreeMarkerPlugin;
import com.alibaba.citrus.service.freemarker.support.DefaultBeansWrapper;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

import freemarker.cache.StrongCacheStorage;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * 代表一组freemarker engine的配置。
 * 
 * @author Michael Zhou
 */
public class FreeMarkerConfigurationImpl implements FreeMarkerConfiguration {
    private final Logger log;
    private final Configuration configuration = new Configuration();
    private final Map<String, String> properties = createHashMap();
    private boolean productionMode = true;
    private ResourceLoader loader;
    private TemplateLoader templateLoader;
    private String path;
    private String charset;
    private FreeMarkerPlugin[] plugins;

    /**
     * 创建一个freemarker配置。
     */
    public FreeMarkerConfigurationImpl(Logger log) {
        this.log = assertNotNull(log, "log");
    }

    /**
     * 取得用于装载模板的loader。
     */
    public TemplateLoader getTemplateLoader() {
        return templateLoader;
    }

    /**
     * 取得freemarker的配置。
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public ResourceLoader getResourceLoader() {
        return loader;
    }

    /**
     * 设置resource loader。
     */
    public void setResourceLoader(ResourceLoader loader) {
        this.loader = loader;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    /**
     * 设置生产模式。默认为<code>true</code>。
     */
    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    /**
     * 设置搜索模板的根目录。默认为<code>/templates</code>。
     */
    public void setPath(String path) {
        this.path = trimToNull(path);
    }

    /**
     * 设置模板的字符集编码。
     */
    public void setTemplateEncoding(String charset) {
        this.charset = trimToNull(charset);
    }

    /**
     * 设置高级配置。
     */
    public void setAdvancedProperties(Map<String, String> configuration) {
        this.properties.clear();
        this.properties.putAll(configuration);
    }

    /**
     * 设置plugins。
     */
    public void setPlugins(FreeMarkerPlugin[] plugins) {
        this.plugins = plugins;
    }

    /**
     * 初始化configuration。
     */
    public void init() {
        removeReservedProperties();

        initProperties();
        initPlugins();
        initWrapper();
    }

    /**
     * 删除保留的properties，这些properties用户不能修改。
     */
    private void removeReservedProperties() {
        Set<String> keysToRemove = createHashSet();

        keysToRemove.add(DEFAULT_ENCODING_KEY);
        keysToRemove.add(LOCALIZED_LOOKUP_KEY);

        // do removing
        for (String key : keysToRemove) {
            if (properties.containsKey(key)) {
                log.warn("Removed reserved property: {} = {}", key, properties.get(key));
                properties.remove(key);
            }
        }
    }

    private void initProperties() {
        assertNotNull(loader, "resourceLoader");

        // 模板字符集编码
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }

        path = defaultIfNull(path, "/templates");
        templateLoader = new SpringResourceLoaderAdapter(loader, path);

        configuration.setTemplateLoader(templateLoader);

        // 默认使用StrongCacheStorage
        setDefaultProperty(CACHE_STORAGE_KEY, StrongCacheStorage.class.getName());

        // 异常处理器
        setDefaultProperty(TEMPLATE_EXCEPTION_HANDLER_KEY, "rethrow");

        // 其它默认选项
        setDefaultProperty(DEFAULT_ENCODING_KEY, charset);
        setDefaultProperty(OUTPUT_ENCODING_KEY, DEFAULT_CHARSET);
        setDefaultProperty(LOCALIZED_LOOKUP_KEY, "false");

        // 设置选项
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = trimToNull(entry.getValue());

            if (value != null) {
                try {
                    configuration.setSetting(key, value);
                } catch (freemarker.template.TemplateException e) {
                    throw new TemplateException("invalid key and value: " + key + " = " + value, e);
                }
            }
        }
    }

    private void initPlugins() {
        if (plugins != null) {
            for (FreeMarkerPlugin plugin : plugins) {
                plugin.init(this);
            }
        }
    }

    private void initWrapper() {
        // 设置ObjectWrapper，使之支持TemplateContext对象
        configuration.setObjectWrapper(new DefaultBeansWrapper(configuration.getObjectWrapper()));
    }

    /**
     * 设置默认值。如果值已存在，则不覆盖。
     */
    private void setDefaultProperty(String key, String value) {
        if (properties.get(key) == null) {
            properties.put(key, value);
        }
    }

    @Override
    public String toString() {
        return new MapBuilder().setSortKeys(true).setPrintCount(true).appendAll(properties).toString();
    }
}
