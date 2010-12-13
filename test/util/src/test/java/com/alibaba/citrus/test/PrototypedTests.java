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

import static com.alibaba.citrus.test.runner.Prototyped.TestData.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * 原型化单元测试示例。
 * <p>
 * 实现<code>Cloneable</code>接口。
 * </p>
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class PrototypedTests implements Cloneable {
    private final static Logger log = LoggerFactory.getLogger(PrototypedTests.class);
    private String p1;
    private int[] p2;
    private Object p3;

    static {
        TestEnvStatic.init();
    }

    /**
     * 这是一个特殊的方法，会根据参数自动设置测试的名称。
     */
    @TestName
    public String testName() {
        return p1;
    }

    /**
     * 取得测试原型的集合。
     */
    @Prototypes
    public static Collection<PrototypedTests> data() {
        TestData<PrototypedTests> data = getInstance(PrototypedTests.class);
        PrototypedTests prototype;

        // 数据1
        prototype = data.newPrototype();
        prototype.p1 = "data1";
        prototype.p2 = new int[] { 1, 2, 3 };

        // 数据2
        prototype = data.newPrototype();
        prototype.p1 = "data2";
        prototype.p3 = new Object();

        return data;
    }

    private String expectedName;
    private String initName;
    private String testName;
    private String disposeName;

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
        log.debug("{}, {}, {}", new Object[] { p1, p2, p3 });

        testName = TestUtil.getTestName();
        expectedName = "test1";

        log.info("test1");
    }

    @Test
    public void test2() {
        log.debug("{}, {}, {}", new Object[] { p1, p2, p3 });

        testName = TestUtil.getTestName();
        expectedName = "test2";

        log.info("test2");
    }
}
