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

import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.configuration.Configuration;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 基于properties的configuration实现。
 * 
 * @author Michael Zhou
 */
public abstract class PropertiesConfigurationSupport<C extends Configuration> extends AbstractService<C> implements
        Configuration, ApplicationContextAware {
    private final Map<String, Object> props = createHashMap();
    private PropertiesConfigurationSupport<C> parent;
    private ApplicationContext factory;

    public PropertiesConfigurationSupport() {
        this(null);
    }

    public PropertiesConfigurationSupport(PropertiesConfigurationSupport<C> parent) {
        this.parent = parent;
    }

    public void setApplicationContext(ApplicationContext factory) throws BeansException {
        this.factory = factory;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void init() {
        assertNotNull(factory, "beanFactory");

        if (parent == null && factory.getParent() != null && factory.getParent().containsBean(getDefaultName())) {
            parent = (PropertiesConfigurationSupport<C>) factory.getParent().getBean(getDefaultName());
        }

        if (hasParent()) {
            Boolean productionMode = (Boolean) getPropertyNoParent("productionMode");

            assertTrue(productionMode == null || productionMode.booleanValue() == true,
                    "productionMode cannot be disabled at App's Context");
        }

        if (!isProductionMode()) {
            getLogger().warn("Application is running in Development Mode.");
        } else {
            getLogger().info("Application is running in Production Mode.");
        }
    }

    public boolean isProductionMode() {
        return getProperty("productionMode", Boolean.FALSE);
    }

    public void setProductionMode(boolean productionMode) {
        setProperty("productionMode", productionMode);
    }

    protected final boolean hasParent() {
        return parent != null;
    }

    protected abstract String getDefaultName();

    protected Set<String> keySet() {
        if (parent == null) {
            return props.keySet();
        } else {
            Set<String> keys = createHashSet();

            keys.addAll(parent.keySet());
            keys.addAll(props.keySet());

            return keys;
        }
    }

    protected Object getPropertyNoParent(String key) {
        return props.get(key);
    }

    protected <T> T getProperty(String key, T defaultValue) {
        Object value = getPropertyNoParent(key);

        if (value == null) {
            if (parent == null) {
                value = defaultValue;
            } else {
                value = parent.getProperty(key, defaultValue);
            }
        }

        @SuppressWarnings("unchecked")
        T result = (T) value;

        return result;
    }

    protected <T> T getBean(String key, String defaultBeanName, Class<T> beanType) {
        return getBean(key, defaultBeanName, beanType, true);
    }

    protected <T> T getBean(String key, String defaultBeanName, Class<T> beanType, boolean required) {
        String beanName = getProperty(key, defaultBeanName);

        if (required || factory.containsBean(beanName)) {
            return beanType.cast(factory.getBean(beanName, beanType));
        } else {
            return null;
        }
    }

    protected void setProperty(String key, Object value) {
        if (value instanceof String) {
            value = trimToNull((String) value);
        }

        if (value == null) {
            props.remove(key);
        } else {
            props.put(key, value);
        }
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder().setSortKeys(true).setPrintCount(true);

        for (String key : keySet()) {
            Object parentValue = parent == null ? null : parent.getProperty(key, null);
            Object value = props.get(key);

            if (parentValue != null && value == null) {
                mb.append(key + " (inherited)", parentValue);
            } else if (parentValue != null && value != null) {
                mb.append(key + " (overrided)", value);
            } else {
                mb.append(key, value);
            }
        }

        return new ToStringBuilder().append(getBeanDescription()).append(mb).toString();
    }
}
