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
package com.alibaba.citrus.webx.config.impl;

import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Collections;
import java.util.Map;

import com.alibaba.citrus.service.configuration.support.PropertiesConfigurationSupport;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.webx.WebxController;
import com.alibaba.citrus.webx.WebxRootController;
import com.alibaba.citrus.webx.config.WebxConfiguration;
import com.alibaba.citrus.webx.impl.WebxControllerImpl;
import com.alibaba.citrus.webx.impl.WebxRootControllerImpl;

/**
 * 实现<code>WebxConfiguration</code>。
 * 
 * @author Michael Zhou
 */
public class WebxConfigurationImpl extends PropertiesConfigurationSupport<WebxConfiguration> implements
        WebxConfiguration {
    public final static String DEFAULT_NAME = "webxConfiguration";

    public WebxConfigurationImpl() {
        super();
    }

    public WebxConfigurationImpl(WebxConfigurationImpl parent) {
        super(parent);
    }

    @Override
    protected String getDefaultName() {
        return DEFAULT_NAME;
    }

    /**
     * 内部链接URL的前缀。内部链接用来显示错误信息、开发者信息。
     */
    public String getInternalPathPrefix() {
        return getProperty("internalPathPrefix", "internal");
    }

    public void setInternalPathPrefix(String internalPathPrefix) {
        setProperty("internalPathPrefix", internalPathPrefix);
    }

    /**
     * Request contexts服务。
     */
    public RequestContextChainingService getRequestContexts() {
        return getBean("requestContexts", "requestContexts", RequestContextChainingService.class);
    }

    public void setRequestContextsRef(String beanName) {
        setProperty("requestContexts", beanName);
    }

    /**
     * Pipeline服务。
     */
    public Pipeline getPipeline() {
        return getBean("pipeline", "pipeline", Pipeline.class, false);
    }

    public void setPipelineRef(String beanName) {
        setProperty("pipeline", beanName);
    }

    /**
     * 用于异常处理的pipeline服务。
     * <p>
     * 可选。假如没有配置这个pipeline，在productionMode下，错误将被sendError，然后由servlet engine来处理。
     * </p>
     */
    public Pipeline getExceptionPipeline() {
        return getBean("exceptionPipeline", "exceptionPipeline", Pipeline.class, false);
    }

    public void setExceptionPipelineRef(String beanName) {
        setProperty("exceptionPipeline", beanName);
    }

    /**
     * 取得一组关于components的配置。
     */
    public ComponentsConfig getComponentsConfig() {
        return getProperty("componentsConfig", null);
    }

    public void setComponentsConfig(ComponentsConfig componentsConfig) {
        setProperty("componentsConfig", componentsConfig);
    }

    public static class ComponentsConfigImpl implements ComponentsConfig {
        private Boolean autoDiscoverComponents;
        private String componentConfigurationLocationPattern;
        private Class<?> defaultControllerClass;
        private Class<?> rootControllerClass;
        private String defaultComponent;
        private Map<String, ComponentConfig> components;
        private WebxRootController rootController;

        public Boolean isAutoDiscoverComponents() {
            return autoDiscoverComponents == null ? true : autoDiscoverComponents;
        }

        public void setAutoDiscoverComponents(boolean autoDiscoverComponents) {
            this.autoDiscoverComponents = autoDiscoverComponents;
        }

        public String getComponentConfigurationLocationPattern() {
            return componentConfigurationLocationPattern == null ? "/WEB-INF/webx-*.xml"
                    : componentConfigurationLocationPattern;
        }

        public void setComponentConfigurationLocationPattern(String componentConfigurationLocationPattern) {
            this.componentConfigurationLocationPattern = trimToNull(componentConfigurationLocationPattern);
        }

        public Class<?> getDefaultControllerClass() {
            return defaultControllerClass == null ? WebxControllerImpl.class : defaultControllerClass;
        }

        public void setDefaultControllerClass(Class<?> defaultControllerClass) {
            this.defaultControllerClass = defaultControllerClass;
        }

        public Class<?> getRootControllerClass() {
            return rootControllerClass == null ? WebxRootControllerImpl.class : rootControllerClass;
        }

        public void setRootControllerClass(Class<?> rootControllerClass) {
            this.rootControllerClass = rootControllerClass;
        }

        public String getDefaultComponent() {
            return defaultComponent;
        }

        public void setDefaultComponent(String defaultComponent) {
            this.defaultComponent = trimToNull(defaultComponent);
        }

        public Map<String, ComponentConfig> getComponents() {
            return components == null ? Collections.<String, ComponentConfig> emptyMap() : components;
        }

        public void setComponents(Map<String, ComponentConfig> components) {
            this.components = components;
        }

        public WebxRootController getRootController() {
            return rootController;
        }

        public void setRootController(WebxRootController rootController) {
            this.rootController = rootController;
        }
    }

    public static class ComponentConfigImpl implements ComponentConfig {
        private String name;
        private String path;
        private WebxController controller;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = trimToNull(name);
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = trimToNull(path);
        }

        public WebxController getController() {
            return controller;
        }

        public void setController(WebxController controller) {
            this.controller = controller;
        }
    }
}
