/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import com.alibaba.citrus.dev.handler.util.DomUtil.ElementFilter;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ConfigurationFileReader {
    private final ConfigurationFile[] configurationFiles;
    private final String              baseURL;
    private final ResourceLoader      loader;

    public ConfigurationFileReader(ResourceLoader loader, String[] configLocations) throws IOException {
        this.loader = assertNotNull(loader);

        // 取得所有顶层resources
        List<NamedResource> resources = createLinkedList();

        for (String configLocation : configLocations) {
            if (loader instanceof ResourcePatternResolver) {
                Resource[] results = ((ResourcePatternResolver) loader).getResources(configLocation);

                if (!isEmptyArray(results)) {
                    for (Resource res : results) {
                        resources.add(new NamedResource(configLocation, res));
                    }
                }
            } else {
                Resource res = loader.getResource(configLocation);

                if (res != null) {
                    resources.add(new NamedResource(configLocation, res));
                }
            }
        }

        // 计算baseurl
        URL base = null;

        try {
            base = URI.create(loader.getResource("WEB-INF/web.xml").getURL().toURI() + "/../..").normalize().toURL();
        } catch (URISyntaxException e) {
            unexpectedException(e);
        }

        this.baseURL = trimEnd(base.toExternalForm(), "/");

        // 读取configuration files
        List<ConfigurationFile> configurationFiles = createLinkedList();
        Set<String> parsedNames = createHashSet();

        for (NamedResource namedResource : resources) {
            ConfigurationFile configurationFile = parseConfigurationFile(namedResource, parsedNames);

            if (configurationFile != null) {
                configurationFiles.add(configurationFile);
            }
        }

        this.configurationFiles = configurationFiles.toArray(new ConfigurationFile[configurationFiles.size()]);
    }

    private ConfigurationFile parseConfigurationFile(final NamedResource namedResource, final Set<String> parsedNames) {
        URL url;

        try {
            url = namedResource.resource.getURL();
        } catch (IOException e) {
            unexpectedException(e);
            return null;
        }

        String name = url.toExternalForm();

        if (name.startsWith(baseURL)) {
            name = name.substring(baseURL.length());
        }

        if (parsedNames.contains(name)) {
            return null;
        }

        parsedNames.add(name);

        final List<ConfigurationFile> importedConfigurationFiles = createLinkedList();
        Element rootElement;

        try {
            rootElement = DomUtil.readDocument(name, url, new ElementFilter() {
                public org.dom4j.Element filter(org.dom4j.Element e) throws Exception {
                    // 删除schemaLocation
                    org.dom4j.Attribute attr = e.attribute(new QName("schemaLocation", new Namespace("xsi",
                                                                                                     "http://www.w3.org/2001/XMLSchema-instance")));

                    if (attr != null) {
                        e.remove(attr);
                    }

                    // 导入beans:import，并删除element
                    if ("http://www.springframework.org/schema/beans".equals(e.getNamespaceURI())
                        && "import".equals(e.getName())) {
                        String importedResourceName = trimToNull(e.attributeValue("resource"));

                        if (importedResourceName != null) {
                            Resource importedResource;

                            if (importedResourceName.contains(":")) {
                                importedResource = loader.getResource(importedResourceName);
                            } else {
                                importedResource = namedResource.resource.createRelative(importedResourceName);
                            }

                            ConfigurationFile importedConfigurationFile = parseConfigurationFile(new NamedResource(
                                    importedResourceName, importedResource), parsedNames);

                            if (importedConfigurationFile != null) {
                                importedConfigurationFiles.add(importedConfigurationFile);
                            }
                        }

                        return null;
                    }

                    return e;
                }
            });
        } catch (Exception e) {
            rootElement = new Element("read-error").setText(getStackTrace(getRootCause(e)));
        }

        return new ConfigurationFile(namedResource.name, url,
                                     importedConfigurationFiles.toArray(new ConfigurationFile[importedConfigurationFiles.size()]),
                                     rootElement);
    }

    public ConfigurationFile[] toConfigurationFiles() {
        return configurationFiles;
    }

    private static class NamedResource {
        private final String   name;
        private final Resource resource;

        public NamedResource(String name, Resource resource) {
            this.name = name;
            this.resource = resource;
        }
    }
}
