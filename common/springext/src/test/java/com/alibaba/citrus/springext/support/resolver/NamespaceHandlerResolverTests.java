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
package com.alibaba.citrus.springext.support.resolver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.context.config.ContextNamespaceHandler;

import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;
import com.alibaba.citrus.test.TestEnvStatic;

public class NamespaceHandlerResolverTests {
    private ConfigurationPointsImpl cps;
    private NamespaceHandlerResolver resolver;

    static {
        TestEnvStatic.init();
    }

    @Test
    public void test6_resolve() {
        createConfigurationPoints("TEST-INF/test6/cps");

        NamespaceHandler nh = resolver.resolve("http://www.alibaba.com/test6/cp1");

        assertSame(cps.getConfigurationPointByName("cp1"), nh);
    }

    @Test
    public void test6_resolveDefault() {
        createConfigurationPoints("TEST-INF/test6/cps");

        NamespaceHandler nh = resolver.resolve("http://www.springframework.org/schema/context");

        assertThat(nh, instanceOf(ContextNamespaceHandler.class));
    }

    private void createConfigurationPoints(String location) {
        cps = new ConfigurationPointsImpl(null, location);
        resolver = new ConfigurationPointNamespaceHandlerResolver(cps, new DefaultNamespaceHandlerResolver());
    }
}
