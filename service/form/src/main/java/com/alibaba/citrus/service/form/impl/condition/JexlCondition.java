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
package com.alibaba.citrus.service.form.impl.condition;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.expr.Expression;
import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.ExpressionFactory;
import com.alibaba.citrus.expr.ExpressionParseException;
import com.alibaba.citrus.expr.jexl.JexlExpressionFactory;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.Validator.Context;
import com.alibaba.citrus.service.form.support.AbstractCondition;
import com.alibaba.citrus.service.form.support.AbstractConditionDefinitionParser;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 用jexl表达式来计算条件。
 * <p>
 * JEXL语法参见：<a href="http://commons.apache.org/jexl/reference/syntax.html">JEXL
 * Reference</a>
 * </p>
 * 
 * @author Michael Zhou
 */
public class JexlCondition extends AbstractCondition {
    protected static final ExpressionFactory EXPRESSION_FACTORY = new JexlExpressionFactory();
    private String conditionString;
    private Expression condition;

    public JexlCondition(String condition) {
        this.conditionString = trimToNull(condition);
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(conditionString, "missing condition expression");

        try {
            condition = EXPRESSION_FACTORY.createExpression(conditionString);
        } catch (ExpressionParseException e) {
            throw new IllegalArgumentException("Invalid if condition: \""
                    + StringEscapeUtil.escapeJava(conditionString) + "\"", e);
        }
    }

    public boolean isSatisfied(final Context context) {
        TypeConverter converter = context.getField().getGroup().getForm().getTypeConverter();
        ExpressionContext expressionContext = new MessageContext() {
            @Override
            protected Object internalGet(String key) {
                return null;
            }

            @Override
            public ExpressionContext getParentContext() {
                return context.getMessageContext();
            }

            @Override
            protected void buildToString(ToStringBuilder sb) {
                sb.append("JexlConditionContext");
            }

            @Override
            protected void buildToString(MapBuilder mb) {
            }
        };

        return (Boolean) converter.convertIfNecessary(condition.evaluate(expressionContext), Boolean.class);
    }

    public static class DefinitionParser extends AbstractConditionDefinitionParser<JexlCondition> {
        @Override
        protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            builder.addConstructorArgValue(element.getTextContent());
        }
    }
}
