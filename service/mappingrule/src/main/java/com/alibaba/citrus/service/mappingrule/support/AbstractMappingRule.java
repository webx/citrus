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
package com.alibaba.citrus.service.mappingrule.support;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.mappingrule.MappingRule;
import com.alibaba.citrus.service.mappingrule.MappingRuleException;
import com.alibaba.citrus.springext.support.BeanSupport;

public abstract class AbstractMappingRule extends BeanSupport implements MappingRule, ProductionModeAware {
    /** 被转换的名称的分隔符。 */
    public static final String NAME_SEPARATOR = ",/";

    /** 被转换的名称的后缀分隔符。 */
    public static final String EXTENSION_SEPARATOR = ".";

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private Boolean cacheEnabled;
    private boolean productionMode = true;
    private Map<String, String> cache;

    public Boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    /**
     * 取得默认的<code>cacheEnabled</code>值。
     * <p>
     * 默认情况下取决于当前是否为生产模式。当<code>productionMode</code>为<code>true</code>
     * 时，打开cache。子类可以改变此行为。
     * </p>
     */
    protected boolean isCacheEnabledByDefault() {
        return isProductionMode();
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    @Override
    protected final void init() throws Exception {
        if (cacheEnabled == null) {
            cacheEnabled = isCacheEnabledByDefault();
        }

        if (cacheEnabled) {
            cache = createConcurrentHashMap();
        }

        initMappingRule();

        log.info("Initialized {} with cache {}", getBeanDescription(), cacheEnabled ? "enabled" : "disabled");
    }

    protected void initMappingRule() throws Exception {
    }

    public final String getMappedName(String name) {
        name = trimToNull(name);

        if (name == null) {
            return null;
        }

        String mappedName = null;

        if (isCacheEnabled()) {
            mappedName = cache.get(name);

            // 如果cache中已经有值了，则直接返回。
            // 注意，cache中的空字符串值代表null。
            if (mappedName != null) {
                return trimToNull(mappedName);
            }
        }

        log.trace("doMapping(\"{}\")", name);

        mappedName = doMapping(name);

        log.debug("doMapping(\"{}\") returned: ", name, mappedName);

        // 注意，可以cache值为null的结果（将null转成空字符串并保存）
        if (isCacheEnabled()) {
            cache.put(name, trimToEmpty(mappedName));
        }

        return mappedName;
    }

    /**
     * 将指定名称映射成指定类型的名称。如果映射失败，则返回<code>null</code>。
     */
    protected abstract String doMapping(String name);

    /**
     * 抛出异常，表示要转换的名称非法或转换失败。
     */
    protected static String throwInvalidNameException(String name) {
        return throwInvalidNameException(name, null);
    }

    /**
     * 抛出异常，表示要转换的名称非法或转换失败。
     */
    protected static String throwInvalidNameException(String name, Exception e) {
        throw new MappingRuleException("Failed to do mapping for name \"" + name + "\"", e);
    }
}
