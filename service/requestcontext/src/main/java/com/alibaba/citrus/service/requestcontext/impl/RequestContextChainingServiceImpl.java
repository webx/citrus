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
package com.alibaba.citrus.service.requestcontext.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.service.requestcontext.RequestContextException;
import com.alibaba.citrus.service.requestcontext.RequestContextFactory;
import com.alibaba.citrus.service.requestcontext.RequestContextInfo;
import com.alibaba.citrus.service.requestcontext.RequestContextInfo.AfterFeature;
import com.alibaba.citrus.service.requestcontext.RequestContextInfo.BeforeFeature;
import com.alibaba.citrus.service.requestcontext.RequestContextInfo.FeatureOrder;
import com.alibaba.citrus.service.requestcontext.RequestContextInfo.RequiresFeature;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 将<code>RequestContext</code>对象串联起来的service。
 * <p>
 * 通过它可以实现多重包装的HTTP request和response。
 * </p>
 */
public class RequestContextChainingServiceImpl extends AbstractService<RequestContextChainingService> implements
        RequestContextChainingService {
    private List<RequestContextFactory<?>> factories;
    private boolean sort;
    private boolean threadContextInheritable;

    public void setFactories(List<RequestContextFactory<?>> factories) {
        this.factories = createArrayList(assertNotNull(factories, "factories"));
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public void setThreadContextInheritable(boolean threadContextInheritable) {
        this.threadContextInheritable = threadContextInheritable;
    }

    @Override
    protected void init() {
        if (factories == null) {
            getLogger().warn("no request context factory defined");
            factories = createArrayList();
        }

        if (sort) {
            // 排序
            Map<Integer, RequestContextFactory<?>> result = createLinkedHashMap();
            Set<Integer> processing = createHashSet();

            for (int i = 0; i < factories.size(); i++) {
                if (!result.containsKey(i)) {
                    sort(i, factories.get(i), result, processing);
                }
            }

            factories = createArrayList(result.values());

            // 检查缺失的features
            Set<String> usableFeatures = createHashSet();

            for (RequestContextFactory<?> f : factories) {
                usableFeatures.addAll(asList(f.getFeatures()));

                FeatureOrder[] featureOrders = f.featureOrders();

                if (featureOrders != null) {
                    for (FeatureOrder requiresFeature : featureOrders) {
                        if (requiresFeature instanceof RequiresFeature) {
                            if (!usableFeatures.contains(requiresFeature.feature)) {
                                throw new IllegalArgumentException(String.format(
                                        "Missing feature of %s, which is required by %s", requiresFeature.feature, f));
                            }
                        }
                    }
                }
            }
        }

        getLogger().debug("Initialized {}", this);
    }

    /**
     * 对factories进行排序，以便符合各自的限定。
     */
    private void sort(int index, RequestContextFactory<?> f, Map<Integer, RequestContextFactory<?>> result,
                      Set<Integer> processing) {
        if (!processing.contains(index)) {
            processing.add(index);

            for (Map.Entry<Integer, RequestContextFactory<?>> entry : getFactoriesBefore(f).entrySet()) {
                sort(entry.getKey(), entry.getValue(), result, processing);
            }

            result.put(index, f);
            processing.remove(index);
        }
    }

    /**
     * 取得所有在f之前的factories。
     */
    private Map<Integer, RequestContextFactory<?>> getFactoriesBefore(RequestContextFactory<?> f) {
        Map<Integer, RequestContextFactory<?>> allBefore = createLinkedHashMap();

        for (int i = 0; i < factories.size(); i++) {
            RequestContextFactory<?> test = factories.get(i);

            if (f != test && compare(test, f) < 0) {
                allBefore.put(i, test);
            }
        }

        return allBefore;
    }

    /**
     * 比较f1和f2，如f1在前，返回-1，如f1在后，则返回1，不确定则返回0。
     * <p>
     * 明确指定的胜于用*指定的。
     * </p>
     */
    private int compare(RequestContextFactory<?> f1, RequestContextFactory<?> f2) {
        boolean f1BeforeF2 = compare(f1, BeforeFeature.class, f2);
        boolean f2AfterF1 = compare(f2, AfterFeature.class, f1);

        if (f1BeforeF2 || f2AfterF1) {
            return -1;
        }

        boolean f2BeforeF1 = compare(f2, BeforeFeature.class, f1);
        boolean f1AfterF2 = compare(f1, AfterFeature.class, f2);

        if (f2BeforeF1 || f1AfterF2) {
            return 1;
        }

        boolean f1BeforeAll = compareWithAll(f1, BeforeFeature.class);
        boolean f2AfterAll = compareWithAll(f2, AfterFeature.class);

        if (f1BeforeAll || f2AfterAll) {
            return -1;
        }

        boolean f2BeforeAll = compareWithAll(f2, BeforeFeature.class);
        boolean f1AfterAll = compareWithAll(f1, AfterFeature.class);

        if (f2BeforeAll || f1AfterAll) {
            return 1;
        }

        return 0;
    }

    private boolean compare(RequestContextFactory<?> f1, Class<? extends FeatureOrder> type, RequestContextFactory<?> f2) {
        FeatureOrder[] featureOrders = f1.featureOrders();
        String[] features = f2.getFeatures();

        if (isEmptyArray(features) || isEmptyArray(featureOrders)) {
            return false;
        }

        for (String feature : features) {
            for (FeatureOrder featureOrder : featureOrders) {
                if (type.isInstance(featureOrder)) {
                    if (isEquals(featureOrder.feature, feature)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean compareWithAll(RequestContextFactory<?> f1, Class<? extends FeatureOrder> type) {
        FeatureOrder[] featureOrders = f1.featureOrders();

        if (isEmptyArray(featureOrders)) {
            return false;
        }

        for (FeatureOrder featureOrder : featureOrders) {
            if (type.isInstance(featureOrder)) {
                if (isEquals(featureOrder.feature, "*")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 取得所有的request context的信息。
     */
    public RequestContextInfo<?>[] getRequestContextInfos() {
        return factories.toArray(new RequestContextInfo<?>[factories.size()]);
    }

    /**
     * 取得<code>RequestContext</code>串。
     * 
     * @param servletContext <code>ServletContext</code>对象
     * @param request <code>HttpServletRequest</code>对象
     * @param response <code>HttpServletResponse</code>对象
     * @return request context
     */
    public RequestContext getRequestContext(ServletContext servletContext, HttpServletRequest request,
                                            HttpServletResponse response) {
        assertInitialized();

        RequestContext requestContext = new SimpleRequestContext(servletContext, request, response);

        // 将requestContext放入request中，以便今后只需要用request就可以取得requestContext。
        // 及早设置setRequestContext，以便随后的prepareRequestContext就能使用。
        RequestContextUtil.setRequestContext(requestContext);

        for (RequestContextFactory<?> factory : factories) {
            requestContext = factory.getRequestContextWrapper(requestContext);

            // 调用<code>requestContext.prepare()</code>方法
            prepareRequestContext(requestContext);

            // 将requestContext放入request中，以便今后只需要用request就可以取得requestContext。
            RequestContextUtil.setRequestContext(requestContext);
        }

        setupSpringWebEnvironment(requestContext.getRequest());

        return requestContext;
    }

    /**
     * 调用<code>requestContext.prepare()</code>方法。
     * 
     * @param requestContext 要初始化的request context
     */
    private void prepareRequestContext(RequestContext requestContext) {
        if (getLogger().isTraceEnabled()) {
            getLogger().trace("Preparing request context: {}", requestContext.getClass().getSimpleName());
        }

        requestContext.prepare();
    }

    /**
     * 由外到内地调用<code>afterRequest()</code>方法。
     * 
     * @param requestContext 要初始化的request context
     * @throws RequestContextException 如果失败
     */
    public void commitRequestContext(RequestContext requestContext) throws RequestContextException {
        assertInitialized();

        for (RequestContext rc = requestContext; rc != null; rc = rc.getWrappedRequestContext()) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("Committing request context: {}", rc.getClass().getSimpleName());
            }

            rc.commit();
        }

        cleanupSpringWebEnvironment(requestContext.getRequest());
        RequestContextUtil.removeRequestContext(requestContext.getRequest());
    }

    private void setupSpringWebEnvironment(HttpServletRequest request) {
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        LocaleContextHolder.setLocale(request.getLocale(), threadContextInheritable);
        RequestContextHolder.setRequestAttributes(attributes, threadContextInheritable);

        getLogger().debug("Bound request context to thread: {}", request);
    }

    private void cleanupSpringWebEnvironment(HttpServletRequest request) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        RequestContextHolder.resetRequestAttributes();
        LocaleContextHolder.resetLocaleContext();

        if (attributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) attributes).requestCompleted();
        }

        getLogger().debug("Cleared thread-bound request context: {}", request);
    }

    @Override
    public String toString() {
        ToStringBuilder buf = new ToStringBuilder();

        buf.append(getBeanDescription()).start();

        for (RequestContextFactory<?> factory : factories) {
            buf.format("  %s with features %s, ordering %s\n", factory.getRequestContextInterface().getSimpleName(),
                    asList(factory.getFeatures()), asList(factory.featureOrders()));
        }

        return buf.end().toString();
    }
}
