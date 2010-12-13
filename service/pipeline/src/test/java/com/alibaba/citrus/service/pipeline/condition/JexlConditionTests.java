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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.expr.ExpressionParseException;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.impl.condition.JexlCondition;

public class JexlConditionTests extends AbstractConditionTests<JexlCondition> {
    @Test
    public void create() {
        try {
            new JexlCondition(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing condition expression"));
        }

        condition = new JexlCondition("1==2");
        assertEquals("1==2", condition.getExpression());
    }

    @Test
    public void init_() throws Exception {
        try {
            condition.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no expression"));
        }

        condition.setExpression("hello");
        condition.afterPropertiesSet();
    }

    @Test
    public void setExpression() {
        assertNull(condition.getExpression());

        // set empty
        try {
            condition.setExpression(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing condition expression"));
        }

        try {
            condition.setExpression("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing condition expression"));
        }

        assertNull(condition.getExpression());

        // set value
        condition.setExpression(" hello ");
        assertEquals("hello", condition.getExpression());

        // set illegal
        try {
            condition.setExpression(" a=  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(ExpressionParseException.class, "Invalid expression: \"a=\""));
        }

        assertEquals("hello", condition.getExpression()); // 保持不变
    }

    @Test
    public void evaluate() {
        reset(pipelineContext);
        expect(pipelineContext.getAttribute("count")).andReturn("1").anyTimes();
        expect(pipelineContext.getAttribute("nullValue")).andReturn(null).anyTimes();
        expect(pipelineContext.getAttribute("strValue")).andReturn("true").anyTimes();
        expect(pipelineContext.getAttribute("strValue2")).andReturn("hello").anyTimes();
        expect(pipelineContext.getAttribute("hello")).andReturn(null).anyTimes();
        pipelineContext.setAttribute("hello", "true");
        replay(pipelineContext);

        // not inited
        try {
            condition.isSatisfied(pipelineContext);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no expression"));
        }

        // boolean expression
        condition = newInstance();
        condition.setExpression("count > 10");
        assertFalse(condition.isSatisfied(pipelineContext));

        condition.setExpression("count < 10");
        assertTrue(condition.isSatisfied(pipelineContext));

        // null value
        condition.setExpression("nullValue");
        assertFalse(condition.isSatisfied(pipelineContext));

        // string value - convert to boolean - success
        condition.setExpression("strValue");
        assertTrue(condition.isSatisfied(pipelineContext));

        // string value - convert to boolean - failure
        condition.setExpression("strValue2");

        try {
            condition.isSatisfied(pipelineContext);
            fail();
        } catch (PipelineException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class,
                            "Failed to evaluating expression for JexlCondition into a boolean value: \"strValue2\"",
                            "Invalid boolean value [hello]"));
        }

        // set attribute
        condition.setExpression(" hello='true' ");
        assertTrue(condition.isSatisfied(pipelineContext));

        verify(pipelineContext);
    }

    @Test
    public void toString_() {
        // no expression
        assertEquals("JexlCondition[null]", condition.toString());

        // expression
        condition.setExpression(" \"hello\" ");
        assertEquals("JexlCondition[\\\"hello\\\"]", condition.toString());
    }

    @Test
    public void config() {
        condition = (JexlCondition) factory.getBean("jexl-true");
        assertTrue(condition.isSatisfied(pipelineContext));

        condition = (JexlCondition) factory.getBean("jexl-false");
        assertFalse(condition.isSatisfied(pipelineContext));
    }
}
