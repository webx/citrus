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
package com.alibaba.citrus.service.pull.support;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.pull.AbstractPullServiceConfigTests;
import com.alibaba.citrus.service.pull.PullService;

public class BeanToolTests extends AbstractPullServiceConfigTests {
    private static ApplicationContext staticFactory;

    @BeforeClass
    public static void initStaticFactory2() {
        staticFactory = createContext("pull/services-pull-tool.xml");
    }

    @Before
    public final void initFactory2() {
        factory = staticFactory;
        pullService = (PullService) factory.getBean("pullService");
    }

    @Test
    public void tool_singleton_noAutowire() throws Exception {
        assertToolBean("inner1", false, true);
    }

    @Test
    public void tool_singleton_autowire() throws Exception {
        assertToolBean("inner2", true, true);
    }

    @Test
    public void tool_non_singleton_noAutowire() throws Exception {
        assertToolBean("inner3", false, false);
    }

    @Test
    public void tool_non_singleton_autowire() throws Exception {
        assertToolBean("inner4", true, false);
    }

    private void assertToolBean(String name, boolean withRequest, boolean same) throws Exception {
        // request 1
        prepareWebEnvironment(null);

        InnerBean bean1 = (InnerBean) pullService.getContext().pull(name);

        assertNotNull(bean1);
        assertTrue(bean1.isInitialized());
        assertEquals(withRequest, bean1.getRequest() != null);
        assertSame(bean1, pullService.getContext().pull(name));

        // request 2
        prepareWebEnvironment(null);

        InnerBean bean2 = (InnerBean) pullService.getContext().pull(name);

        assertNotNull(bean2);
        assertTrue(bean2.isInitialized());
        assertEquals(withRequest, bean2.getRequest() != null);
        assertSame(bean2, pullService.getContext().pull(name));

        assertEquals(same, bean1 == bean2);
    }
}
