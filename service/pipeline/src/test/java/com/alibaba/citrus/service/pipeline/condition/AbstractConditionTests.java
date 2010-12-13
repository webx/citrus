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
package com.alibaba.citrus.service.pipeline.condition;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static org.easymock.classextension.EasyMock.*;

import org.junit.Before;
import org.junit.BeforeClass;

import com.alibaba.citrus.service.pipeline.AbstractPipelineTests;
import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.PipelineContext;

public abstract class AbstractConditionTests<C extends Condition> extends AbstractPipelineTests {
    protected Class<C> conditionClass;
    protected C condition;
    protected PipelineContext pipelineContext;

    @BeforeClass
    public static void initFactory() throws Exception {
        createFactory("services-conditions.xml");
    }

    @Before
    @SuppressWarnings("unchecked")
    public final void init() {
        conditionClass = (Class<C>) resolveParameter(getClass(), AbstractConditionTests.class, 0).getRawType();
        condition = newInstance();
        pipelineContext = createMock(PipelineContext.class);
    }

    protected final C newInstance() {
        try {
            return conditionClass.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
