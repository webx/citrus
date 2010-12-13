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
package com.alibaba.citrus.service.requestcontext.lazycommit.impl;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.lazycommit.LazyCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;

/**
 * 创建<code>LazyCommitRequestContext</code>的工厂。
 * 
 * @author Michael Zhou
 */
public class LazyCommitRequestContextFactoryImpl extends AbstractRequestContextFactory<LazyCommitRequestContext> {
    /**
     * 包装一个request context。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>对象
     * @return request context
     */
    public LazyCommitRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
        return new LazyCommitRequestContextImpl(wrappedContext);
    }

    /**
     * 本类提供了延迟提交headers的功能。
     */
    public String[] getFeatures() {
        return new String[] { "lazyCommitHeaders" };
    }

    /**
     * 本类实现了延迟提交headers的功能。但是，假如不对content也进行延迟提交的话，
     * 应用程序所输出的content会导致response提前被提交，从而导致headers无法提交。
     * 而且，headers必须先于content提交。因此，lazyCommitHeaders 必须排在
     * lazyCommitContent之后，且依赖于lazyCommitContent功能。
     */
    public FeatureOrder[] featureOrders() {
        return new FeatureOrder[] { new RequiresFeature("lazyCommitContent") };
    }
}
