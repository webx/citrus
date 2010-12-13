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
package com.alibaba.citrus.service.requestcontext.basic.impl;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.basic.BasicRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;

/**
 * 创建<code>BasicRequestContext</code>的工厂。
 * 
 * @author Michael Zhou
 */
public class BasicRequestContextFactoryImpl extends AbstractRequestContextFactory<BasicRequestContext> {
    private Object[] interceptors;

    public void setInterceptors(Object[] interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 包装一个request context。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>对象
     * @return request context
     */
    public BasicRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
        return new BasicRequestContextImpl(wrappedContext, interceptors);
    }

    /**
     * 本类提供了拦截headers的功能。
     */
    public String[] getFeatures() {
        return new String[] { "headerInterceptors" };
    }

    /**
     * 本类提供了基础性的安全机制，因此应该把它放在最前面。
     */
    public FeatureOrder[] featureOrders() {
        return new FeatureOrder[] { new BeforeFeature("*") };
    }
}
