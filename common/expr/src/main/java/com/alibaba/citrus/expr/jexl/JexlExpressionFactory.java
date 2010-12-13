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
package com.alibaba.citrus.expr.jexl;

import com.alibaba.citrus.expr.Expression;
import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.ExpressionFactory;
import com.alibaba.citrus.expr.ExpressionParseException;
import com.alibaba.citrus.expr.support.ExpressionSupport;

/**
 * 创建<code>JexlExpression</code>的工厂。
 * 
 * @author Michael Zhou
 */
public class JexlExpressionFactory implements ExpressionFactory {
    /** 是否支持context变量，就是用小数点分隔的变量名。 */
    private boolean supportContextVariables = true;

    /**
     * 是否支持context变量，就是用小数点分隔的变量名。
     * 
     * @return 如果支持，则返回<code>true</code>
     */
    public boolean isSupportContextVariables() {
        return supportContextVariables;
    }

    /**
     * 设置支持context变量。
     * 
     * @param supportContextVariables 是否支持context变量
     */
    public void setSupportContextVariables(boolean supportContextVariables) {
        this.supportContextVariables = supportContextVariables;
    }

    /**
     * 创建表达式。
     * 
     * @param expr 表达式字符串
     * @return 表达式
     */
    public Expression createExpression(final String expr) throws ExpressionParseException {
        final Expression jexlExpression;

        try {
            jexlExpression = new JexlExpression(org.apache.commons.jexl.ExpressionFactory.createExpression(expr));
        } catch (Exception e) {
            throw new ExpressionParseException(e);
        }

        if (isSupportContextVariables() && isValidContextVariableName(expr)) {
            return new ExpressionSupport() {
                /**
                 * 取得表达式字符串表示。
                 * 
                 * @return 表达式字符串表示
                 */
                public String getExpressionText() {
                    return expr;
                }

                /**
                 * 在指定的上下文中计算表达式。
                 * 
                 * @param context <code>ExpressionContext</code>上下文
                 * @return 表达式的计算结果
                 */
                public Object evaluate(ExpressionContext context) {
                    // 首先执行jexl表达式
                    Object value = jexlExpression.evaluate(context);

                    // 如果jexl表达式结果为null，则从context中直接取值
                    if (value == null) {
                        value = context.get(expr);
                    }

                    return value;
                }
            };
        }

        return jexlExpression;
    }

    /**
     * 判断是否为context变量。
     * 
     * @return 如果是，则返回<code>true</code>
     */
    protected boolean isValidContextVariableName(String varName) {
        for (int i = 0; i < varName.length(); i++) {
            char ch = varName.charAt(i);

            if (Character.isWhitespace(ch) || ch == '[') {
                return false;
            }
        }

        return true;
    }
}
