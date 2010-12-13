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
package com.alibaba.citrus.util.internal;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 测试<code>NormalizableStringBuilder</code>。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractNormalizableStringBuilderTests<B extends NormalizableStringBuilder<B>> {
    protected B buf;

    @Test
    public void crlf() {
        // cr, lf, crlf全部转换成统一的lf
        buf.append("a\r");
        buf.append("b\n");
        buf.append("c\r\n");
        buf.append("d\n");
        buf.append("\r");
        buf.append("e\n");
        buf.append("\n");
        buf.append("f\r");
        buf.append("\r");
        buf.flush();

        assertEquals("a\n" + "b\n" + "c\n" + "d\n" + "\n" + "e\n" + "\n" + "f\n" + "\n", buf.toString());
    }

    @Test
    public void lineLength() {
        assertEquals(0, buf.lineLength());

        buf.append("abc");
        assertEquals(3, buf.lineLength());

        buf.append("\n");
        assertEquals(0, buf.lineLength());

        buf.append("def");
        assertEquals(3, buf.lineLength());

        assertEquals(7, buf.length());
    }

    @Test
    public void endsWith() {
        assertEquals(false, buf.endsWith(null));

        assertEquals(false, buf.endsWith("abc"));

        buf.append("\nabc");
        assertEquals(true, buf.endsWith("abc"));
    }

    @Test
    public void endsWithNewLine() {
        assertEquals(true, buf.endsWithNewLine());

        buf.append("abc");
        assertEquals(false, buf.endsWithNewLine());

        buf.append("\n");
        assertEquals(true, buf.endsWithNewLine());

        buf.append("def");
        assertEquals(false, buf.endsWithNewLine());
    }
}
