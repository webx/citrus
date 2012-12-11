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

package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.util.Assert.*;
import static org.springframework.core.io.support.ResourcePatternResolver.*;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.citrus.springext.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 从class loader中装载资源的<code>ResourceResolver</code>实现。
 *
 * @author Michael Zhou
 */
public class ClasspathResourceResolver extends ResourceResolver {
    private final ResourcePatternResolver resolver;

    public ClasspathResourceResolver(ClassLoader classLoader) {
        this.resolver = new PathMatchingResourcePatternResolver(classLoader);
    }

    @Override
    @Nullable
    public Resource getResource(@NotNull String location) {
        org.springframework.core.io.Resource springResource = resolver.getResource(CLASSPATH_URL_PREFIX + location);

        if (springResource != null && springResource.exists()) {
            return new SpringResourceAdapter(springResource);
        } else {
            return null;
        }
    }

    @Override
    @NotNull
    public Resource[] getResources(@NotNull String locationPattern) throws IOException {
        org.springframework.core.io.Resource[] springResources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX + locationPattern);

        if (springResources == null) {
            return new Resource[0];
        } else {
            Resource[] resources = new Resource[springResources.length];

            for (int i = 0; i < springResources.length; i++) {
                resources[i] = new SpringResourceAdapter(springResources[i]);
            }

            return resources;
        }
    }

    private static class SpringResourceAdapter extends Resource {
        private final org.springframework.core.io.Resource springResource;

        private SpringResourceAdapter(org.springframework.core.io.Resource springResource) {
            this.springResource = assertNotNull(springResource, "missing spring resource");
        }

        @Override
        public String getName() {
            try {
                return springResource.getURL().toExternalForm();
            } catch (IOException e) {
                return springResource.getDescription();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return springResource.getInputStream();
        }
    }
}
