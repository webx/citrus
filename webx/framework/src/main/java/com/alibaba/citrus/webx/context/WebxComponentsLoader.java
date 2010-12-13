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
package com.alibaba.citrus.webx.context;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.internal.regex.PathNameWildcardCompiler.*;
import static com.alibaba.citrus.webx.WebxConstant.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import com.alibaba.citrus.springext.util.SpringExtUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.WebxController;
import com.alibaba.citrus.webx.WebxRootController;
import com.alibaba.citrus.webx.config.WebxConfiguration;
import com.alibaba.citrus.webx.config.WebxConfiguration.ComponentConfig;
import com.alibaba.citrus.webx.config.WebxConfiguration.ComponentsConfig;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl;
import com.alibaba.citrus.webx.config.impl.WebxConfigurationImpl.ComponentsConfigImpl;

/**
 * 用来装载webx components的装载器。
 * 
 * @author Michael Zhou
 */
public class WebxComponentsLoader extends ContextLoader {
    private final static Logger log = LoggerFactory.getLogger(WebxComponentsLoader.class);
    private String webxConfigurationName;
    private ServletContext servletContext;
    private WebApplicationContext componentsContext;
    private WebxComponentsImpl components;

    /**
     * 取得context中<code>WebxConfiguration</code>的名称。
     */
    public String getWebxConfigurationName() {
        return webxConfigurationName == null ? "webxConfiguration" : webxConfigurationName;
    }

    /**
     * 设置context中<code>WebxConfiguration</code>的名称。
     */
    public void setWebxConfigurationName(String webxConfigurationName) {
        this.webxConfigurationName = trimToNull(webxConfigurationName);
    }

    /**
     * 取得在servlet context中保存component context的key。
     */
    public String getComponentContextAttributeName(String componentName) {
        return COMPONENT_CONTEXT_PREFIX + componentName;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 取得components。
     */
    public WebxComponents getWebxComponents() {
        return components;
    }

    @Override
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) throws IllegalStateException,
            BeansException {
        this.servletContext = servletContext;
        init();

        return super.initWebApplicationContext(servletContext);
    }

    protected void init() {
        setWebxConfigurationName(servletContext.getInitParameter("webxConfigurationName"));
    }

    @Override
    protected final Class<?> determineContextClass(ServletContext servletContext) throws ApplicationContextException {
        String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);

