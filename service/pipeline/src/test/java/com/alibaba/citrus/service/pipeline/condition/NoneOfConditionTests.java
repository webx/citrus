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

import com.alibaba.citrus.service.pipeline.impl.condition.NoneOf;

public class NoneOfConditionTests extends AbstractCompositeConditionTests<NoneOf> {
    @Test
    public void logic() {
        setConditions();
        assertTrue(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(true), new MyCondition(true), new MyCondition(true));
        assertFalse(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(false), new MyCondition(false), new MyCondition(true));
        assertFalse(condition.isSatisfied(pipelineContext));

        setConditions(new MyCondition(false), new MyCondition(false), new MyCondition(false));
        assertTrue(condition.isSatisfied(pipelineContext));
    }

    @Test
    public void config() {
        condition = (NoneOf) factory.getBean("noneOf1");
        assertTrue(condition.isSatisfied(pipelineContext));

        condition = (NoneOf) factory.getBean("noneOf2");
        assertFalse(condition.isSatisfied(pipelineContext));

        condition = (NoneOf) factory.getBean("noneOf3");
        assertFalse(condition.isSatisfied(pipelineContext));

        condition = (NoneOf) factory.getBean("noneOf4");
        assertTrue(condition.isSatisfied(pipelineContext));
    }

    @Override
    protected String desc() {
        return "NoneOf";
    }
}
