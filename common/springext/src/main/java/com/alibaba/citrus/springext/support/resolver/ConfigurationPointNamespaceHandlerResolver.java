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
package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.util.Assert.*;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPoints;

public class ConfigurationPointNamespaceHandlerResolver implements NamespaceHandlerResolver {
    private final ConfigurationPoints cps;
    private final NamespaceHandlerResolver defaultResolver;

    public ConfigurationPointNamespaceHandlerResolver(ConfigurationPoints cps, NamespaceHandlerResolver defaultResolver) {
        this.cps = assertNotNull(cps, "configurationPoints");
        this.defaultResolver = defaultResolver;
    }

    public NamespaceHandler resolve(String namespaceUri) {
        ConfigurationPoint cp = cps.getConfigurationPointByNamespaceUri(namespaceUri);

        if (cp != null) {
            return cp.getNamespaceHandler();
        } else if (defaultResolver != null) {
            return defaultResolver.resolve(namespaceUri);
        } else {
            return null;
        }
    }
}
