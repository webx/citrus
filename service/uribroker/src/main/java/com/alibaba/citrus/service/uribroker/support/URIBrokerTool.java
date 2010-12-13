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
package com.alibaba.citrus.service.uribroker.support;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.service.pull.ToolSetFactory;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 取得所有<code>URIBroker</code>的pull tool。
 * 
 * @author Michael Zhou
 */
public class URIBrokerTool implements ToolFactory, ToolSetFactory, InitializingBean {
    private URIBrokerService brokers;

    @Autowired
    public void setBrokers(URIBrokerService brokers) {
        this.brokers = brokers;
    }

    /**
     * 初始化pull tool。
     */
    public void afterPropertiesSet() throws Exception {
        assertNotNull(brokers, "no URIBrokerService");
    }

    /**
     * 每个请求都会创建新的实例。
     */
    public boolean isSingleton() {
        return false;
    }

    /**
     * 取得所有exposed URI broker的名称。
     */
    public Iterable<String> getToolNames() {
        return brokers.getExposedNames();
    }

    /**
     * 取得一个对象，可以从中取得所有的brokers。
     */
    public Object createTool() throws Exception {
        return new Helper();
    }

    /**
     * 取得指定名称的broker。
     */
    public Object createTool(String name) throws Exception {
        return brokers.getURIBroker(name);
    }

    /**
     * 这是一个辅助类，每个请求都会创建一次。
     */
    public class Helper {
        private Map<String, URIBroker> cache = createHashMap();

        /**
         * 便于模板使用的方法：取得指定名称的broker。
         */
        public URIBroker get(String name) {
            URIBroker broker = cache.get(name);

            if (broker == null) {
                broker = brokers.getURIBroker(name);

                if (broker == null) {
                    return null;
                }

                cache.put(name, broker);
            }

            return broker;
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<URIBrokerTool> {
    }
}
