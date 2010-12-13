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
package com.alibaba.citrus.service.requestcontext;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

/**
 * 代表一个<code>RequestContext</code>相关的信息。
 * 
 * @author Michael Zhou
 */
public interface RequestContextInfo<R extends RequestContext> {
    /**
     * 取得当前factory将生成的request context接口。
     */
    Class<R> getRequestContextInterface();

    /**
     * 取得用于生成proxy类的接口。
     */
    Class<? extends R> getRequestContextProxyInterface();

    /**
     * 取得当前factory生成的request context所提供的features。
     * 所谓features，是一些字符串。RequestContextChaining服务利用这些feature及依赖关系，将request
     * context排序。
     */
    String[] getFeatures();

    /**
     * 指出当前request context必须排在哪些features之前或之后。
     */
    FeatureOrder[] featureOrders();

    /**
     * 代表request context feature的顺序。
     */
    abstract class FeatureOrder {
        public final String feature;

        public FeatureOrder(String feature) {
            this.feature = assertNotNull(trimToNull(feature), "feature");
        }
    }

    /**
     * 表示当前request context应该排在提供指定feature的request context之前。
     */
    class BeforeFeature extends FeatureOrder {
        public BeforeFeature(String feature) {
            super(feature);
        }

        @Override
        public String toString() {
            return "Before " + feature;
        }
    }

    /**
     * 表示当前request context应该排在提供指定feature的request context之后。
     */
    class AfterFeature extends FeatureOrder {
        public AfterFeature(String feature) {
            super(feature);
        }

        @Override
        public String toString() {
            return "After " + feature;
        }
    }

    /**
     * 表示当前request context前面必须有提供指定feature的request context存在。
     */
    class RequiresFeature extends AfterFeature {
        public RequiresFeature(String feature) {
            super(feature);
        }

        @Override
        public String toString() {
            return "Requires " + feature;
        }
    }
}
