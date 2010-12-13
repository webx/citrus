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

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>IndentableStringBuilder</code>。
 * 
 * @author Michael Zhou
 */
public class IndentableStringBuilderTests extends AbstractNormalizableStringBuilderTests<IndentableStringBuilder> {
    @Before
    public void init() {
        buf = new IndentableStringBuilder();
    }

    @Test
    public void indent_simple() {
        buf.append("aaa").start();
        buf.append("bbb");
        buf.end();

        assertEquals("aaa {\n" + "  bbb\n" + "}", buf.toString());
    }

    @Test
    public void indent_simple2() {
        buf.append("aaa").start(4);
        buf.append("bbb");
        buf.end();

        buf.start();
        buf.append("ccc");
        buf.end();

        assertEquals("aaa {\n" + "    bbb\n" + "}\n" + "{\n" + "  ccc\n" + "}", buf.toString());
    }

    @Test
    public void indent_multilines() {
        buf.append("a\n");
        buf.append("aa\n");
        buf.append("aaa").start();

        buf.append("b\n");
        buf.append("bb\n");
        buf.append("bbb\n"); // 最后一个\n会被忽略

        buf.end();

        assertEquals("a\n" + "aa\n" + "aaa {\n" + "  b\n" + "  bb\n" + "  bbb\n" + "}", buf.toString());
    }

    @Test
    public void indent_multiple_indention() {
        buf.append("aaa").start();
        {
            buf.start("[", "]");
            {
                buf.start("[[", "]]");
                buf.end();
            }
            buf.end();

            buf.start("[", "]");
            {
                buf.start("[[", "]]");
                {
                    buf.append("bbb");
                }
                buf.end();

                buf.append("ccc");
            }
            buf.end();

            buf.start("[[[", "]]]");
            {
                buf.append("ddd");

                buf.start("", "");
                {
                    buf.append("eee\n");

                    buf.startHangingIndent();
                    {
                        buf.append("fff\nhhh\niii"); // 悬挂缩进：默认缩进2
                    }
                    buf.end();

                    buf.startHangingIndent();
                    {
                        buf.append("jjj"); // 悬挂缩进 （由于没有换行，所以实际不缩进）
                    }
                    buf.end();

                    buf.append("value = ").startHangingIndent();
                    {
                        buf.append("lll\n"); // 悬挂缩进：缩进到当前列（即value = 之后）
                        buf.append("mmm");
                    }
                    buf.end();

                    buf.append("kkk");

                }
                buf.end();

                buf.append("ggg");
            }
            buf.end();
        }
        buf.end();

        assertEquals("aaa {\n" + "  [\n" + "    [[\n" + "      bbb\n" + "    ]]\n" + "    ccc\n" + "  ]\n" + "  [[[\n"
                + "    ddd\n" + "      eee\n" + "      fff\n" + "        hhh\n" + "        iii\n" + "      jjj\n"
                + "      value = lll\n" + "              mmm\n" + "      kkk\n" + "    ggg\n" + "  ]]]\n" + "}",
                buf.toString());
    }

    @Test
    public void indent_hanging() {
        buf.startHangingIndent();
        buf.append("aaa");
        buf.end();

        assertEquals("aaa", buf.toString());
    }

    @Test
    public void indent_hanging_2() {
        buf.startHangingIndent(4);
        buf.append("aaa\n");
        buf.append("bbb");
        buf.end();

        buf.start();
        buf.append("ccc");
        buf.end();

        assertEquals("aaa\n    bbb\n{\n  ccc\n}", buf.toString());
    }

    @Test
    public void indent_hanging_3() {
        buf.startHangingIndent();
        buf.append("aaa\n");
        buf.append("bbb\n");
        buf.end();

        assertEquals("aaa\n  bbb\n", buf.toString());
    }

    @Test
    public void indent_hanging_4() {
        buf.startHangingIndent();
        buf.append("\n");
        buf.end();

        assertEquals("\n", buf.toString());
    }

    @Test
    public void indent_hanging_5() {
        buf.start();

        buf.startHangingIndent();
        buf.append("a");
        buf.end();

        buf.startHangingIndent();
        buf.append("b");
        buf.end();

        buf.startHangingIndent();
        buf.append("c");
        buf.end();

        buf.end();

        assertEquals("{\n" + "  a\n" + "  b\n" + "  c\n" + "}", buf.toString());
    }

    @Test
    public void indent_hanging_after_start() {
        buf.start();
        {
            buf.append("a = ").startHangingIndent();
            {
                buf.append("values").start();
                {
                    buf.append("value 1\n");
                    buf.append("value 2\n");
                }
                buf.end();
            }
            buf.end();
        }
        buf.end();

        assertEquals("{\n" + "  a = values {\n" + "        value 1\n" + "        value 2\n" + "      }\n" + "}",
                buf.toString());
    }
}
