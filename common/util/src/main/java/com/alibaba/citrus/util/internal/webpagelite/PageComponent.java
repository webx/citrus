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
package com.alibaba.citrus.util.internal.webpagelite;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ServletUtil.*;

import java.net.URL;

import com.alibaba.citrus.util.internal.templatelite.Template;

/**
 * 代表一个简单的页面组件。
 * 
 * @author Michael Zhou
 */
public abstract class PageComponent {
    private final PageComponentRegistry registry;
    private final String componentPath;
    private final String componentName;
    private final Template template;

    /**
     * 创建并注册页面组件。
     */
    public PageComponent(PageComponentRegistry registry, String componentPath) {
        this.registry = assertNotNull(registry, "pageComponentRegistry");
        this.componentPath = componentPath = normalizeComponentPath(componentPath);

        registry.register(componentPath, this);

        // 根据naming convention创建template
        String className = getClass().getSimpleName();
        StringBuilder name = new StringBuilder(className);

        if (className.endsWith("Component")) {
            name.setLength(name.length() - "Component".length());
        }

        assertTrue(name.length() > 0, "Invalid page component name: %s", className);

        name.setCharAt(0, Character.toLowerCase(name.charAt(0)));

        this.componentName = name.toString();

        URL templateResource = assertNotNull(getClass().getResource(name + ".htm"),
                "Could not find template for page component class: %s", className);

        template = new Template(templateResource);
    }

    /**
     * 规格化componentPath。
     */
    static String normalizeComponentPath(String componentPath) {
        // 去除开始的/，确保以/结尾，除非是空路径
        componentPath = normalizeURI(componentPath).replaceAll("^/|/$", "") + "/";

        if (componentPath.equals("/")) {
            componentPath = EMPTY_STRING;
        }

        return componentPath;
    }

    public PageComponentRegistry getRegistry() {
        return registry;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public String getComponentName() {
        return componentName;
    }

    public Template getTemplate() {
        return template;
    }

    /**
     * 生成component资源的URL，供visitor调用。
     */
    public String getComponentURL(RequestContext request, String relativeUrl) {
        return request.getResourceURL(getComponentPath() + relativeUrl);
    }
}
