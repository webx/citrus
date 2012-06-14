/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.requestcontext.locale;

import static org.junit.Assert.*;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.meterware.servletunit.ServletRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractSetLocaleRequestContextTests
        extends AbstractRequestContextsTests<SetLocaleRequestContext> {
    protected boolean useSession;

    @Override
    protected void registerServlets(ServletRunner runner) {
        runner.registerServlet("*.json", NoopServlet.class.getName());
        runner.registerServlet("*.js", NoopServlet.class.getName());
    }

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-locale.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext();
    }

    @After
    public void dispose() {
        // 确保session没有启动
        if (!useSession) {
            assertFalse("sessionCreated", ((MyHttpRequest) request).isSessionCreated());
        }
    }
}
