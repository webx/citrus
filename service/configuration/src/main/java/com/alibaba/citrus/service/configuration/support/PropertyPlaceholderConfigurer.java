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
package com.alibaba.citrus.service.configuration.support;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * 扩展Spring的
 * {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer}
 * ，增加默认值的功能。
 * <p>
 * 例如：<code>${placeholder:defaultValue}</code>，假如placeholder的值不存在，则默认取得
 * <code>defaultValue</code>。
 * </p>
 * <p>
 * 此外，该类自身的location也可以包含placeholder且支持默认值，例如：
 * </p>
 * 
 * <pre>
 * &lt;services:property-placeholder location="${props:default.properties}" /&gt;
 * </pre>
 * <p>
 * 假如未指定-Dprops=xyz，那么就取默认值：<code>default.properties</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class PropertyPlaceholderConfigurer extends
        org.springframework.beans.factory.config.PropertyPlaceholderConfigurer implements ResourceLoaderAware,
        InitializingBean {
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private ResourceLoader loader;
    private String locationNames;

    public PropertyPlaceholderConfigurer() {
        setIgnoreUnresolvablePlaceholders(true); // 默认值
    }

    public void setResourceLoader(ResourceLoader loader) {
        this.loader = loader;
    }

    public void setLocationNames(String locations) {
        this.locationNames = locations;
    }

    public void afterPropertiesSet() throws Exception {
        assertNotNull(loader, "no resourceLoader");

        if (locationNames != null) {
            locationNames = resolveSystemPropertyPlaceholders(locationNames);
        }

        if (StringUtils.hasLength(locationNames)) {
            String[] locations = StringUtils.commaDelimitedListToStringArray(locationNames);
            List<Resource> resources = createArrayList(locations.length);

            for (String location : locations) {
                location = trimToNull(location);

                if (location != null) {
                    resources.add(loader.getResource(location));
                }
            }

            super.setLocations(resources.toArray(new Resource[resources.size()]));
        }
    }

    private String resolveSystemPropertyPlaceholders(String text) {
        StringBuilder buf = new StringBuilder(text);

        for (int startIndex = buf.indexOf(PLACEHOLDER_PREFIX); startIndex >= 0;) {
            int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());

            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();

                try {
                    String value = resolveSystemPropertyPlaceholder(placeholder);

                    if (value != null) {
                        buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), value);
                        nextIndex = startIndex + value.length();
                    } else {
                        System.err.println("Could not resolve placeholder '" + placeholder + "' in [" + text
                                + "] as system property: neither system property nor environment variable found");
                    }
                } catch (Throwable ex) {
                    System.err.println("Could not resolve placeholder '" + placeholder + "' in [" + text
                            + "] as system property: " + ex);
                }

                startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
            } else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

    private String resolveSystemPropertyPlaceholder(String placeholder) {
        DefaultablePlaceholder dp = new DefaultablePlaceholder(placeholder);
        String value = System.getProperty(dp.placeholder);

        if (value == null) {
            value = System.getenv(dp.placeholder);
        }

        if (value == null) {
            value = dp.defaultValue;
        }

        return value;
    }

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        DefaultablePlaceholder dp = new DefaultablePlaceholder(placeholder);
        String value = super.resolvePlaceholder(dp.placeholder, props, systemPropertiesMode);

        if (value == null) {
            value = dp.defaultValue;
        }

        return value;
    }

    private static class DefaultablePlaceholder {
        private final String defaultValue;
        private final String placeholder;

        public DefaultablePlaceholder(String placeholder) {
            int commaIndex = placeholder.indexOf(":");
            String defaultValue = null;

            if (commaIndex >= 0) {
                defaultValue = trimToEmpty(placeholder.substring(commaIndex + 1));
                placeholder = trimToEmpty(placeholder.substring(0, commaIndex));
            }

            this.placeholder = placeholder;
            this.defaultValue = defaultValue;
        }
    }
}
