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
package com.alibaba.citrus.turbine;

import com.alibaba.citrus.service.uribroker.uri.URIBroker;

/**
 * 用来方便进行内部或外部重定向的接口。
 * 
 * @author Michael Zhou
 */
public interface Navigator {
    /**
     * 进行内部重定向，指定一个target名称。
     */
    Parameters forwardTo(String target);

    /**
     * 进行外部重定向，指定一个uri broker的名称。
     */
    RedirectParameters redirectTo(String uriName);

    /**
     * 进行外部重定向，指定一个完整的URL location。
     */
    void redirectToLocation(String location);

    /**
     * 重定向的参数。
     */
    interface Parameters {
        Parameters withParameter(String name, String... values);
    }

    /**
     * 外部重定向的参数。
     */
    interface RedirectParameters extends Parameters {
        RedirectParameters withTarget(String target);

        URIBroker uri();
    }
}
