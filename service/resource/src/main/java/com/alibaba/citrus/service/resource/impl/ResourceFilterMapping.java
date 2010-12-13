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

import static com.alibaba.citrus.util.ArrayUtil.*;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceFilter;
import com.alibaba.citrus.service.resource.ResourceFilterChain;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class ResourceFilterMapping extends ResourcePattern {
    private final static Logger log = LoggerFactory.getLogger(ResourceFilterMapping.class);
    private ResourceFilter[] filters;

    public ResourceFilter[] getFilters() {
        return filters;
    }

    public void setFilters(ResourceFilter[] filters) {
        this.filters = filters;
    }

    @Override
    public String getPatternType() {
        return "resource-filters";
    }

    @Override
    protected void init() {
        if (filters == null) {
            filters = new ResourceFilter[0];
        }

        for (ResourceFilter filter : filters) {
            filter.init(getResourceLoadingService());
        }
    }

    /**
     * 取得用于执行filter的链。
     */
    public ResourceFilterChain getResourceFilterChain(final ResourceFilterChain root) {
        if (isEmptyArray(filters)) {
            return root;
        }

        return new ResourceFilterChain() {
            private int i = 0;

            public Resource doFilter(ResourceMatchResult filterMatchResult, Set<ResourceLoadingOption> options)
                    throws ResourceNotFoundException {
                ResourceFilter filter;

                if (i < filters.length) {
                    filter = filters[i++];

                    String resourceName = filterMatchResult.getResourceName();

                    log.debug("Applying filter to resource \"{}\": {}", resourceName, filter);

                    return filter.doFilter(filterMatchResult, options, this);
                } else {
                    return root.doFilter(filterMatchResult, options);
                }
            }
        };
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("pattern", getPatternName());
        mb.append("filters", getFilters());

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
