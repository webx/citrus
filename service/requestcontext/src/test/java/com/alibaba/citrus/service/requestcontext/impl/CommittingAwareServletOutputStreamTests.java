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

package com.alibaba.citrus.service.requestcontext.impl;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import javax.servlet.ServletOutputStream;

import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import org.junit.Before;
import org.junit.Test;

public class CommittingAwareServletOutputStreamTests
        extends AbstractCommittingAwareTests<ServletOutputStream, CommittingAwareServletOutputStream> {
    /** 测试所有方法调用，所有方法均会调用delegate，其中部分方法会调用commitHeaders。 */
    @Prototypes
    public static Collection<CommittingAwareServletOutputStreamTests> data() {
        TestData<CommittingAwareServletOutputStreamTests> data = TestData.getInstance(CommittingAwareServletOutputStreamTests.class);
        CommittingAwareServletOutputStreamTests prototype;

        for (Method method : ServletOutputStream.class.getMethods()) {
            String name = method.getName();

            if (!Modifier.isFinal(method.getModifiers()) && !name.equals("equals") && !name.equals("hashCode") && !name.equals("toString")) {
                prototype = data.newPrototype();
                prototype.method = method;
                prototype.doCommitHeaders = name.startsWith("print") || name.startsWith("write") || name.startsWith("flush");
            }
        }

        return data;
    }

    @Before
    public void init() throws Exception {
        testObject = new CommittingAwareServletOutputStream(committer, originalObject);
    }

    @Test
    public void _toString() {
        assertEquals(originalObject.toString(), testObject.toString());
    }
}
