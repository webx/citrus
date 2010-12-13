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
package com.alibaba.citrus.service.resource.impl;

import java.util.Set;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLister;
import com.alibaba.citrus.service.resource.ResourceListerContext;
import com.alibaba.citrus.service.resource.ResourceLoader;
import com.alibaba.citrus.service.resource.ResourceLoaderContext;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个resource name pattern到一组resource loader的映射。
 * 
 * @author Michael Zhou
 */
public class ResourceLoaderMapping extends ResourceMapping {
    private ResourceLoader[] loaders;

    public ResourceLoader[] getLoaders() {
        return loaders;
    }

    public void setLoaders(ResourceLoader[] loaders) {
        this.loaders = loaders;
    }

    @Override
    public String getPatternType() {
        return "resource";
    }

    @Override
    protected void init() {
        if (loaders == null) {
            loaders = new ResourceLoader[0];
        }

        for (ResourceLoader loader : loaders) {
            loader.init(getResourceLoadingService());
        }
    }

    public Resource getResource(ResourceLoaderContext context, Set<ResourceLoadingOption> options) {
        Resource firstResult = null;

        // 查找所有的loader：
        // * 返回第一个存在的resource
        // * 假如resource不存在，则返回第一个非空的resource。
        //   例如，file-loader在option=FOR_CREATE时，可能返回一个不存在，但可被创建的resource。
        for (ResourceLoader loader : loaders) {
            Resource resource = loader.getResource(context, options);

            if (resource != null) {
                if (resource.exists()) {
                    return resource;
                }

                if (firstResult == null) {
                    firstResult = resource;
                }
            }
        }

        return firstResult;
    }

    public String[] list(ResourceListerContext context, Set<ResourceLoadingOption> options) {
        for (ResourceLoader loader : loaders) {
            if (loader instanceof ResourceLister) {
                String[] names = ((ResourceLister) loader).list(context, options);

                if (names != null) {
                    return names;
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("pattern", getPatternName());

        if (isInternal()) {
            mb.append("internal", isInternal());
        }

        mb.append("loaders", getLoaders());

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
