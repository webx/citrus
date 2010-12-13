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
package com.alibaba.citrus.service.uribroker;

import java.io.Writer;
import java.util.List;

import com.alibaba.citrus.service.uribroker.uri.URIBroker;

/**
 * URI Broker的service 接口定义。
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public interface URIBrokerService {
    /**
     * 取得所有URI broker名称.
     */
    List<String> getNames();

    /**
     * 取得所有被导出的URI broker名称.
     */
    List<String> getExposedNames();

    /**
     * 取得指定名称的URI broker.
     */
    URIBroker getURIBroker(String name);

    /**
     * 列出所有的URI brokers.
     */
    String dump();

    /**
     * 列出所有的URI brokers.
     */
    void dump(Writer writer);
}
