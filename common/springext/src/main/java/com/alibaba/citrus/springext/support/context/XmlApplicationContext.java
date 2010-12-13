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
package com.alibaba.citrus.springext.support.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 一个简易的application context，用来从一个resource中启动spring。如需要更复杂的需求，请直接从
 * {@link AbstractXmlApplicationContext}派生。
 * 
 * @author Michael Zhou
 * @see AbstractXmlApplicationContext
 */
public class XmlApplicationContext extends AbstractXmlApplicationContext {
    private Resource configResource;

    public XmlApplicationContext() {
    }

    public XmlApplicationContext(Resource resource) throws BeansException {
        this(resource, null);
    }

    public XmlApplicationContext(Resource resource, ApplicationContext parentContext) throws BeansException {
        super(parentContext);
        this.configResource = resource;
        refresh();
    }

    public void setConfigResource(Resource configResource) {
        this.configResource = configResource;
    }

    @Override
    protected Resource[] getConfigResources() {
        if (configResource == null) {
            return null;
        } else {
            return new Resource[] { configResource };
        }
    }
}
