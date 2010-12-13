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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.util.internal.ToStringBuilder.CollectionBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * ≤‚ ‘<code>ToStringBuilder</code>°£
 * 
 * @author Michael Zhou
 */
public class ToStringBuilderTests {
    private ToStringBuilder buf;
    private String result;

    @Before
    public void init() {
        buf = new ToStringBuilder();
        result = "";
    }

    @Test
    public void primitives() {
        buf.append(true).append("\n");
        buf.append((byte) 123).append("\n");
        buf.append('c').append("\n");
        buf.append(123.456D).append("\n");
        buf.append(123.456F).append("\n");
        buf.append(123).append("\n");
        buf.append(123456L).append("\n");
        buf.append((short) 123).append("\n");

        result += "true\n";
        result += "123\n";
        result += "c\n";
        result += "123.456\n";
        result += "123.456\n";
        result += "123\n";
        result += "123456\n";
        result += "123\n";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_multilines_desc() {
        Object[] values = getPrimitiveArrayData(18);

        buf.setPrintDescription(true).append(values);

        result += "java.lang.Object[9] [\n";
        result += "  [1/9] boolean[18] [\n";
        result += "          [01-10/18] true  false true  false true  false true  false true  false\n";
        result += "          [11-18/18] true  false true  false true  false true  false\n";
        result += "        ]\n";
        result += "  [2/9] byte[18] [\n";
        result += "          [01-10/18] 00 01 02 03 04 05 06 07 08 09\n";
        result += "          [11-18/18] 0a 0b 0c 0d 0e 0f 10 11\n";
        result += "        ]\n";
        result += "  [3/9] char[18] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] : ; < = > ? @ A\n";
        result += "        ]\n";
        result += "  [4/9] double[18] [\n";
        result += "          [01-10/18] 0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0\n";
        result += "          [11-18/18] 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0\n";
        result += "        ]\n";
        result += "  [5/9] float[18] [\n";
        result += "          [01-10/18] 0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0\n";
        result += "          [11-18/18] 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0\n";
        result += "        ]\n";
        result += "  [6/9] int[18] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] 10 11 12 13 14 15 16 17\n";
        result += "        ]\n";
        result += "  [7/9] long[18] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] 10 11 12 13 14 15 16 17\n";
        result += "        ]\n";
        result += "  [8/9] short[18] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] 10 11 12 13 14 15 16 17\n";
        result += "        ]\n";
        result += "  [9/9] aaa\n";
        result += "        bbb\n";
        result += "        ccc\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_multilines_no_desc() {
        Object[] values = getPrimitiveArrayData(18);

        buf.append(values);

        result += "[\n";
        result += "  [1/9] [\n";
        result += "          [01-10/18] true  false true  false true  false true  false true  false\n";
        result += "          [11-18/18] true  false true  false true  false true  false\n";
        result += "        ]\n";
        result += "  [2/9] [\n";
        result += "          [01-10/18] 00 01 02 03 04 05 06 07 08 09\n";
        result += "          [11-18/18] 0a 0b 0c 0d 0e 0f 10 11\n";
        result += "        ]\n";
        result += "  [3/9] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] : ; < = > ? @ A\n";
        result += "        ]\n";
        result += "  [4/9] [\n";
        result += "          [01-10/18] 0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0\n";
        result += "          [11-18/18] 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0\n";
        result += "        ]\n";
        result += "  [5/9] [\n";
        result += "          [01-10/18] 0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0\n";
        result += "          [11-18/18] 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0\n";
        result += "        ]\n";
        result += "  [6/9] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] 10 11 12 13 14 15 16 17\n";
        result += "        ]\n";
        result += "  [7/9] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] 10 11 12 13 14 15 16 17\n";
        result += "        ]\n";
        result += "  [8/9] [\n";
        result += "          [01-10/18] 0 1 2 3 4 5 6 7 8 9\n";
        result += "          [11-18/18] 10 11 12 13 14 15 16 17\n";
        result += "        ]\n";
        result += "  [9/9] aaa\n";
        result += "        bbb\n";
        result += "        ccc\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_oneline_desc() {
        Object[] values = getPrimitiveArrayData(8);

        buf.setPrintDescription(true).append(values);

        result += "java.lang.Object[9] [\n";
        result += "  [1/9] boolean[8] [true, false, true, false, true, false, true, false]\n";
        result += "  [2/9] byte[8] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [3/9] char[8] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [4/9] double[8] [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0]\n";
        result += "  [5/9] float[8] [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0]\n";
        result += "  [6/9] int[8] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [7/9] long[8] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [8/9] short[8] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [9/9] aaa\n";
        result += "        bbb\n";
        result += "        ccc\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_oneline_no_desc() {
        Object[] values = getPrimitiveArrayData(8);

        buf.append(values);

        result += "[\n";
        result += "  [1/9] [true, false, true, false, true, false, true, false]\n";
        result += "  [2/9] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [3/9] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [4/9] [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0]\n";
        result += "  [5/9] [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0]\n";
        result += "  [6/9] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [7/9] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [8/9] [0, 1, 2, 3, 4, 5, 6, 7]\n";
        result += "  [9/9] aaa\n";
        result += "        bbb\n";
        result += "        ccc\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_subset() {
        byte[] values = (byte[]) getPrimitiveArrayData(30)[1];

        buf.setPrintDescription(true).appendArray(values, 2, 20);

        result += "byte[30] [\n";
        result += "  [03-12/30] 02 03 04 05 06 07 08 09 0a 0b\n";
        result += "  [13-22/30] 0c 0d 0e 0f 10 11 12 13 14 15\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_subset_lengthOutOfRange() {
        byte[] values = (byte[]) getPrimitiveArrayData(30)[1];

        buf.setPrintDescription(true).appendArray(values, 2, 40);

        result += "byte[30] [\n";
        result += "  [03-12/30] 02 03 04 05 06 07 08 09 0a 0b\n";
        result += "  [13-22/30] 0c 0d 0e 0f 10 11 12 13 14 15\n";
        result += "  [23-30/30] 16 17 18 19 1a 1b 1c 1d\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void primitiveArrays_subset_offsetOutOfRange() {
        byte[] values = (byte[]) getPrimitiveArrayData(30)[1];

        buf.setPrintDescription(true).appendArray(values, 40, 40);

        result += "byte[30] []";

        assertEquals(result, buf.toString());
    }

    private Object[] getPrimitiveArrayData(int length) {
        boolean[] a1 = new boolean[length];
        byte[] a2 = new byte[length];
        char[] a3 = new char[length];
        double[] a4 = new double[length];
        float[] a5 = new float[length];
        int[] a6 = new int[length];
        long[] a7 = new long[length];
        short[] a8 = new short[length];

        for (int i = 0; i < length; i++) {
            a1[i] = i % 2 == 0;
            a2[i] = (byte) i;
            a3[i] = (char) ('0' + i);
            a4[i] = i;
            a5[i] = i;
            a6[i] = i;
            a7[i] = i;
            a8[i] = (short) i;
        }

        Object[] values = new Object[] { a1, a2, a3, a4, a5, a6, a7, a8, "aaa\nbbb\nccc" };
        return values;
    }

    @Test
    public void objects() {
        buf.append("aaa\rbbb\r\nccc");
        assertEquals("aaa\nbbb\nccc", buf.toString());

        buf.append("ddd\n\reee\n");
        assertEquals("aaa\nbbb\ncccddd\n\neee\n", buf.toString());
    }

    @Test
    public void _null() {
        buf.append(null);
        assertEquals("<null>", buf.toString());
    }

    @Test
    public void appendNull() {
        buf.appendNull();
        assertEquals("<null>", buf.toString());
    }

    @Test
    public void objectsWithHangingIndent() {
        buf.appendHangingIndent("aaa\nbbb\nccc");

        result += "aaa\n";
        result += "  bbb\n";
        result += "  ccc";

        assertEquals(result, buf.toString());

        buf.appendHangingIndent("ddd\neee\n");

        result += "\n";
        result += "ddd\n";
        result += "  eee\n";

        assertEquals(result, buf.toString());
    }

    @Test
    public void objectsWithHangingIndent2() {
        buf.appendHangingIndent("\n");
        assertEquals("\n", buf.toString());
    }

    @Test
    public void map() {
        MapBuilder mb = new MapBuilder().setSortKeys(true).setPrintCount(true);

        mb.append("dddd", "111\n222\n333");
        mb.append("ccc", "444\n555\n");
        mb.append("a", "666");

        buf.append("map ");
        buf.append(mb);

        result += "{\n";
        result += "  [1/3] a    = 666\n";
        result += "  [2/3] ccc  = 444\n";
        result += "               555\n";
        result += "  [3/3] dddd = 111\n";
        result += "               222\n";
        result += "               333\n";
        result += "}";

        assertEquals("map " + result, buf.toString());
        assertEquals(result, mb.toString());
    }

    @Test
    public void map_clear() {
        MapBuilder mb = new MapBuilder().setSortKeys(true).setPrintCount(true);

        mb.append("dddd", "111\n222\n333");
        mb.append("ccc", "444\n555\n");
        mb.append("a", "666");

        buf.append("map ");
        buf.append(mb);

        result += "{\n";
        result += "  [1/3] a    = 666\n";
        result += "  [2/3] ccc  = 444\n";
        result += "               555\n";
        result += "  [3/3] dddd = 111\n";
        result += "               222\n";
        result += "               333\n";
        result += "}";

        assertEquals("map " + result, buf.toString());
        assertEquals(result, mb.toString());

        mb.clear();
        buf.clear();

        assertEquals("{}", mb.toString());
        assertEquals("", buf.toString());
    }

    @Test
    public void map_noCount() {
        MapBuilder mb = new MapBuilder().setSortKeys(true).setPrintCount(false);

        mb.append("dddd", "111\n222\n333");
        mb.append("ccc", "444\n555\n");
        mb.append("a", "666");

        buf.append(mb);

        result += "{\n";
        result += "  a    = 666\n";
        result += "  ccc  = 444\n";
        result += "         555\n";
        result += "  dddd = 111\n";
        result += "         222\n";
        result += "         333\n";
        result += "}";

        assertEquals(result, mb.toString());
    }

    @Test
    public void appendMap() {
        Map<String, String> map = createHashMap();

        map.put("dddd", "111\n222\n333");
        map.put("ccc", "444\n555\n");
        map.put("a", "666");

        buf.appendMap(map, true);

        result += "{\n";
        result += "  [1/3] a    = 666\n";
        result += "  [2/3] ccc  = 444\n";
        result += "               555\n";
        result += "  [3/3] dddd = 111\n";
        result += "               222\n";
        result += "               333\n";
        result += "}";

        assertEquals(result, buf.toString());
    }

    @Test
    public void list() {
        CollectionBuilder cb = new CollectionBuilder().setPrintCount(true);

        cb.append("111\n222\n333");
        cb.append("444\n555\n");
        cb.append("666");

        buf.append("list ");
        buf.append(cb);

        result += "[\n";
        result += "  [1/3] 111\n";
        result += "        222\n";
        result += "        333\n";
        result += "  [2/3] 444\n";
        result += "        555\n";
        result += "  [3/3] 666\n";
        result += "]";

        assertEquals("list " + result, buf.toString());
        assertEquals(result, cb.toString());
    }

    @Test
    public void list_sort() {
        CollectionBuilder cb = new CollectionBuilder().setSort(true);

        cb.append("666");
        cb.append("444\n555\n");
        cb.append("111\n222\n333");

        buf.append("list ");
        buf.append(cb);

        result += "[\n";
        result += "  111\n";
        result += "    222\n";
        result += "    333\n";
        result += "  444\n";
        result += "    555\n";
        result += "  666\n";
        result += "]";

        assertEquals(result, cb.toString());
        assertEquals("list " + result, buf.toString());
    }

    @Test
    public void list_clear() {
        CollectionBuilder cb = new CollectionBuilder().setPrintCount(true);

        cb.append("111\n222\n333");
        cb.append("444\n555\n");
        cb.append("666");

        buf.append("list ");
        buf.append(cb);

        result += "[\n";
        result += "  [1/3] 111\n";
        result += "        222\n";
        result += "        333\n";
        result += "  [2/3] 444\n";
        result += "        555\n";
        result += "  [3/3] 666\n";
        result += "]";

        assertEquals("list " + result, buf.toString());
        assertEquals(result, cb.toString());

        cb.clear();
        buf.clear();

        assertEquals("[]", cb.toString());
        assertEquals("", buf.toString());
    }

    @Test
    public void list_oneline() {
        CollectionBuilder cb = new CollectionBuilder().setOneLine(true);

        cb.append("111");
        cb.append("444");
        cb.append("666");

        buf.append(cb);

        result += "[111, 444, 666]";

        assertEquals(result, cb.toString());
    }

    @Test
    public void list_noCount() {
        CollectionBuilder cb = new CollectionBuilder();

        cb.append("111\n222\n333");
        cb.append("444\n555\n");
        cb.append("666");

        buf.append("list ");
        buf.append(cb);

        result += "[\n";
        result += "  111\n";
        result += "    222\n";
        result += "    333\n";
        result += "  444\n";
        result += "    555\n";
        result += "  666\n";
        result += "]";

        assertEquals(result, cb.toString());
        assertEquals("list " + result, buf.toString());
    }

    @Test
    public void appendList() {
        List<String> list = createArrayList();

        list.add("111\n222\n333");
        list.add("444\n555\n");
        list.add("666");

        buf.appendCollection(list);

        result += "[\n";
        result += "  [1/3] 111\n";
        result += "        222\n";
        result += "        333\n";
        result += "  [2/3] 444\n";
        result += "        555\n";
        result += "  [3/3] 666\n";
        result += "]";

        assertEquals(result, buf.toString());
    }

    @Test
    public void appendDescription() {
        buf.appendDescription(new Object());

        assertTrue(buf.toString().startsWith("java.lang.Object@"));
    }
}
