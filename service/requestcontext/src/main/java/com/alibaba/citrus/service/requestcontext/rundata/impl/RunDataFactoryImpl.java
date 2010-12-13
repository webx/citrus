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
package com.alibaba.citrus.service.requestcontext.rundata.impl;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;

public class RunDataFactoryImpl extends AbstractRequestContextFactory<RunData> {
    public RunData getRequestContextWrapper(RequestContext wrappedContext) {
        return new RunDataImpl(wrappedContext);
    }

    /**
     * RunData本身不提供新功能。
     */
    public String[] getFeatures() {
        return null;
    }

    /**
     * RunData必须排在所有request context之后，并且依赖某几种request context。
     */
    public FeatureOrder[] featureOrders() {
        return new FeatureOrder[] { new AfterFeature("*"), new AfterFeature("lazyCommitContent"),
                new AfterFeature("lazyCommitHeaders"), new AfterFeature("setLocaleAndCharset"),
                new AfterFeature("parseRequest") };
    }
}
