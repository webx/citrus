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
package com.alibaba.citrus.service.moduleloader.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleAdapterFactory;
import com.alibaba.citrus.service.moduleloader.ModuleFactory;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.moduleloader.ModuleNotFoundException;
import com.alibaba.citrus.service.moduleloader.UnadaptableModuleException;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 实现<code>ModuleLoaderService</code>。
 * 
 * @author Michael Zhou
 */
public class ModuleLoaderServiceImpl extends AbstractService<ModuleLoaderService> implements ModuleLoaderService,
        ProductionModeAware {
    private Map<ModuleKey, Module> moduleCache = createConcurrentHashMap();
    private boolean productionMode = true;
    private Boolean cacheEnabled;
    private ModuleFactory[] factories;
    private ModuleAdapterFactory[] adapters;

    public void setFactories(ModuleFactory[] factories) {
        this.factories = factories;
    }

    public void setAdapters(ModuleAdapterFactory[] adapters) {
        this.adapters = adapters;
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

    @Override
    protected void init() {
        assertNotNull(factories, "factories");
        assertNotNull(adapters, "adapters");

        if (cacheEnabled == null) {
            cacheEnabled = productionMode; // 如果未指定cacheEnabled，则默认当productionMode时，打开cache。
        }

        getLogger().debug("Initialized {}", this);
    }

    public Set<String> getModuleTypes() {
        Set<String> types = createTreeSet();

        for (ModuleFactory factory : factories) {
            types.addAll(factory.getModuleTypes());
        }

        return types;
    }

    public Set<String> getModuleNames(String moduleType) {
        Set<String> names = createTreeSet();

        for (ModuleFactory factory : factories) {
            names.addAll(factory.getModuleNames(moduleType));
        }

        return names;
    }

    public Module getModule(String moduleType, String moduleName) throws ModuleLoaderException, ModuleNotFoundException {
        Module module = getModuleQuiet(moduleType, moduleName);

        if (module == null) {
            throw new ModuleNotFoundException("Module not found: type=" + moduleType + ", name=" + moduleName);
        }

        return module;
    }

    public Module getModuleQuiet(String moduleType, String moduleName) throws ModuleLoaderException {
        ModuleKey moduleKey = new ModuleKey(moduleType, moduleName);
        moduleType = moduleKey.getModuleType();
        moduleName = moduleKey.getModuleName();

        // 先检查cache
        if (cacheEnabled) {
            Module module = moduleCache.get(moduleKey);

            if (module != null) {
                return module;
            }
        }

        // 从factory中装载
        Object moduleObject = null;
        Module module = null;

        for (ModuleFactory factory : factories) {
            moduleObject = factory.getModule(moduleType, moduleName);

            if (moduleObject != null) {
                break;
            }
        }

        // 通过适配器转换接口
        if (moduleObject != null) {
            if (moduleObject instanceof Module) {
                module = (Module) moduleObject; // 假如moduleObject直接实现了接口
            } else {
                for (ModuleAdapterFactory adapter : adapters) {
                    module = adapter.adapt(moduleType, moduleName, moduleObject);

                    if (module != null) {
                        break;
                    }
                }
            }
        }

        if (module == null) {
            if (moduleObject != null) {
                throw new UnadaptableModuleException("Could not adapt object to module: type=" + moduleType + ", name="
                        + moduleName + ", class=" + moduleObject.getClass());
            }
        }

        // 保存到cache。
        if (cacheEnabled && module != null) {
            moduleCache.put(moduleKey, module);
        }

        return module;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("factories", factories);
        mb.append("adapters", adapters);

        return new ToStringBuilder().append(getBeanDescription()).append(mb).toString();
    }
}
