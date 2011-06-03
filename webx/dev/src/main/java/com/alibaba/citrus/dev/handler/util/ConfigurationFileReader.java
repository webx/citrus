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

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.alibaba.citrus.dev.handler.util.DomUtil.ElementFilter;

public class ConfigurationFileReader {
    private final ConfigurationFile[] configurationFiles;
    private final String baseURL;
    private final ResourceLoader loader;

    public ConfigurationFileReader(ResourceLoader loader, String[] configLocations) throws IOException {
        this.loader = assertNotNull(loader);

        // 取得所有顶层resources
        List<Resource> resources = createLinkedList();

        for (String configLocation : configLocations) {
            if (loader instanceof ResourcePatternResolver) {
                Resource[] results = ((ResourcePatternResolver) loader).getResources(configLocation);

                if (!isEmptyArray(results)) {
                    for (Resource res : results) {
                        resources.add(res);
                    }
                }
            } else {
                Resource res = loader.getResource(configLocation);

                if (res != null) {
                    resources.add(res);
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

        for (Resource resource : resources) {
            ConfigurationFile configurationFile = parseConfigurationFile(resource, parsedNames);

            if (configurationFile != null) {
                configurationFiles.add(configurationFile);
            }
        }

        this.configurationFiles = configurationFiles.toArray(new ConfigurationFile[configurationFiles.size()]);
    }

    private ConfigurationFile parseConfigurationFile(final Resource resource, final Set<String> parsedNames) {
        URL url;

        try {
            url = resource.getURL();
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
                        String relativeResourceName = trimToNull(e.attributeValue("resource"));

                        if (relativeResourceName != null) {
                            Resource importedResource;

                            if (relativeResourceName.contains(":")) {
                                importedResource = loader.getResource(relativeResourceName);
                            } else {
                                importedResource = resource.createRelative(relativeResourceName);
                            }

                            ConfigurationFile importedConfigurationFile = parseConfigurationFile(importedResource,
                                    parsedNames);

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

        return new ConfigurationFile(name, url,
                importedConfigurationFiles.toArray(new ConfigurationFile[importedConfigurationFiles.size()]),
                rootElement);
    }

    public ConfigurationFile[] toConfigurationFiles() {
        return configurationFiles;
    }
}
