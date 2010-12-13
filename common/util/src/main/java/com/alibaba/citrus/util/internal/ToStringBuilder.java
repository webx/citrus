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
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 方便实现<code>toString()</code>方法。
 * 
 * @author Michael Zhou
 */
public class ToStringBuilder {
    private static final String NULL_STR = "<null>";
    private static final int ARRAY_ITEMS_PER_LINE = 10;
    private final IndentableStringBuilder out;
    private final Formatter formatter;
    private boolean printDescription;

    public ToStringBuilder() {
        this(-1);
    }

    public ToStringBuilder(int indent) {
        this.out = new IndentableStringBuilder(indent);
        this.formatter = new Formatter(out);
    }

    /**
     * 清除所有数据。
     */
    public void clear() {
        out.clear();
        printDescription = false;
    }

    /**
     * 取得底层indentable string builder。
     */
    public IndentableStringBuilder out() {
        return out;
    }

    /**
     * 打印数组时，是否打印出描述。
     */
    public boolean isPrintDescription() {
        return printDescription;
    }

    /**
     * 打印数组时，是否打印出描述。
     */
    public ToStringBuilder setPrintDescription(boolean printDescription) {
        this.printDescription = printDescription;
        return this;
    }

    /**
     * 创建一级缩进。
     */
    public ToStringBuilder start() {
        out.start();
        return this;
    }

    /**
     * 创建一级缩进。
     */
    public ToStringBuilder start(String beginQuote, String endQuote) {
        out.start(beginQuote, endQuote);
        return this;
    }

    /**
     * 结束一级缩进。注意，输出结果之前，须至少调用一次end()，以确保最后的换行可以被输出。
     */
    public ToStringBuilder end() {
        out.end();
        return this;
    }

    public ToStringBuilder format(String format, Object... args) {
        formatter.format(format, args);
        return this;
    }

