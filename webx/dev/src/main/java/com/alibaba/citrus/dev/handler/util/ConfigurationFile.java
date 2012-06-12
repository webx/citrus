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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.net.URL;

public class ConfigurationFile {
    private final String              name;
    private final URL                 url;
    private final ConfigurationFile[] importedFiles;
    private final Element             rootElement;

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
