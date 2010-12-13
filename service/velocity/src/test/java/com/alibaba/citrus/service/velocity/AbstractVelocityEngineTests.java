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
package com.alibaba.citrus.service.velocity;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.service.velocity.VelocityEngineTests.MyProxy;
import com.alibaba.citrus.service.velocity.impl.VelocityEngineImpl;

public abstract class AbstractVelocityEngineTests {
    protected static ApplicationContext factory;
    protected TemplateService templateService;
    protected VelocityEngineImpl velocityEngine;

    protected static ApplicationContext createFactory(String configFile) {
        return createFactory(configFile, true);
    }

    protected static ApplicationContext createFactory(String configFile, boolean contextResourceSupport) {
        // 如果!contextResourceSupport，则appcontext返回的resource将被转换成非ContextResource对象，
        // 用于测试preloaded resource loader。
        if (contextResourceSupport) {
            return new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir, configFile)));
        } else {
            return new ResourceLoadingXmlApplicationContext(new FileSystemResource(new File(srcdir, configFile))) {
                @Override
                public Resource[] getResources(String locationPattern) throws IOException {
                    Resource[] resources = super.getResources(locationPattern);

                    if (!isEmptyArray(resources)) {
                        for (int i = 0; i < resources.length; i++) {
                            if (resources[i] instanceof ContextResource) {
                                resources[i] = getResourceProxy(getClassLoader(), resources[i]);
                            }
                        }
                    }

                    return resources;
                }
            };
        }
    }

    private static Resource getResourceProxy(ClassLoader loader, final Resource resource) {
        return (Resource) Proxy.newProxyInstance(loader, new Class<?>[] { Resource.class, MyProxy.class },
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getInstance".equals(method.getName()) && isEmptyArray(args)) {
                            return resource;
                        }

                        if ("equals".equals(method.getName()) && arrayLength(args) == 1 && args[0] instanceof MyProxy) {
                            args[0] = ((MyProxy) args[0]).getInstance();
                        }

                        try {
                            return method.invoke(resource, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                });
    }

    protected void getEngine(String id, ApplicationContext factory) {
        templateService = (TemplateService) factory.getBean(id);
        velocityEngine = (VelocityEngineImpl) templateService.getTemplateEngine("vm");

        assertNotNull(velocityEngine);
    }
}
