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
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import org.slf4j.Logger;

import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.util.Assert;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;

/**
 * 查找和装载resource的逻辑。
 * 
 * @author Michael Zhou
 */
abstract class AbstractResourceLoadingContext<R> implements ResourceMatchResult {
    private final static Set<ResourceLoadingOption> EMPTY_OPTIONS = emptySet();

    // 不变量
    protected final Logger log;
    protected final ResourceLoadingService parent;
    private final String originalResourceName;
    private final Set<ResourceLoadingOption> originalOptions;
    private final ResourceMapping[] mappings;
    private final BestResourcesMatcher resourcesMatcher;

    // 变量
    private List<ResourceMapping> visitedMappings;
    protected String resourceName; // 当前正在匹配的resourceName
    protected Set<ResourceLoadingOption> options; // 当前正在使用的options
    protected ResourcePattern lastMatchedPattern; // 最近的匹配
    protected MatchResultSubstitution lastSubstitution; // 和最近的匹配对应的替换工具

    /**
     * 创建一个context。
     */
    public AbstractResourceLoadingContext(String resourceName, Set<ResourceLoadingOption> options,
                                          ResourceMapping[] mappings, ResourceLoadingService parent, Logger log) {
        // 不变量
        this.log = assertNotNull(log, "logger");
        this.parent = parent;
        this.originalResourceName = normalizeAbsolutePath(assertNotNull(trimToNull(resourceName), "resourceName"));
        this.originalOptions = defaultIfNull(options, EMPTY_OPTIONS);
        this.mappings = assertNotNull(mappings, "mappings");
        this.resourcesMatcher = new BestResourcesMatcher();

        // 变量
        this.resourceName = originalResourceName;
        this.options = originalOptions;
    }

    /**
     * 实现<code>ResourceMatchResult.getResourceName()</code>。
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * 实现<code>ResourceMatchResult.substitute()</code>。
     */
    public String substitute(String substitution) {
        return resourceName.substring(0, lastSubstitution.getMatch().start())
                + lastSubstitution.substitute(substitution) + resourceName.substring(lastSubstitution.getMatch().end());
    }

    /**
     * 寻找资源的真实逻辑，被filter chain调用，或被getResource直接调用（假如没有filter），或被list调用。
     */
    protected R doLoad(String newResourceName, Set<ResourceLoadingOption> newOptions) throws ResourceNotFoundException {
        resourceName = newResourceName;
        options = newOptions;

        log.trace("Looking for resource: name={}", resourceName);

        R resource = null;
        ResourceNotFoundException chainingException = null;

        if (visitedMappings == null) {
            visitedMappings = createLinkedList();
        }

        if (findBestMatch()) {
            // findBestMatch() 情况1. 找到alias，但没有找到最终的resource mapping
            if (lastMatchedPattern instanceof ResourceAlias) {
                if (parent != null) {
                    log.trace("Resource \"{}\" not found.  Trying to find it in super ResourceLoadingService",
                            resourceName);

                    try {
                        resource = loadParentResource(resourceName, options);
                    } catch (ResourceNotFoundException e) {
                        // alias将改变resourceName，故保存异常作为caused by异常
                        chainingException = e;
                    }
                }
            } else {
                // findBestMatch() 情况2, 3. 找到resource mapping
                ResourceLoaderMapping mapping = (ResourceLoaderMapping) lastMatchedPattern;

                resource = loadMappedResource(mapping, options);

                if (resource == null) {
                    // 假如resourceName被改变，则保存异常作为caused by异常
                    if (!isEquals(resourceName, originalResourceName)) {
                        logResourceNotFound(resourceName);
                        chainingException = new ResourceNotFoundException(String.format(
                                "Could not find resource \"%s\"", resourceName));
                    }
                }
            }
        }

        // findBestMatch() 情况4. 什么也没找到
        else {
            if (parent != null) {
                log.trace("Resource \"{}\" not found.  " + "Trying to find it in super ResourceLoadingService",
                        resourceName);

                // 直接抛出异常，因为送给parent的resourceName并未改变，没必要创建重复的异常信息
                resource = loadParentResource(resourceName, options);
            }
        }

        if (resource == null) {
            logResourceNotFound(originalResourceName);
            throw new ResourceNotFoundException(String.format("Could not find resource \"%s\"", originalResourceName),
                    chainingException);
        }

        log.debug("Found resource \"{}\": {}", originalResourceName, resource);

        return resource;
    }

