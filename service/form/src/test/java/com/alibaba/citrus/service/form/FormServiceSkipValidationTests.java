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

package com.alibaba.citrus.service.form;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import com.alibaba.citrus.service.form.configuration.FormConfig.FieldKeyFormat;
import com.alibaba.citrus.service.form.impl.FormServiceImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;

@RunWith(Parameterized.class)
public class FormServiceSkipValidationTests extends AbstractFormServiceTests {
    private final boolean            skipValidation;
    private       ApplicationContext factory;
    private       FormConfigImpl     config;

    public FormServiceSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = createContext("services-skip-validation.xml", true);
        formService = (FormServiceImpl) factory.getBean("formService");
        config = (FormConfigImpl) formService.getFormConfig();
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void formConfig() {
        assertEquals(true, config.isConverterQuiet());
        assertEquals(true, config.isPostOnlyByDefault());
        assertSame(FieldKeyFormat.compressed, config.getFieldKeyFormat());
        assertEquals("form.", config.getMessageCodePrefix());
    }

    @Test
    public void groupConfig() {
        assertEquals(true, config.getGroupConfig("test").isPostOnly());
        assertEquals(true, config.getGroupConfig("test").isTrimmingByDefault());
    }
}
