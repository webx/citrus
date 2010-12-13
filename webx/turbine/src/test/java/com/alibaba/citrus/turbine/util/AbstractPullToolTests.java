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
package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;

import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.turbine.AbstractWebxTests;
import com.alibaba.citrus.webx.config.WebxConfiguration;

public abstract class AbstractPullToolTests<T> extends AbstractWebxTests {
    protected T tool;

    @BeforeClass
    public static void initWebx() throws Exception {
        prepareServlet();

        WebxConfiguration webxConfiguration = (WebxConfiguration) factory.getBean("webxConfiguration");

        assertFalse(webxConfiguration.isProductionMode());
    }

    @Before
    public void initTool() throws Exception {
        tool = getTool();
    }

    protected final T getTool() throws Exception {
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        PullService pull = (PullService) factory.getBean("pullService");
        T tool = getToolType().cast(pull.getTools().get(toolName()));

        assertNotNull(tool);

        return tool;
    }

    protected abstract String toolName();

    @SuppressWarnings("unchecked")
    protected final Class<T> getToolType() {
        return (Class<T>) resolveParameter(getClass(), AbstractPullToolTests.class, 0).getRawType();
    }
}
