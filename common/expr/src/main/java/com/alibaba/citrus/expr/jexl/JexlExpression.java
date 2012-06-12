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

package com.alibaba.citrus.expr.jexl;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.support.ExpressionSupport;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代表一个jexl表达式。
 *
 * @author Michael Zhou
 */
public class JexlExpression extends ExpressionSupport {
    private static final Logger log = LoggerFactory.getLogger(JexlExpression.class);
    private Expression expression;

    /**
     * 创建一个Jexl表达式。
     *
     * @param expr jexl表达式对象
     */
    public JexlExpression(Expression expr) {
        this.expression = expr;
    }

    /**
     * 取得表达式字符串表示。
     *
     * @return 表达式字符串表示
     */
    public String getExpressionText() {
        return expression.getExpression();
    }

    /**
     * 在指定的上下文中计算表达式。
     *
     * @param context <code>ExpressionContext</code>上下文
     * @return 表达式的计算结果
     */
    public Object evaluate(ExpressionContext context) {
        try {
            JexlContext jexlContext = new JexlContextAdapter(context);

            if (log.isDebugEnabled()) {
                log.debug("Evaluating EL: " + expression.getExpression());
            }

            Object value = expression.evaluate(jexlContext);

            if (log.isDebugEnabled()) {
                log.debug("value of expression: " + value);
            }

            return value;
        } catch (Exception e) {
            log.warn("Caught exception evaluating: " + expression + ". Reason: " + e, e);
            return null;
        }
    }

    /** 将<code>ExpressionContext</code>适配到<code>JexlContext</code>。 */
    private static class JexlContextAdapter implements JexlContext {
        private ExpressionContext expressionContext;

        public JexlContextAdapter(ExpressionContext expressionContext) {
            this.expressionContext = assertNotNull(expressionContext, "expressionContext");
        }

        public Object get(String key) {
            return expressionContext.get(key);
        }

        public void set(String key, Object value) {
            expressionContext.put(key, value);
        }

        public boolean has(String key) {
            return expressionContext.get(key) != null;
        }
    }
}
