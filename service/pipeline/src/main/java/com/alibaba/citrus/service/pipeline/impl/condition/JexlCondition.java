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
package com.alibaba.citrus.service.pipeline.impl.condition;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.expr.Expression;
import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.ExpressionFactory;
import com.alibaba.citrus.expr.ExpressionParseException;
import com.alibaba.citrus.expr.jexl.JexlExpressionFactory;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.PipelineStates;
import com.alibaba.citrus.service.pipeline.support.AbstractCondition;
import com.alibaba.citrus.service.pipeline.support.AbstractConditionDefinitionParser;
import com.alibaba.citrus.util.StringEscapeUtil;

/**
 * 用jexl表达式来计算的condition。
 * <p>
 * JEXL语法参见：<a href="http://commons.apache.org/jexl/reference/syntax.html">JEXL
 * Reference</a>
 * </p>
 * 
 * @author Michael Zhou
 */
public class JexlCondition extends AbstractCondition {
    protected static final ExpressionFactory EXPRESSION_FACTORY = new JexlExpressionFactory();
    private String expressionString;
    private Expression expression;

    public JexlCondition() {

    }

    public JexlCondition(String expr) {
        setExpression(expr);
    }

    public String getExpression() {
        return expressionString;
    }

    public void setExpression(String expr) {
        expr = assertNotNull(trimToNull(expr), "missing condition expression");

        try {
            this.expression = EXPRESSION_FACTORY.createExpression(expr);
        } catch (ExpressionParseException e) {
            throw new IllegalArgumentException("Invalid expression: \"" + getEscapedExpression(expr) + "\"", e);
        }

        this.expressionString = expr;
    }

    @Override
    protected void init() {
        assertNotNull(expression, "no expression");
    }

    public boolean isSatisfied(PipelineStates pipelineStates) {
        assertNotNull(expression, "no expression");

        ExpressionContext expressionContext = new PipelineExpressionContext(pipelineStates);
        Object value = expression.evaluate(expressionContext);

        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            try {
                return (Boolean) new SimpleTypeConverter().convertIfNecessary(value, Boolean.class);
            } catch (TypeMismatchException e) {
                throw new PipelineException(
                        "Failed to evaluating expression for JexlCondition into a boolean value: \""
                                + getEscapedExpression(expressionString) + "\"", e);
            }
        }
    }

    @Override
    public String toString() {
        return "JexlCondition[" + getEscapedExpression(expressionString) + "]";
    }

    private String getEscapedExpression(String expr) {
        return StringEscapeUtil.escapeJava(expr);
    }

    private static class PipelineExpressionContext implements ExpressionContext {
        private final PipelineStates pipelineStates;

        public PipelineExpressionContext(PipelineStates pipelineStates) {
            this.pipelineStates = pipelineStates;
        }

        public Object get(String key) {
            return pipelineStates.getAttribute(key);
        }

        public void put(String key, Object value) {
            pipelineStates.setAttribute(key, value);
        }
    }

    public static class DefinitionParser extends AbstractConditionDefinitionParser<JexlCondition> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            super.doParse(element, parserContext, builder);
            builder.addPropertyValue("expression", element.getAttribute("expr"));
        }
    }
}
