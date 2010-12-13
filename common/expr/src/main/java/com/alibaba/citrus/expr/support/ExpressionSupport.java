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
package com.alibaba.citrus.expr.support;

import com.alibaba.citrus.expr.Expression;
import com.alibaba.citrus.util.ClassUtil;

/**
 * 抽象的<code>Expression</code>实现。
 * 
 * @author Michael Zhou
 */
public abstract class ExpressionSupport implements Expression {
    /**
     * 取得字符串表示。
     * 
     * @return 表达式的字符串表示
     */
    @Override
    public String toString() {
        return ClassUtil.getSimpleClassName(getClass()) + "[" + getExpressionText() + "]";
    }
}
