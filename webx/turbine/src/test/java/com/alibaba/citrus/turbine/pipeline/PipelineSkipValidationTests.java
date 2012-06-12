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

package com.alibaba.citrus.turbine.pipeline;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.turbine.AbstractWebxTests;
import com.alibaba.citrus.turbine.pipeline.valve.AnalyzeURLValve;
import com.alibaba.citrus.turbine.pipeline.valve.ExportControlValve;
import com.alibaba.citrus.turbine.pipeline.valve.GetResourceValve;
import com.alibaba.citrus.turbine.pipeline.valve.HandleExceptionValve;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PipelineSkipValidationTests extends AbstractWebxTests {
    private final boolean      skipValidation;
    private       PipelineImpl pipeline;

    public PipelineSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() throws Exception {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        prepareServlet("app3");
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        pipeline = (PipelineImpl) factory.getBean("pipeline");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void analyzeURL() {
        AnalyzeURLValve valve = (AnalyzeURLValve) pipeline.getValves()[0];
        assertEquals("/index", valve.getHomepage());
    }

    @Test
    public void exportControl() {
        ExportControlValve valve = (ExportControlValve) pipeline.getValves()[1];
        assertEquals("control", getFieldValue(valve, "controlToolName", null));
        assertEquals("subst", getFieldValue(valve, "substName", null));
    }

    @Test
    public void getResource() {
        GetResourceValve valve = (GetResourceValve) pipeline.getValves()[2];
        assertEquals("subst", getFieldValue(valve, "substName", null));
    }

    @Test
    public void handleException() {
        HandleExceptionValve valve = (HandleExceptionValve) pipeline.getValves()[3];
        assertEquals("error", getFieldValue(valve, "helperName", null));
    }
}
