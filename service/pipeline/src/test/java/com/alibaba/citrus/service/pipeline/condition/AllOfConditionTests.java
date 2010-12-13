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

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.impl.condition.AllOf;

public class AllOfConditionTests extends AbstractCompositeConditionTests<AllOf> {
    @Test
    public void logic() {
        setConditions();
        assertTrue(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(true), new MyCondition(true), new MyCondition(true));
        assertTrue(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(true), new MyCondition(false), new MyCondition(true));
        assertFalse(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(false), new MyCondition(false), new MyCondition(false));
        assertFalse(condition.isSatisfied(pipelineContext));
    }

    @Test
    public void config() {
        condition = (AllOf) factory.getBean("allOf1");
        assertTrue(condition.isSatisfied(pipelineContext));

        condition = (AllOf) factory.getBean("allOf2");
        assertTrue(condition.isSatisfied(pipelineContext));

        condition = (AllOf) factory.getBean("allOf3");
        assertFalse(condition.isSatisfied(pipelineContext));

        condition = (AllOf) factory.getBean("allOf4");
        assertFalse(condition.isSatisfied(pipelineContext));
    }

    @Override
    protected String desc() {
        return "AllOf";
    }
}