        if (contextClassName != null) {
            try {
                return ClassUtils.forName(contextClassName);
            } catch (ClassNotFoundException ex) {
                throw new ApplicationContextException("Failed to load custom context class [" + contextClassName + "]",
                        ex);
            }
        } else {
            return getDefaultContextClass();
        }
    }

    /**
     * 取得默认的components <code>WebApplicationContext</code>实现类。
     * <p>
     * 子类可以覆盖并修改此方法。
     * </p>
     */
    protected Class<?> getDefaultContextClass() {
        return WebxComponentsContext.class;
    }

    /**
     * 在componentsContext.refresh()之前被调用。
     */
    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext componentsContext) {
        this.componentsContext = componentsContext;

        if (componentsContext instanceof WebxComponentsContext) {
            ((WebxComponentsContext) componentsContext).setLoader(this);
        }
    }

    /**
     * 在创建beanFactory之初被调用。
     * 
     * @param webxComponentsContext
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 由于初始化components依赖于webxConfiguration，而webxConfiguration可能需要由PropertyPlaceholderConfigurer来处理，
        // 此外，其它有一些BeanFactoryPostProcessors会用到components，
        // 因此components必须在PropertyPlaceholderConfigurer之后初始化，并在其它BeanFactoryPostProcessors之前初始化。
        //
        // 下面创建的WebxComponentsCreator辅助类就是用来确保这个初始化顺序：
        // 1. PriorityOrdered - PropertyPlaceholderConfigurer
        // 2. Ordered - WebxComponentsCreator
        // 3. 普通 - 其它BeanFactoryPostProcessors
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(WebxComponentsCreator.class);
        builder.addConstructorArgValue(this);
        BeanDefinition componentsCreator = builder.getBeanDefinition();
        componentsCreator.setAutowireCandidate(false);

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        String name = SpringExtUtil.generateBeanName(WebxComponentsCreator.class.getName(), registry);

        registry.registerBeanDefinition(name, componentsCreator);
    }

    public static class WebxComponentsCreator implements BeanFactoryPostProcessor, Ordered {
        private final WebxComponentsLoader loader;

        public WebxComponentsCreator(WebxComponentsLoader loader) {
            this.loader = assertNotNull(loader, "WebxComponentsLoader");
        }

        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            if (loader.components == null) {
                WebxComponentsImpl components = loader.createComponents(loader.getParentConfiguration(), beanFactory);
                AbstractApplicationContext wcc = (AbstractApplicationContext) components.getParentApplicationContext();
                wcc.addApplicationListener(new SourceFilteringListener(wcc, components));
                loader.components = components;
            }
        }

        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * 初始化所有components。
     */
    public void finishRefresh() {
        components.getWebxRootController().onFinishedProcessContext();

        for (WebxComponent component : components) {
            logInBothServletAndLoggingSystem("Initializing Spring sub WebApplicationContext: " + component.getName());

            WebxComponentContext wcc = (WebxComponentContext) component.getApplicationContext();
            WebxController controller = component.getWebxController();

            wcc.refresh();
            controller.onFinishedProcessContext();
        }

        logInBothServletAndLoggingSystem("WebxComponents: initialization completed");
    }

    private void logInBothServletAndLoggingSystem(String msg) {
        servletContext.log(msg);
        log.info(msg);
    }

    /**
     * 初始化components。
     */
    private WebxComponentsImpl createComponents(WebxConfiguration parentConfiguration,
                                                ConfigurableListableBeanFactory beanFactory) {
        ComponentsConfig componentsConfig = getComponentsConfig(parentConfiguration);

        // 假如isAutoDiscoverComponents==true，试图自动发现components 
        Map<String, String> componentNamesAndLocations = findComponents(componentsConfig, getServletContext());

        // 取得特别指定的components
        Map<String, ComponentConfig> specifiedComponents = componentsConfig.getComponents();

        // 实际要初始化的comonents，为上述两种来源的并集
        Set<String> componentNames = createTreeSet();

        componentNames.addAll(componentNamesAndLocations.keySet());
        componentNames.addAll(specifiedComponents.keySet());

        // 创建root controller
        WebxRootController rootController = componentsConfig.getRootController();

        if (rootController == null) {
            rootController = (WebxRootController) BeanUtils.instantiateClass(componentsConfig.getRootControllerClass());
        }

        // 创建并将components对象置入resolvable dependencies，以便注入到需要的bean中
        WebxComponentsImpl components = new WebxComponentsImpl(componentsContext,
                componentsConfig.getDefaultComponent(), rootController, parentConfiguration);

        beanFactory.registerResolvableDependency(WebxComponents.class, components);

        // 初始化每个component
        for (String componentName : componentNames) {
            ComponentConfig componentConfig = specifiedComponents.get(componentName);

            String componentPath = null;
            WebxController controller = null;

            if (componentConfig != null) {
                componentPath = componentConfig.getPath();
                controller = componentConfig.getController();
            }

            if (controller == null) {
                controller = (WebxController) BeanUtils.instantiateClass(componentsConfig.getDefaultControllerClass());
            }

            WebxComponentImpl component = new WebxComponentImpl(components, componentName, componentPath,
                    componentName.equals(componentsConfig.getDefaultComponent()), controller,
                    getWebxConfigurationName());

            components.addComponent(component);

            prepareComponent(component, componentNamesAndLocations.get(componentName));
        }

        return components;
    }

    private void prepareComponent(WebxComponentImpl component, String componentLocation) {
        String componentName = component.getName();
        WebxComponentContext wcc = new WebxComponentContext(component);

        wcc.setServletContext(getServletContext());
        wcc.setNamespace(componentName);
        wcc.addApplicationListener(new SourceFilteringListener(wcc, component));

        if (componentLocation != null) {
            wcc.setConfigLocation(componentLocation);
        }

        component.setApplicationContext(wcc);

        // 将context保存在servletContext中
        String attrName = getComponentContextAttributeName(componentName);
        getServletContext().setAttribute(attrName, wcc);

        log.debug("Published WebApplicationContext of component {} as ServletContext attribute with name [{}]",
                componentName, attrName);
    }

    /**
     * 查找component名称。
     */
    private Map<String, String> findComponents(ComponentsConfig componentsConfig, ServletContext servletContext) {
        String componentConfigurationLocationPattern = checkComponentConfigurationLocationPattern(componentsConfig
                .getComponentConfigurationLocationPattern());

        Map<String, String> componentNamesAndLocations = createTreeMap();

        if (componentsConfig.isAutoDiscoverComponents()) {
            try {
                ResourcePatternResolver resolver = new ServletContextResourcePatternResolver(servletContext);
                Resource[] componentConfigurations = resolver.getResources(componentConfigurationLocationPattern);
                Pattern pattern = compilePathName(componentConfigurationLocationPattern);

                if (componentConfigurations != null) {
                    for (Resource resource : componentConfigurations) {
                        String path = resource.getURL().getPath();
                        Matcher matcher = pattern.matcher(path);

                        assertTrue(matcher.find(), "unknown component configuration file: %s", path);
                        String componentName = trimToNull(matcher.group(1));

                        if (componentName != null) {
                            componentNamesAndLocations.put(componentName,
                                    componentConfigurationLocationPattern.replace("*", componentName));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return componentNamesAndLocations;
    }

    /**
     * 检查componentConfigurationLocationPattern是否符合以下要求：
     * <ol>
     * <li>非空。</li>
     * <li>包含且只包含一个<code>*</code>。</li>
     * </ol>
     */
    private String checkComponentConfigurationLocationPattern(String componentConfigurationLocationPattern) {
        if (componentConfigurationLocationPattern != null) {
            int index = componentConfigurationLocationPattern.indexOf("*");

            if (index >= 0) {
                index = componentConfigurationLocationPattern.indexOf("*", index + 1);

                if (index < 0) {
                    if (componentConfigurationLocationPattern.startsWith("/")) {
                        componentConfigurationLocationPattern = componentConfigurationLocationPattern.substring(1);
                    }

                    return componentConfigurationLocationPattern;
                }
            }
        }

        throw new IllegalArgumentException("Invalid componentConfigurationLocationPattern: "
                + componentConfigurationLocationPattern);
    }

    /**
     * 从parent configuration中取得components配置。
     */
    private ComponentsConfig getComponentsConfig(WebxConfiguration parentConfiguration) {
        ComponentsConfig componentsConfig = assertNotNull(parentConfiguration, "parentConfiguration")
                .getComponentsConfig();

        if (componentsConfig == null) {
            // create default components configuration
            componentsConfig = new ComponentsConfigImpl();
        }

        return componentsConfig;
    }

    /**
     * 从parent context中取得<code>WebxConfiguration</code>。
     */
    private WebxConfiguration getParentConfiguration() {
        try {
            return (WebxConfiguration) componentsContext.getBean(getWebxConfigurationName());
        } catch (BeansException e) {
            // create default configuration
            WebxConfigurationImpl parentConfiguration = new WebxConfigurationImpl();
            parentConfiguration.setApplicationContext(componentsContext);

            try {
                parentConfiguration.afterPropertiesSet();
            } catch (RuntimeException ee) {
                throw ee;
            } catch (Exception ee) {
                throw new RuntimeException(ee);
            }

            return parentConfiguration;
        }
    }

    private static class WebxComponentsImpl implements WebxComponents, ApplicationListener {
        private final WebxConfiguration parentConfiguration;
        private final WebApplicationContext parentContext;
        private final Map<String, WebxComponent> components;
        private final RootComponent rootComponent;
        private final String defaultComponentName;
        private final WebxRootController rootController;

        public WebxComponentsImpl(WebApplicationContext parentContext, String defaultComponentName,
                                  WebxRootController rootController, WebxConfiguration parentConfiguration) {
            this.parentConfiguration = assertNotNull(parentConfiguration, "no parent webx-configuration");
            this.parentContext = parentContext;
            this.components = createHashMap();
            this.rootComponent = new RootComponent();
            this.defaultComponentName = defaultComponentName;
            this.rootController = assertNotNull(rootController, "no rootController");

            rootController.init(this);
        }

        public WebxConfiguration getParentWebxConfiguration() {
            return parentConfiguration;
        }

        private void addComponent(WebxComponent component) {
            components.put(component.getName(), component);
        }

        public WebxComponent getComponent(String componentName) {
            if (componentName == null) {
                return rootComponent;
            } else {
                return components.get(componentName);
            }
        }

        public String[] getComponentNames() {
            String[] names = components.keySet().toArray(new String[components.size()]);
            Arrays.sort(names);
            return names;
        }

        public WebxComponent getDefaultComponent() {
            return defaultComponentName == null ? null : components.get(defaultComponentName);
        }

        public Iterator<WebxComponent> iterator() {
            return components.values().iterator();
        }

        public WebxComponent findMatchedComponent(String path) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            WebxComponent defaultComponent = getDefaultComponent();
            WebxComponent matched = null;

            // 前缀匹配componentPath。
            for (WebxComponent component : this) {
                if (component == defaultComponent) {
                    continue;
                }

                String componentPath = component.getComponentPath();

                if (!path.startsWith(componentPath)) {
                    continue;
                }

                // path刚好等于componentPath，或者path以componentPath/为前缀
                if (path.length() == componentPath.length() || path.charAt(componentPath.length()) == '/') {
                    matched = component;
                    break;
                }
            }

            // fallback to default component
            if (matched == null) {
                matched = defaultComponent;
            }

            return matched;
        }

        public WebxRootController getWebxRootController() {
            return rootController;
        }

        public WebApplicationContext getParentApplicationContext() {
            return parentContext;
        }

        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                // autowire and init root controller
                autowireAndInitialize(rootController, getParentApplicationContext(),
                        AbstractBeanDefinition.AUTOWIRE_AUTODETECT, "webxRootController");

                rootController.onRefreshContext();
            }
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("parentContext", parentContext);
            mb.append("defaultComponentName", defaultComponentName);
            mb.append("components", components);
            mb.append("rootController", rootController);

            return new ToStringBuilder().append("WebxComponents").append(mb).toString();
        }

        /**
         * 这是一个特殊的component实现，对应于root context。
         */
        private class RootComponent implements WebxComponent {
            public WebxComponents getWebxComponents() {
                return WebxComponentsImpl.this;
            }

            public String getName() {
                return null;
            }

            public String getComponentPath() {
                return EMPTY_STRING;
            }

            public WebxConfiguration getWebxConfiguration() {
                return getParentWebxConfiguration();
            }

            public WebxController getWebxController() {
                unsupportedOperation("RootComponent.getWebxController()");
                return null;
            }

            public WebApplicationContext getApplicationContext() {
                return getParentApplicationContext();
            }

            @Override
            public String toString() {
                return WebxComponentsImpl.this.toString();
            }
        }
    }

    private static class WebxComponentImpl implements WebxComponent, ApplicationListener {
        private final WebxComponents components;
        private final String name;
        private final String componentPath;
        private final WebxController controller;
        private final String webxConfigurationName;
        private WebApplicationContext context;

        public WebxComponentImpl(WebxComponents components, String name, String path, boolean defaultComponent,
                                 WebxController controller, String webxConfigurationName) {
            this.components = assertNotNull(components, "components");
            this.name = assertNotNull(name, "componentName");
            this.controller = assertNotNull(controller, "controller");
            this.webxConfigurationName = assertNotNull(webxConfigurationName, "webxConfigurationName");

            // 规格化path，去除尾部的/；空路径则设为null
            path = trimToNull(normalizeAbsolutePath(path, true));

            if (defaultComponent) {
                assertTrue(path == null, "default component \"%s\" should not have component path \"%s\"", name, path);
                this.componentPath = EMPTY_STRING;
            } else if (path != null) {
                this.componentPath = path;
            } else {
                this.componentPath = "/" + name;
            }

            controller.init(this);
        }

        public WebxComponents getWebxComponents() {
            return components;
        }

        public String getName() {
            return name;
        }

        public String getComponentPath() {
            return componentPath;
        }

        public WebxController getWebxController() {
            return controller;
        }

        public WebxConfiguration getWebxConfiguration() {
            return (WebxConfiguration) context.getBean(webxConfigurationName);
        }

        public WebApplicationContext getApplicationContext() {
            return context;
        }

        private void setApplicationContext(WebApplicationContext context) {
            this.context = context;
        }

        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                // autowire and init controller
                autowireAndInitialize(controller, getApplicationContext(), AbstractBeanDefinition.AUTOWIRE_AUTODETECT,
                        "webxController." + getName());

                controller.onRefreshContext();
            }
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("name", name);
            mb.append("path", componentPath);
            mb.append("controller", controller);
            mb.append("context", context);

            return new ToStringBuilder().append("WebxComponent").append(mb).toString();
        }
    }
}
