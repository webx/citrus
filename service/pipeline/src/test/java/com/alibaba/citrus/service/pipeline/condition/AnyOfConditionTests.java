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

import com.alibaba.citrus.service.pipeline.impl.condition.AnyOf;

public class AnyOfConditionTests extends AbstractCompositeConditionTests<AnyOf> {
    @Test
    public void logic() {
        setConditions();
        assertFalse(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(true), new MyCondition(true), new MyCondition(true));
        assertTrue(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(false), new MyCondition(false), new MyCondition(true));
        assertTrue(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(false), new MyCondition(false), new MyCondition(false));
        assertFalse(condition.isSatisfied(pipelineContext));
    }

    @Test
    public void config() {
        condition = (AnyOf) factory.getBean("anyOf1");
        assertFalse(condition.isSatisfied(pipelineContext));

        condition = (AnyOf) factory.getBean("anyOf2");
        assertTrue(condition.isSatisfied(pipelineContext));

        condition = (AnyOf) factory.getBean("anyOf3");
        assertTrue(condition.isSatisfied(pipelineContext));

        condition = (AnyOf) factory.getBean("anyOf4");
        assertFalse(condition.isSatisfied(pipelineContext));
    }

    @Override
    protected String desc() {
        return "AnyOf";
    }
}
