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
package com.alibaba.citrus.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.test.runner.TestNameAware;

@RunWith(TestNameAware.class)
public class TestNameAwareTests {
    private final static Logger log = LoggerFactory.getLogger(TestNameAwareTests.class);
    private String expectedName;
    private String initName;
    private String testName;
    private String disposeName;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() {
        initName = TestUtil.getTestName();
        log.info("init");
    }

    @After
    public void dispose() {
        disposeName = TestUtil.getTestName();

        assertNotNull(expectedName);
        assertEquals(expectedName, initName);
        assertEquals(expectedName, testName);
        assertEquals(expectedName, disposeName);

        log.info("dispose");
    }

    @Test
    public void test1() {
        testName = TestUtil.getTestName();
        expectedName = "test1";

        log.info("test1");
    }

    @Test
    public void test2() {
        testName = TestUtil.getTestName();
        expectedName = "test2";

        log.info("test2");
    }
}
