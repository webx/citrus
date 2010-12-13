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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.expr.ExpressionContext;
import com.alibaba.citrus.expr.support.ExpressionSupport;

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

    /**
     * 将<code>ExpressionContext</code>适配到<code>JexlContext</code>。
     */
    private static class JexlContextAdapter implements JexlContext {
        private Map<String, Object> vars;

        public JexlContextAdapter(final ExpressionContext context) {
            this.vars = new Map<String, Object>() {
                public Object get(Object key) {
                    return context.get((String) key);
                }

                public void clear() {
                }

                public boolean containsKey(Object key) {
                    return get(key) != null;
                }

                public boolean containsValue(Object value) {
                    return false;
                }

                public Set<Map.Entry<String, Object>> entrySet() {
                    return null;
                }

                public boolean isEmpty() {
                    return false;
                }

                public Set<String> keySet() {
                    return null;
                }

                public Object put(String key, Object value) {
                    Object old = context.get(key);

                    context.put(key, value);

                    return old;
                }

                public void putAll(Map<? extends String, ? extends Object> t) {
                }

                public Object remove(Object key) {
                    return null;
                }

                public int size() {
                    return -1;
                }

                public Collection<Object> values() {
                    return null;
                }
            };
        }

        @SuppressWarnings("rawtypes")
        public void setVars(Map vars) {
        }

        @SuppressWarnings("rawtypes")
        public Map getVars() {
            return this.vars;
        }
    }
}
