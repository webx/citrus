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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.rewrite.RewriteRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;
import com.alibaba.citrus.util.internal.ToStringBuilder.CollectionBuilder;

/**
 * 创建<code>RewriteRequestContext</code>的工厂。
 */
public class RewriteRequestContextFactoryImpl extends AbstractRequestContextFactory<RewriteRequestContext> {
    private RewriteRule[] rules;

    public void setRules(RewriteRule[] rules) {
        this.rules = rules;
    }

    public RewriteRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
        return new RewriteRequestContextImpl(wrappedContext, rules);
    }

    /**
     * 本类提供了重写request参数和URL的功能。
     */
    public String[] getFeatures() {
        return new String[] { "rewrite" };
    }

    /**
     * Rewrite机制会在prepare阶段，修改parameters和cookie，因此依赖于parser。此外，
     * 第一次访问parser的parameters之前，必须设置locale。设置locale是由setlocale prepare完成的。
     * 由于rewrite prepare会访问parser parameters，因此rewrite必须在setlocale之后。
     */
    public FeatureOrder[] featureOrders() {
        return new FeatureOrder[] { new RequiresFeature("parseRequest"), new AfterFeature("setLocaleAndCharset") };
    }

    @Override
    protected Object dumpConfiguration() {
        if (!isEmptyArray(rules)) {
            return new CollectionBuilder().setPrintCount(true).appendAll(rules);
        }

        return null;
    }
}
