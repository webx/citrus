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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.internal.regex.PathNameWildcardCompiler.*;

import java.util.regex.Pattern;

import com.alibaba.citrus.service.resource.ResourceLoadingService;

/**
 * <code>ResourceAlias</code>、<code>ResourceLoaderMapping</code>、
 * <code>ResourceFilterMapping</code>的基类，提供了正则表达式的支持。
 * 
 * @author Michael Zhou
 */
abstract class ResourcePattern {
    private ResourceLoadingService resourceLoadingService;
    private String patternName;
    private Pattern pattern;
    private int relevancy;

    public ResourceLoadingService getResourceLoadingService() {
        return resourceLoadingService;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public abstract String getPatternType();

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = trimToEmpty(patternName);
    }

    public int getRelevancy() {
        return relevancy;
    }

    public boolean isRelativePattern() {
        return !patternName.startsWith("/");
    }

    /**
     * 初始化loader，并设定loader所在的<code>ResourceLoadingService</code>的实例。
     */
    public final void init(ResourceLoadingService resourceLoadingService) {
        this.pattern = compilePathName(patternName);
        this.relevancy = getPathNameRelevancy(patternName);
        this.resourceLoadingService = assertNotNull(resourceLoadingService, "resourceLoadingService");

        init();
    }

    protected abstract void init();
}
