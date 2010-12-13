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
package com.alibaba.citrus.service.freemarker.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import freemarker.cache.TemplateLoader;

/**
 * 能让freemarker模板系统使用spring resource loader装载模板的适配器.
 * 
 * @author Michael Zhou
 */
public class SpringResourceLoaderAdapter implements TemplateLoader {
    private final ResourceLoader springLoader;
    private final String path;

    public SpringResourceLoaderAdapter(ResourceLoader springLoader, String path) {
        this.springLoader = assertNotNull(springLoader, "spring resource loader");

        path = normalizeAbsolutePath(path, true);

        assertTrue(!isEmpty(path), "path");

        this.path = path + '/';
    }

    public String getPath() {
        return path;
    }

    public Object findTemplateSource(String templateName) {
        Resource resource = springLoader.getResource(path + normalizeTemplateName(templateName));

        if (resource == null || !resource.exists()) {
            return null;
        }

        return new TemplateSource(resource);
    }

    public long getLastModified(Object templateSource) {
        long lastModified;

        try {
            lastModified = getTemplateSource(templateSource).getResource().lastModified();

            if (lastModified <= 0) {
                lastModified = -1; // not supported
            }
        } catch (IOException e) {
            lastModified = -1;
        }

        return lastModified;
    }

    public Reader getReader(Object templateSource, String charset) throws IOException {
        return new InputStreamReader(getTemplateSource(templateSource).getInputStream(), charset);
    }

    public void closeTemplateSource(Object templateSource) throws IOException {
        getTemplateSource(templateSource).close();
    }

    private TemplateSource getTemplateSource(Object templateSource) {
        return (TemplateSource) assertNotNull(templateSource, "templateSource");
    }

    /**
     * 规格化模板名。
     */
    private String normalizeTemplateName(String templateName) {
        templateName = assertNotNull(trimToNull(templateName), "templateName");

        if (templateName.startsWith("/")) {
            templateName = templateName.substring(1);
        }

        return templateName;
    }

    /**
     * 保存resource已经打开的流，以便关闭。
     */
    public static class TemplateSource {
        private final Resource resource;
        private InputStream istream;

        public TemplateSource(Resource resource) {
            this.resource = assertNotNull(resource, "resource");
        }

        public Resource getResource() {
            return resource;
        }

        public InputStream getInputStream() throws IOException {
            if (istream == null) {
                istream = resource.getInputStream();
            }

            return istream;
        }

        public void close() {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                }

                istream = null;
            }
        }

        @Override
        public int hashCode() {
            return 31 + (resource == null ? 0 : resource.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            TemplateSource other = (TemplateSource) obj;

            if (resource == null) {
                if (other.resource != null) {
                    return false;
                }
            } else if (!resource.equals(other.resource)) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return resource.toString();
        }
    }
}
