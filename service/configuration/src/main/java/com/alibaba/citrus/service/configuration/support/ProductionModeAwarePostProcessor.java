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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.alibaba.citrus.service.configuration.Configuration;
import com.alibaba.citrus.service.configuration.ProductionModeAware;

/**
 * 用来支持<code>ProductionModeSensible</code>的post processor。
 * 
 * @author Michael Zhou
 */
public class ProductionModeAwarePostProcessor implements BeanPostProcessor {
    private final Configuration configuration;

    public ProductionModeAwarePostProcessor(Configuration configuration) {
        this.configuration = assertNotNull(configuration, "configuration");
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ProductionModeAware) {
            ((ProductionModeAware) bean).setProductionMode(configuration.isProductionMode());
        }

        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
