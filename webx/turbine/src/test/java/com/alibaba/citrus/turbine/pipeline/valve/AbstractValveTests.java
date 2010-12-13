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
package com.alibaba.citrus.turbine.pipeline.valve;

import org.junit.Before;
import org.junit.BeforeClass;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.turbine.AbstractWebxTests;

public abstract class AbstractValveTests extends AbstractWebxTests {
    protected PipelineImpl pipeline;

    @BeforeClass
    public static void initWebx() throws Exception {
        prepareServlet();
    }

    @Before
    public void init() throws Exception {
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();
    }
}
