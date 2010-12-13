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
package com.alibaba.citrus.service.requestcontext.parser;

import static com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.beans.PropertyEditor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;

/**
 * 测试ValueParser类。
 * 
 * @author Michael Zhou
 */
public class ValueParserTests extends AbstractRequestContextsTests<ParserRequestContext> {
    private final static Logger log = LoggerFactory.getLogger(ValueParserTests.class);
    private AbstractValueParser parser;
    private AbstractValueParser parserNoisy;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-parser.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");

        // converterQuiet=false
        initRequestContext("parser_noisy");
        parserNoisy = new AbstractValueParser(requestContext) {
            @Override
            protected Logger getLogger() {
                return log;
            }
        };

        assertEquals(URL_CASE_FOLDING_LOWER_WITH_UNDERSCORES, requestContext.getCaseFolding());
        assertFalse(requestContext.isConverterQuiet());

        // converterQuiet=true
        initRequestContext();
        parser = new AbstractValueParser(requestContext) {
            @Override
            protected Logger getLogger() {
                return log;
            }
        };

        assertEquals(URL_CASE_FOLDING_LOWER_WITH_UNDERSCORES, requestContext.getCaseFolding());
        assertTrue(requestContext.isConverterQuiet());
    }

    @Test
    public void getBoolean() {
        parser.add("Aa_a", " true ");
        parser.add("aaA", false);

        // 测试单项
        assertEquals(true, parser.getBoolean("aa_a"));

        // 测试数组
        assertAnyArrayEquals(new boolean[] { true, false }, parser.getObjectOfType("aa_a", boolean[].class));

        // 测试默认值
        assertEquals(false, parser.getBoolean("bbb"));
        assertEquals(true, parser.getBoolean("bbb", true));

        // 测试数组默认值
        parser.add("bbb", null);
        assertAnyArrayEquals(new boolean[] {}, parser.getObjectOfType("bbb", boolean[].class));
        assertAnyArrayEquals(new boolean[] { true, true, false },
                parser.getObjectOfType("bbb", boolean[].class, null, new Boolean[] { true, true, false }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(false, parser.getBoolean("ccc"));

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getBoolean("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Boolean"));
        }
    }

    @Test
    public void getByte() {
        parser.add("Aa_a", " 11  ");
        parser.add("aaA", (byte) 22);

        // 测试单项
        assertEquals((byte) 11, parser.getByte("aa_a"));

        // 测试数组
        assertArrayEquals(new byte[] { 11, 22 }, parser.getObjectOfType("aa_a", byte[].class));

        // 测试默认值
        assertEquals((byte) 0, parser.getByte("bbb"));
        assertEquals((byte) 33, parser.getByte("bbb", (byte) 33));

        // 测试数组默认值
        parser.add("bbb", null);
        assertArrayEquals(new byte[] {}, parser.getObjectOfType("bbb", byte[].class));
        assertArrayEquals(new byte[] { 33, 44 },
                parser.getObjectOfType("bbb", byte[].class, null, new Byte[] { 33, 44 }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(0, parser.getByte("ccc"));

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getByte("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Byte"));
        }
    }

    @Test
    public void getBytes() throws UnsupportedEncodingException {
        parser.add("Aaa", "abcde");

        byte[] values = parser.getBytes("aaa");

        assertEquals('a', values[0]);
        assertEquals('b', values[1]);
        assertEquals('c', values[2]);
        assertEquals('d', values[3]);
        assertEquals('e', values[4]);
    }

    @Test
    public void getChar() {
        parser.add("Aa_a", "a");
        parser.add("aaA", 'b');

        // 测试单项
        assertEquals('a', parser.getChar("aa_a"));

        // 测试数组
        char[] values = parser.getObjectOfType("aa_a", char[].class);

        assertEquals('a', values[0]);
        assertEquals('b', values[1]);

        // 测试默认值
        assertEquals('\0', parser.getChar("bbb"));
        assertEquals('c', parser.getChar("bbb", 'c'));

        // 测试数组默认值
        parser.add("bbb", null);
        assertArrayEquals(new char[] {}, parser.getObjectOfType("bbb", char[].class));
        assertArrayEquals(new char[] { 'a', 'b' },
                parser.getObjectOfType("bbb", char[].class, null, new Character[] { 'a', 'b' }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals('\0', parser.getChar("ccc"));

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getChar("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Character"));
        }
    }

    @Test
    public void getDouble() {
        parser.add("Aa_a", " 1.23 ");
        parser.add("aaA", 2.34D);

        // 测试单项
        assertEquals(1.23D, parser.getDouble("aa_a"), 0.0D);

        // 测试数组
        double[] values = parser.getObjectOfType("aa_a", double[].class);

        assertEquals(1.23D, values[0], 0);
        assertEquals(2.34D, values[1], 0);

        // 测试默认值
        assertEquals(0.0D, parser.getDouble("bbb"), 0.0D);
        assertEquals(3.45D, parser.getDouble("bbb", 3.45D), 0.0D);

        // 测试数组默认值
        parser.add("bbb", null);
        assertAnyArrayEquals(new double[] {}, parser.getObjectOfType("bbb", double[].class));
        assertAnyArrayEquals(new double[] { 11D, 22D },
                parser.getObjectOfType("bbb", double[].class, null, new Double[] { 11D, 22D }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(0D, parser.getDouble("ccc"), 0);

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getDouble("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Double"));
        }
    }

    @Test
    public void getFloat() {
        parser.add("Aa_a", "  1.23  ");
        parser.add("aaA", 2.34F);

        // 测试单项
        assertEquals(1.23F, parser.getFloat("aa_a"), 0.0F);

        // 测试数组
        float[] values = parser.getObjectOfType("aa_a", float[].class);

        assertEquals(1.23F, values[0], 0);
        assertEquals(2.34F, values[1], 0);

        // 测试默认值
        assertEquals(0.0f, parser.getFloat("bbb"), 0.0F);
        assertEquals(3.45F, parser.getFloat("bbb", 3.45F), 0.0D);

        // 测试数组默认值
        parser.add("bbb", null);
        assertAnyArrayEquals(new float[] {}, parser.getObjectOfType("bbb", float[].class));
        assertAnyArrayEquals(new float[] { 11F, 22F },
                parser.getObjectOfType("bbb", float[].class, null, new Float[] { 11F, 22F }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(0D, parser.getFloat("ccc"), 0);

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getFloat("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Float"));
        }
    }

    @Test
    public void getInt() {
        parser.add("Aa_a", "  111  ");
        parser.add("aaA", 222);

        // 测试单项
        assertEquals(111, parser.getInt("aa_a"));

        // 测试数组
        assertArrayEquals(new int[] { 111, 222 }, parser.getInts("aa_a"));

        // 测试默认值
        assertEquals(0, parser.getInt("bbb"));
        assertEquals(333, parser.getInt("bbb", 333));

        // 测试数组默认值
        parser.add("bbb", null);
        assertArrayEquals(new int[] {}, parser.getInts("bbb"));
        assertArrayEquals(new int[] { 3, 4 }, parser.getInts("bbb", new int[] { 3, 4 }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(0D, parser.getInt("ccc"), 0);

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getInt("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Integer"));
        }
    }

    @Test
    public void getLong() {
        parser.add("Aa_a", 111L);
        parser.add("aaA", 222L);

        // 测试单项
        assertEquals(111L, parser.getLong("aa_a"));

        // 测试数组
        assertArrayEquals(new long[] { 111L, 222L }, parser.getLongs("aa_a"));

        // 测试默认值
        assertEquals(0L, parser.getLong("bbb"));
        assertEquals(333, parser.getLong("bbb", 333));

        // 测试数组默认值
        parser.add("bbb", null);
        assertArrayEquals(new long[] {}, parser.getLongs("bbb"));
        assertArrayEquals(new long[] { 1, 2 }, parser.getLongs("bbb", new long[] { 1, 2 }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(0D, parser.getLong("ccc"), 0);

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getLong("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Long"));
        }
    }

    @Test
    public void getShort() {
        parser.add("Aa_a", (short) 11);
        parser.add("aaA", (short) 22);

        // 测试单项
        assertEquals((short) 11, parser.getShort("aa_a"));

        // 测试数组
        assertAnyArrayEquals(new short[] { 11, 22 }, parser.getObjectOfType("aa_a", short[].class));

        // 测试默认值
        assertEquals((short) 0, parser.getShort("bbb"));
        assertEquals((short) 33, parser.getShort("bbb", (short) 33));

        // 测试数组默认值
        parser.add("bbb", null);
        assertAnyArrayEquals(new short[] {}, parser.getObjectOfType("bbb", short[].class, null, null));
        assertAnyArrayEquals(new short[] { 1, 2 },
                parser.getObjectOfType("bbb", short[].class, null, new Short[] { 1, 2 }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(0D, parser.getShort("ccc"), 0);

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getShort("ccc");
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Short"));
        }
    }

    @Test
    public void getString() {
        parser.add("Aa_a", "111");
        parser.add("aaA", "222");

        // 测试单项
        assertEquals("111", parser.getString("aa_a"));

        // 测试数组
        assertArrayEquals(new String[] { "111", "222" }, parser.getStrings("aa_a"));

        // 测试默认值
        assertEquals(null, parser.getString("bbb"));
        assertEquals("333", parser.getString("bbb", "333"));
        assertEquals(null, parser.getString("bbb", null));

        // 测试数组默认值
        parser.add("bbb", null);
        assertArrayEquals(new String[] {}, parser.getStrings("bbb"));
        assertArrayEquals(new String[] { "aa", "bb" }, parser.getStrings("bbb", new String[] { "aa", "bb" }));

        // 特殊情况
        parser.add("ccc", "null");
        assertEquals(null, parser.getString("ccc"));
        assertEquals("333", parser.getString("ccc", "333"));
        assertEquals("", parser.getStrings("ccc")[0]);

        parser.add("ccc", "");
        assertEquals(null, parser.getString("ccc"));
        assertEquals("333", parser.getString("ccc", "333"));
        assertEquals("", parser.getStrings("ccc")[0]);
    }

    @Test
    public void get() {
        parser.add("Aaa", new Integer(111));

        assertEquals(new Integer(111), parser.get("aaa"));
        assertNull(parser.get("bbb"));
    }

    @Test
    public void getObject() {
        parser.add("Aa_a", new Integer(111));
        parser.add("aaA", new Double(222));

        // 测试单项
        assertEquals(new Integer(111), parser.getObject("aa_a"));

        // 测试数组
        assertArrayEquals(new Object[] { 111, 222D }, parser.getObjects("aa_a"));

        // 测试默认值
        assertEquals(null, parser.getObject("bbb"));
        assertEquals("333", parser.getObject("bbb", "333"));

        // 测试数组默认值
        assertArrayEquals(new Object[] {}, parser.getObjects("bbb"));
        assertArrayEquals(new Object[] { 111 }, parser.getObjects("bbb", new Object[] { 111 }));
    }

    @Test
    public void getObjectOfType() {
        parser.add("Aa_a", "  111  ");
        parser.add("aaA", 222);

        // 测试单项
        assertEquals((Integer) 111, parser.getObjectOfType("aa_a", Integer.class));

        // 测试数组
        assertArrayEquals(new int[] { 111, 222 }, parser.getObjectOfType("aa_a", int[].class));

        // 测试默认值
        assertEquals(null, parser.getObjectOfType("bbb", Integer.class));
        assertEquals((Integer) 0, parser.getObjectOfType("bbb", Integer.class, true, null, null));
        assertEquals((Integer) 333, parser.getObjectOfType("bbb", Integer.class, null, new Object[] { 333 }));

        // 测试数组默认值
        parser.add("bbb", null);
        assertArrayEquals(new int[] {}, parser.getObjectOfType("bbb", int[].class));
        assertArrayEquals(new int[] { 3, 4 }, parser.getObjectOfType("bbb", int[].class, null, new Integer[] { 3, 4 }));

        // 测试非法值
        parser.add("ccc", "illegal");
        assertEquals(null, parser.getObjectOfType("ccc", Integer.class));

        try {
            parserNoisy.add("ccc", "illegal");
            parserNoisy.getObjectOfType("ccc", Integer.class);
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal", "Integer"));
        }
    }

    @Test
    public void getDate() {
        parser.add("aaa", "2003-10-21");

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = parser.getDate("aaa", format);
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        assertEquals(2003, calendar.get(Calendar.YEAR));
        assertEquals(9, calendar.get(Calendar.MONTH));
        assertEquals(21, calendar.get(Calendar.DATE));

        assertSame(date, parser.getDate("bbb", format, date));
    }

    @Test
    public void setString() {
        parser.add("Aa_a", "111");
        parser.setString("aaA", "222");

        assertEquals("222", parser.getString("aa_a"));

        parser.setStrings("aa_a", new String[] { "333", "444" });

        assertEquals("333", parser.getString("aa_a"));

        assertEquals("333", parser.getStrings("aa_a")[0]);
        assertEquals("444", parser.getStrings("aa_a")[1]);
    }

    @Test
    public void setObject() {
        parser.add("Aa_a", new Integer(111));
        parser.setObject("aaA", new Integer(222));

        assertEquals("222", parser.getString("aa_a"));

        parser.setObjects("aa_a", new Object[] { new Integer(333), new Integer(444) });

        assertEquals("333", parser.getString("aa_a"));

        assertEquals("333", parser.getStrings("aa_a")[0]);
        assertEquals("444", parser.getStrings("aa_a")[1]);
    }

    @Test
    public void remove() {
        parser.add("Aaa", "111");

        assertEquals("111", parser.getString("aaa"));

        parser.remove("aaa");

        assertNull(parser.getString("aaa"));
    }

    @Test
    public void clear() {
        parser.add("Aa_a", "111");
        parser.add("bbB", "222");

        assertEquals("111", parser.getString("aa_a"));
        assertEquals("222", parser.getString("bb_b"));

        parser.clear();

        assertNull(parser.getString("aa_a"));
        assertNull(parser.getString("bb_b"));
    }

    @Test
    public void containsKey() {
        parser.add("Aa_a", "111");
        parser.add("bbB", "222");

        assertTrue(parser.containsKey("aa_a"));
        assertTrue(parser.containsKey("bb_b"));
        assertFalse(parser.containsKey("ccc"));
    }

    @Test
    public void keys() {
        parser.add("Aa_a", "111");
        parser.add("bbB", "222");

        String[] keys = parser.getKeys();

        assertEquals("Aa_a", keys[0]);
        assertEquals("bbB", keys[1]);

        Iterator<String> i = parser.keySet().iterator();

        assertEquals("Aa_a", i.next());
        assertEquals("bbB", i.next());
    }

    @Test
    public void setProperties() {
        MyClass myClass = new MyClass();

        parser.setObject("my_integer", "illegal");
        parser.setObjects("my_strings", new Object[] { "hello\nworld", "haha" });
        parser.setObject("my_long", "ten");
        parser.setObjects("my_integerList", new Object[] { 1, 2, 3 });
        parser.setObject("my_noProperty", "value");

        parser.setProperties(myClass);

        assertEquals(0, myClass.getMyInteger());
        assertArrayEquals(new String[] { "hello\nworld", "haha" }, myClass.getMyStrings());
        assertEquals(10L, myClass.getMyLong());
        assertArrayEquals(new Integer[] { 1, 2, 3 }, myClass.getMyIntegerList().toArray(new Integer[3]));
    }

    @Test
    public void setProperties_noisy() {
        MyClass myClass = new MyClass();

        parserNoisy.setObject("my_integer", "illegal");
        parserNoisy.setObject("my_strings", new Object[] { "hello\nworld", "haha" });
        parserNoisy.setObject("my_long", "ten");
        parserNoisy.setObject("my_integerList", new Object[] { 1, 2, 3 });
        parserNoisy.setObject("my_noProperty", "value");

        try {
            parserNoisy.setProperties(myClass);
            fail();
        } catch (TypeMismatchException e) {
            assertThat(e, exception("illegal"));
        }
    }

    public static class MyClass {
        private int myInteger = -1;
        private String[] myStrings;
        private long myLong = -1;
        private List<Integer> myIntegerList;

        public int getMyInteger() {
            return myInteger;
        }

        public void setMyInteger(int myInteger) {
            this.myInteger = myInteger;
        }

        public String[] getMyStrings() {
            return myStrings;
        }

        public void setMyStrings(String[] myStrings) {
            this.myStrings = myStrings;
        }

        public long getMyLong() {
            return myLong;
        }

        public void setMyLong(long myLong) {
            this.myLong = myLong;
        }

        public List<Integer> getMyIntegerList() {
            return myIntegerList;
        }

        public void setMyIntegerList(List<Integer> myIntegerList) {
            this.myIntegerList = myIntegerList;
        }
    }

    private void assertAnyArrayEquals(Object b1, Object b2) {
        assertEquals(Array.getLength(b1), Array.getLength(b2));

        for (int i = 0; i < Array.getLength(b1); i++) {
            assertEquals(Array.get(b1, i), Array.get(b2, i));
        }
    }

    public static class MyRegistrar implements PropertyEditorRegistrar {
        private final static String[] NUMBERS = { "zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "ten" };

        public void registerCustomEditors(PropertyEditorRegistry registry) {
            PropertyEditor editor = new CustomNumberEditor(Long.class, true) {
                @Override
                public void setAsText(String text) {
                    for (int i = 0; i < NUMBERS.length; i++) {
                        if (NUMBERS[i].equalsIgnoreCase(text)) {
                            setValue(i);
                            return;
                        }
                    }

                    super.setAsText(text);
                }
            };

            registry.registerCustomEditor(Long.class, editor);
            registry.registerCustomEditor(long.class, editor);
        }
    }
}
