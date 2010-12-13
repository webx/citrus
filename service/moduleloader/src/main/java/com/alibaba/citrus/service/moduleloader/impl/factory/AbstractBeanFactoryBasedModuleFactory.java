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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.citrus.service.moduleloader.ModuleFactory;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;

/**
 * 通过查找beans来创建相应的modules的工厂。
 * 
 * @author Michael Zhou
 */
abstract class AbstractBeanFactoryBasedModuleFactory implements InitializingBean, BeanFactoryAware, ModuleFactory {
    private BeanFactory beans;
    private Map<String, Map<String, ModuleInfo>> modules;

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beans = beanFactory;
    }

    public void setModules(ModuleInfo[] modules) {
        this.modules = createHashMap();

        if (modules != null) {
            for (ModuleInfo module : modules) {
                Map<String, ModuleInfo> typedModules = this.modules.get(module.getKey().getModuleType());

                if (typedModules == null) {
                    typedModules = createHashMap();
                    this.modules.put(module.getKey().getModuleType(), typedModules);
                }

                typedModules.put(module.getKey().getModuleName(), module);
            }
        }
    }

    public void afterPropertiesSet() {
        assertNotNull(beans, "beans");
        assertNotNull(modules, "modules");
    }

    public Set<String> getModuleTypes() {
        return unmodifiableSet(modules.keySet());
    }

    public Set<String> getModuleNames(String moduleType) {
        Map<String, ModuleInfo> typedModules = modules.get(moduleType);

        if (typedModules == null) {
            return emptySet();
        } else {
            return unmodifiableSet(typedModules.keySet());
        }
    }

    public Object getModule(String moduleType, String moduleName) throws ModuleLoaderException {
        Map<String, ModuleInfo> typedModules = modules.get(moduleType);

        if (typedModules == null) {
            return null;
        } else {
            ModuleInfo module = typedModules.get(moduleName);

            if (module == null) {
                return null;
            }

            if (!beans.containsBean(module.getBeanName())) {
                return null;
            }

            try {
                return beans.getBean(module.getBeanName());
            } catch (Exception e) {
                throw new ModuleLoaderException("Failure loading module: " + moduleType + ":" + moduleName, e);
            }
        }
    }
}
