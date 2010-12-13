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
package com.alibaba.citrus.test.runner;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.alibaba.citrus.test.TestUtil;

/**
 * 支持<code>TestUtil.getTestName()</code>，以便在测试中取得当前测试的名称。
 * 
 * @author Michael Zhou
 */
public class TestNameAware extends BlockJUnit4ClassRunner {
    public TestNameAware(Class<?> aClass) throws InitializationError {
        super(aClass);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        TestUtil.setTestName(method.getName());

        try {
            super.runChild(method, notifier);
        } finally {
            TestUtil.setTestName(null);
        }
    }
}