    public ToStringBuilder append(boolean value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder append(byte value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder append(char value) {
        out.append(value);
        return this;
    }

    public ToStringBuilder append(double value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder append(float value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder append(int value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder append(long value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder append(short value) {
        out.append(String.valueOf(value));
        return this;
    }

    public ToStringBuilder appendNull() {
        out.append(NULL_STR);
        return this;
    }

    public ToStringBuilder append(Object value) {
        if (value == null) {
            appendNull();
        } else if (value instanceof Object[]) {
            appendArray((Object[]) value);
        } else if (value.getClass().isArray()) {
            appendPrimitiveArray(value, -1);
        } else if (value instanceof Collection<?>) {
            appendCollection((Collection<?>) value);
        } else if (value instanceof Map<?, ?>) {
            appendMap((Map<?, ?>) value);
        } else if (value instanceof StructureBuilder) {
            ((StructureBuilder) value).appendTo(this);
        } else {
            out.append(value.toString());
        }

        return this;
    }

    public ToStringBuilder appendDescription(Object value) {
        if (value != null) {
            if (value.getClass().isArray()) {
                format("%s[%d]", value.getClass().getComponentType().getCanonicalName(), Array.getLength(value));
            } else {
                format("%s@%x", value.getClass().getCanonicalName(), System.identityHashCode(value));
            }
        }

        return this;
    }

    public ToStringBuilder appendHangingIndent(Object value) {
        return appendHangingIndent(value, -1);
    }

    private ToStringBuilder appendHangingIndent(Object value, int hangingIndent) {
        out.startHangingIndent(hangingIndent);
        append(value);
        out.end();

        return this;
    }

    public ToStringBuilder appendMap(Map<?, ?> map) {
        return appendMap(map, false);
    }

    public ToStringBuilder appendMap(Map<?, ?> map, boolean sort) {
        if (map == null) {
            appendNull();
        } else {
            new MapBuilder().setSortKeys(sort).setPrintCount(true).appendAll(map).appendTo(this);
        }

        return this;
    }

    public ToStringBuilder appendCollection(Collection<?> list) {
        return appendCollection(list, false);
    }

    public ToStringBuilder appendCollection(Collection<?> list, boolean oneLine) {
        if (list == null) {
            appendNull();
        } else {
            new CollectionBuilder().setPrintCount(true).setOneLine(oneLine).appendAll(list).appendTo(this);
        }

        return this;
    }

    public ToStringBuilder appendArray(Object[] list) {
        if (isPrintDescription()) {
            appendDescription(list);
        }

        return appendCollection(asList(list));
    }

    public ToStringBuilder appendArray(boolean[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(boolean[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(byte[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(byte[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(char[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(char[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(double[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(double[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(float[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(float[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(int[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(int[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(long[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(long[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    public ToStringBuilder appendArray(short[] list) {
        return appendPrimitiveArray(list, -1);
    }

    public ToStringBuilder appendArray(short[] list, int offset, int length) {
        return appendPrimitiveArray(list, -1, offset, length);
    }

    private ToStringBuilder appendPrimitiveArray(Object primitiveList, int itemsPerLine) {
        return appendPrimitiveArray(primitiveList, itemsPerLine, -1, -1);
    }

    private ToStringBuilder appendPrimitiveArray(Object primitiveList, int itemsPerLine, int offset, int length) {
        if (primitiveList == null) {
            return appendNull();
        }

        if (itemsPerLine <= 0) {
            itemsPerLine = ARRAY_ITEMS_PER_LINE;
        }

        int arrayLength = Array.getLength(primitiveList);

        if (offset < 0) {
            offset = 0;
        }

        if (length < 0 || offset + length > arrayLength) {
            length = arrayLength - offset;
        }

        int lengthWidth = String.valueOf(length).length();
        String countPattern = "[%0" + lengthWidth + "d-%0" + lengthWidth + "d/%" + lengthWidth + "d] ";

        if (isPrintDescription()) {
            appendDescription(primitiveList).append(" ");
        }

        if (length == 0) {
            if (!isPrintDescription()) {
                append("[]");
            }

            return this;
        } else if (length <= itemsPerLine) {
            append("[");

            for (int i = 0; i < length; i++) {
                appendPrimitive(Array.get(primitiveList, i), false);

                if (i < length - 1) {
                    append(", ");
                }
            }

            append("]");
        } else {
            start("[", "]");

            int lines = (length + itemsPerLine - 1) / itemsPerLine;

            for (int l = 0; l < lines; l++) {
                int start = l * itemsPerLine + offset;
                int end = Math.min((l + 1) * itemsPerLine, length) + offset;

                format(countPattern, start + 1, end, arrayLength);

                for (int i = start; i < end; i++) {
                    Object value = Array.get(primitiveList, i);
                    appendPrimitive(value, true);

                    if (i < end - 1) {
                        format(" ");
                    }
                }

                if (l < lines - 1) {
                    format("%n");
                }
            }

            end();
        }

        return this;
    }

    private void appendPrimitive(Object value, boolean fixedWidth) {
        String pattern;

        if (value instanceof Byte) {
            pattern = fixedWidth ? "%02x" : "%x";
        } else if (value instanceof Boolean) {
            pattern = fixedWidth ? "%-5s" : "%s";
        } else {
            pattern = "%s";
        }

        format(pattern, value);
    }

    @Override
    public String toString() {
        return out.toString();
    }

    /**
     * 可用来创建一个结构的builder，例如：map，collection等。
     * 
     * @author Michael Zhou
     */
    public static interface StructureBuilder {
        ToStringBuilder appendTo(ToStringBuilder toStringBuilder);
    }

    /**
     * 创建一系列key/value值对。
     */
    public static class MapBuilder implements StructureBuilder {
        private final Map<String, Object> map = createLinkedHashMap();
        private boolean sortKeys;
        private boolean printCount;

        public MapBuilder clear() {
            map.clear();
            sortKeys = false;
            printCount = false;
            return this;
        }

        public boolean isSortKeys() {
            return sortKeys;
        }

        public MapBuilder setSortKeys(boolean sortKeys) {
            this.sortKeys = sortKeys;
            return this;
        }

        public boolean isPrintCount() {
            return printCount;
        }

        public MapBuilder setPrintCount(boolean printCount) {
            this.printCount = printCount;
            return this;
        }

        public MapBuilder append(String key, Object value) {
            key = defaultIfNull(key, NULL_STR);
            map.put(key, value);
            return this;
        }

        public MapBuilder appendAll(Map<?, ?> map) {
            if (map != null) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = entry.getKey() == null ? NULL_STR : entry.getKey().toString();

                    append(key, entry.getValue());
                }
            }

            return this;
        }

        public void remove(String key) {
            map.remove(key);
        }

        public ToStringBuilder appendTo(ToStringBuilder toStringBuilder) {
            if (map.isEmpty()) {
                toStringBuilder.append("{}");
                return toStringBuilder;
            }

            List<String> keys = createArrayList(map.keySet());

            if (sortKeys) {
                Collections.sort(keys);
            }

            int maxKeyLength = 0;

            for (String key : keys) {
                maxKeyLength = Math.max(maxKeyLength, key.length());
            }

            String pattern;

            if (printCount) {
                int size = keys.size();
                int sizeWidth = String.valueOf(size).length();

                pattern = "[%" + sizeWidth + "d/" + size + "] %-" + maxKeyLength + "s = ";
            } else {
                pattern = "%-" + maxKeyLength + "s = ";
            }

            int count = 1;

            toStringBuilder.start("{", "}");

            for (String key : keys) {
                Object value = map.get(key);

                if (printCount) {
                    toStringBuilder.format(pattern, count++, key);
                } else {
                    toStringBuilder.format(pattern, key);
                }

                toStringBuilder.out().startHangingIndent();
                toStringBuilder.append(value);
                toStringBuilder.out().end();
            }

            return toStringBuilder.end();
        }

        @Override
        public String toString() {
            return appendTo(new ToStringBuilder()).toString();
        }
    }

    /**
     * 创建值的列表。
     */
    public static class CollectionBuilder implements StructureBuilder {
        private final List<Object> list = createLinkedList();
        private boolean sort;
        private boolean printCount;
        private boolean oneLine;

        public CollectionBuilder clear() {
            list.clear();
            sort = false;
            printCount = false;
            oneLine = false;
            return this;
        }

        public boolean isSort() {
            return sort;
        }

        public CollectionBuilder setSort(boolean sort) {
            this.sort = sort;
            return this;
        }

        public boolean isPrintCount() {
            return printCount;
        }

        public CollectionBuilder setPrintCount(boolean printCount) {
            this.printCount = printCount;
            return this;
        }

        public boolean isOneLine() {
            return oneLine;
        }

        public CollectionBuilder setOneLine(boolean oneLine) {
            this.oneLine = oneLine;
            return this;
        }

        public CollectionBuilder append(Object value) {
            list.add(value);
            return this;
        }

        public CollectionBuilder appendAll(Collection<?> list) {
            if (list != null) {
                for (Object value : list) {
                    append(value);
                }
            }

            return this;
        }

        public CollectionBuilder appendAll(Object[] list) {
            return appendAll(asList(list));
        }

        public ToStringBuilder appendTo(ToStringBuilder toStringBuilder) {
            if (list.isEmpty()) {
                toStringBuilder.append("[]");
                return toStringBuilder;
            }

            List<Object> list = this.list;

            if (sort) {
                list = createArrayList(list);

                Collections.sort(list, new Comparator<Object>() {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    public int compare(Object o1, Object o2) {
                        if (o1 instanceof Comparable<?> && o2 instanceof Comparable<?>) {
                            return ((Comparable) o1).compareTo(o2);
                        }

                        return 0;
                    }
                });
            }

            if (oneLine) {
                toStringBuilder.append("[");

                for (Iterator<Object> i = list.iterator(); i.hasNext();) {
                    toStringBuilder.append(i.next());

                    if (i.hasNext()) {
                        toStringBuilder.append(", ");
                    }
                }

                toStringBuilder.append("]");

                return toStringBuilder;
            }

            int size = list.size();
            int sizeWidth = String.valueOf(size).length();
            String pattern = "[%" + sizeWidth + "d/" + size + "] ";
            int count = 1;

            toStringBuilder.start("[", "]");

            for (Object value : list) {
                if (printCount) {
                    toStringBuilder.format(pattern, count++);
                }

                toStringBuilder.out().startHangingIndent();
                toStringBuilder.append(value);
                toStringBuilder.out().end();
            }

            return toStringBuilder.end();
        }

        @Override
        public String toString() {
            return appendTo(new ToStringBuilder()).toString();
        }
    }
}
