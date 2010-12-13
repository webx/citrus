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
package com.alibaba.citrus.service.dataresolver;

import static com.alibaba.citrus.test.TestEnvStatic.*;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.dataresolver.impl.DataResolverServiceImpl;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.test.TestEnvStatic;

public abstract class AbstractDataResolverTests {
    protected static ApplicationContext factory;
    protected DataResolverService resolverServices;

    static {
        TestEnvStatic.init();
    }

    protected static ApplicationContext createFactory(String location) {
        return new XmlApplicationContext(new FileSystemResource(new File(srcdir, location)));
    }

    protected final DataResolverServiceImpl getResolvers(String beanName) {
        return (DataResolverServiceImpl) factory.getBean(beanName);
    }
}
