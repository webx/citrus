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

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import org.junit.Before;
import org.junit.Test;

public class CommittingAwarePrintWriterTests
        extends AbstractCommittingAwareTests<PrintWriter, CommittingAwarePrintWriter> {
    @Prototypes
    public static Collection<CommittingAwarePrintWriterTests> data() {
        TestData<CommittingAwarePrintWriterTests> data = TestData.getInstance(CommittingAwarePrintWriterTests.class);
        CommittingAwarePrintWriterTests prototype;

        for (Method method : PrintWriter.class.getMethods()) {
            String name = method.getName();

            if (!Modifier.isFinal(method.getModifiers()) && !name.equals("equals") && !name.equals("hashCode") && !name.equals("toString")) {
                prototype = data.newPrototype();
                prototype.method = method;
                prototype.doCommitHeaders = name.startsWith("print") || name.startsWith("write") || name.startsWith("flush")
                                            || name.startsWith("format") || name.startsWith("append") || name.startsWith("checkError");
            }
        }

        return data;
    }

    @Before
    public void init() throws Exception {
        testObject = new CommittingAwarePrintWriter(committer, originalObject);
    }

    @Test
    public void _toString() {
        assertEquals(originalObject.toString(), testObject.toString());
    }
}
