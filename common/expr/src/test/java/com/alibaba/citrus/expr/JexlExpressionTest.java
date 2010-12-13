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

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.expr.jexl.JexlExpressionFactory;
import com.alibaba.citrus.expr.support.MappedExpressionContext;

/**
 * ≤‚ ‘<code>JexlExpression</code>°£
 * 
 * @author Michael Zhou
 */
public class JexlExpressionTest {
    private ExpressionFactory factory;
    private ExpressionContext context;
    private Date now;

    @Before
    public void init() {
        factory = new JexlExpressionFactory();
        context = new MappedExpressionContext();
        now = new Date();
        context.put("now", now);
        context.put("xxx.yyy.zzz", "hello, world");
    }

    @Test
    public void jexlExpression() throws Exception {
        assertSame(now, evaluate("now"));
        assertEquals(new Long(now.getTime()), evaluate("now.time"));
        assertEquals(null, evaluate("null"));
        assertEquals(new Integer(123), evaluate("123"));
        assertEquals("abc", evaluate("'abc'"));
        assertEquals(Boolean.TRUE, evaluate("2 > 1"));
    }

    @Test
    public void jexlExpressionContextVariables() throws Exception {
        assertEquals("hello, world", evaluate("xxx.yyy.zzz"));
    }

    private Object evaluate(String expr) throws ExpressionParseException {
        return factory.createExpression(expr).evaluate(context);
    }
}
