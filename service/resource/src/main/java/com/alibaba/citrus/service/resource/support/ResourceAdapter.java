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
package com.alibaba.citrus.service.resource.support;

import static com.alibaba.citrus.util.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * 将一个resource loading service装载的资源转换成spring resource的适配器。
 * 
 * @author Michael Zhou
 */
public class ResourceAdapter extends AbstractResource implements ContextResource {
    private final ResourceLoadingSupport loader;
    private final String location;
    private final com.alibaba.citrus.service.resource.Resource resource;
    private final String description;

    public ResourceAdapter(String location, com.alibaba.citrus.service.resource.Resource resource) {
        this(location, resource, null);
    }

    public ResourceAdapter(String location, com.alibaba.citrus.service.resource.Resource resource,
                           ResourceLoadingSupport loader) {
        this.loader = loader;
        this.location = location;
        this.resource = resource;
        this.description = "Resource[" + location + ", loaded by ResourceLoadingService]";
    }

    public com.alibaba.citrus.service.resource.Resource getResource() {
        return resource;
    }

    public String getDescription() {
        return description;
    }

    public String getPathWithinContext() {
        return location;
    }

    @Override
    public boolean exists() {
        return resource.exists();
    }

    @Override
    public URL getURL() throws IOException {
        URL url = resource.getURL();

        if (url == null) {
            throw new IOException(getDescription() + " cannot be resolved as URL");
        }

        return url;
    }

    @Override
    public File getFile() throws IOException {
        File file = resource.getFile();

        if (file == null) {
            throw new IOException(getDescription() + " cannot be resolved as File");
        }

        return file;
    }

    public InputStream getInputStream() throws IOException {
        InputStream istream = resource.getInputStream();

        if (istream == null) {
            throw new IOException(getDescription() + " cannot be resolved as InputStream");
        }

        return istream;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        if (loader == null) {
            unsupportedOperation("no ResourceLoadingSupport");
            return null;
        }

        String pathToUse = StringUtils.applyRelativePath(location, relativePath);

        return loader.getResourceByPath(pathToUse);
    }

    @Override
    public String getFilename() throws IllegalStateException {
        try {
            return StringUtils.getFilename(getURL().getPath());
        } catch (IOException e) {
            return super.getFilename();
        }
    }

    @Override
    public long lastModified() {
        return resource.lastModified();
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

        if (getClass() != obj.getClass()) {
            return false;
        }

        ResourceAdapter other = (ResourceAdapter) obj;

        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }

        return true;
    }
}
