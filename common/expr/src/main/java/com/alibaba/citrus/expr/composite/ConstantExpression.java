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
package com.alibaba.citrus.expr.composite;

import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.support.ExpressionSupport;

/**
 * 代表一个常量表达式，该表达式的值不依赖于<code>ExpressionContext</code>。
 * 
 * @author Michael Zhou
 */
public class ConstantExpression extends ExpressionSupport {
    private Object value;

    /**
     * 创建一个常量表达式。
     */
    public ConstantExpression() {
    }

    /**
     * 创建一个常量表达式。
     * 
     * @param value 常量值
     */
    public ConstantExpression(Object value) {
        this.value = value;
    }

    /**
     * 取得常量值。
     * 
     * @return 常量值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置常量值。
     * 
     * @param value 常量值
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * 取得表达式字符串表示。
     * 
     * @return 表达式字符串表示
     */
    public String getExpressionText() {
        return String.valueOf(value);
    }

    /**
     * 在指定的上下文中计算表达式。
     * 
     * @param context <code>ExpressionContext</code>上下文
     * @return 表达式的计算结果
     */
    public Object evaluate(ExpressionContext context) {
        return value;
    }
}
