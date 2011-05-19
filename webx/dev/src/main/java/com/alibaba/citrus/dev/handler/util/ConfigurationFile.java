package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.net.URL;

public class ConfigurationFile {
    private final String name;
    private final URL url;
    private final ConfigurationFile[] importedFiles;
    private final Element rootElement;

    public ConfigurationFile(String name, URL url, ConfigurationFile[] importedFiles, Element rootElement) {
        this.name = assertNotNull(name, "name");
        this.url = assertNotNull(url, "url");
        this.importedFiles = defaultIfNull(importedFiles, new ConfigurationFile[0]);
        this.rootElement = assertNotNull(rootElement, "root element");
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public ConfigurationFile[] getImportedFiles() {
        return importedFiles;
    }

    public Element getRootElement() {
        return rootElement;
    }
}
