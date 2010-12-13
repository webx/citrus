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
package com.alibaba.citrus.expr;

/**
 * 代表一个表达式。
 * 
 * @author Michael Zhou
 */
public interface Expression {
    /**
     * 取得表达式字符串表示。
     * 
     * @return 表达式字符串表示
     */
    String getExpressionText();

    /**
     * 在指定的上下文中计算表达式。
     * 
     * @param context <code>ExpressionContext</code>上下文
     * @return 表达式的计算结果
     */
    Object evaluate(ExpressionContext context);
}