    /**
     * 查找最佳匹配的&lt;resource&gt;或&lt;resource-alias&gt;。
     */
    private boolean findBestMatch() throws ResourceNotFoundException {
        if (resourcesMatcher.matches(resourceName)) {
            ResourceMapping resourceMapping = resourcesMatcher.bestMatchPettern;

            lastMatchedPattern = resourceMapping;
            lastSubstitution = new MatchResultSubstitution(resourcesMatcher.bestMatchResult);

            // 递归查找resource alias，直到一个resource mapping被匹配为止
            if (resourceMapping instanceof ResourceAlias) {
                ResourceAlias alias = (ResourceAlias) resourceMapping;

                // 设置alias替换后产生新的resourceName
                String newResourceName = substitute(alias.getName());

                if (log.isDebugEnabled()) {
                    log.debug("Resource \"{}\" matched resource-alias pattern: \"{}\".  "
                            + "Use a new resourceName: \"{}\"", new Object[] { resourceName, alias.getPatternName(),
                            newResourceName });
                }

                visitMapping(alias);
                visitedMappings.add(alias);

                resourceName = newResourceName;

                // 递归匹配alias
                findBestMatch();

                // 情形1. findBestMatch()==false, 匹配了一个alias，但没找到可继续匹配项，则返回最后匹配的alias。
                // 情形2. findBestMatch()==true, 匹配了一个alias，并递归找到了经过替换后的新匹配项，则返回最终的匹配。
                // 无论哪种情型，都返回true
            }

            // 如果被匹配的是resource loader mapping，则返回之。
            else if (resourceMapping instanceof ResourceLoaderMapping) {
                ResourceLoaderMapping mapping = (ResourceLoaderMapping) resourceMapping;

                log.debug("Resource \"{}\" matched pattern: \"{}\"", resourceName, mapping.getPatternName());

                visitMapping(mapping);
                visitedMappings.add(mapping);

                // 情形3. 匹配了一个resource，返回true
            }

            return true;
        }

        // 情形4. 什么也没有匹配。
        return false;
    }

    /**
     * 回调函数：访问某个mapping。
     */
    protected abstract void visitMapping(ResourceMapping mapping);

    /**
     * 调用parent resource loading service取得资源。
     */
    protected abstract R loadParentResource(String resourceName, Set<ResourceLoadingOption> options)
            throws ResourceNotFoundException;

    /**
     * 调用mapping取得资源。
     */
    protected abstract R loadMappedResource(ResourceLoaderMapping mapping, Set<ResourceLoadingOption> options);

    /**
     * 被<code>ResourceLoaderContext.getResource()</code>和
     * <code>ResourceListerContext.list()</code>调用的通用方法。
     */
    protected final R loadContextResource(String newResourceName, Set<ResourceLoadingOption> newOptions) {
        assertTrue(!visitedMappings.isEmpty(), Assert.ExceptionType.ILLEGAL_STATE,
                "getResource() can only be called within a ResourceLoader");

        try {
            // 如果当前resourceName和新的resourceName相同，则直接调用parent service
            if (resourceName.equals(newResourceName)) {
                if (parent == null) {
                    log.debug("No parent ResourceLoadingService exists for loading resource \"{}\"", newResourceName);
                    return null;
                } else {
                    return loadParentResource(newResourceName, newOptions);
                }
            } else {
                // 用新的名称装载资源
                log.trace("Trying to find resource \"{}\" using a new resourceName: \"{}\"", resourceName,
                        newResourceName);

                return doLoad(newResourceName, newOptions);
            }
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    private void logResourceNotFound(String resourceName) {
        log.trace("Resource \"{}\" not found", resourceName);
    }

    /**
     * 找出最相关的匹配。
     * <p>
     * 算法：先按pattern相关度排序，再按匹配长度排序。
     * </p>
     */
    protected static abstract class BestMatcher<P extends ResourcePattern> {
        protected String resourceName;
        protected P bestMatchPettern;
        protected MatchResult bestMatchResult;
        private int bestMatchRelevancy;
        private int bestMatchLength;

        protected abstract void init();

        protected abstract boolean accept(P pattern);

        protected abstract P nextPattern();

        public final boolean matches(String resourceName) {
            // 因为此对象会被多次复用，所以使用前必须初始化参数
            this.resourceName = assertNotNull(resourceName, "resourceName");
            this.bestMatchPettern = null;
            this.bestMatchResult = null;
            this.bestMatchRelevancy = -1;
            this.bestMatchLength = -1;

            init();

            for (P pattern = nextPattern(); pattern != null; pattern = nextPattern()) {
                Matcher matcher = pattern.getPattern().matcher(resourceName);

                if (matcher.find() && accept(pattern)) {
                    int relevancy = pattern.getRelevancy();
                    int length = matcher.group().length();

                    if (relevancy > bestMatchRelevancy || relevancy == bestMatchRelevancy && length > bestMatchLength) {
                        bestMatchPettern = pattern;
                        bestMatchResult = matcher;
                        bestMatchRelevancy = relevancy;
                        bestMatchLength = length;
                    }
                }
            }

            return bestMatchLength >= 0;
        }
    }

    /**
     * 找出最匹配的&lt;resource&gt;或&lt;resource-alias&gt;。
     */
    private class BestResourcesMatcher extends BestMatcher<ResourceMapping> {
        private int i;

        @Override
        protected void init() {
            this.i = 0;
            assertNotNull(visitedMappings, "visitedMappings");
        }

        @Override
        protected ResourceMapping nextPattern() {
            if (i < mappings.length) {
                return mappings[i++];
            } else {
                return null;
            }
        }

        @Override
        protected boolean accept(ResourceMapping pattern) {
            // visitedMappings为空，表示是第一个匹配，此时不适用internal mappings
            if (visitedMappings.isEmpty() && pattern.isInternal()) {
                return false;
            }

            // 除去已经匹配过的match，防止死循环
            if (visitedMappings.contains(pattern)) {
                return false;
            }

            return true;
        }
    }
}
