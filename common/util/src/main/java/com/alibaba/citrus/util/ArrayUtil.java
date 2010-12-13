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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Iterator;
import java.util.Map;

/**
 * 有关数组处理的工具类。
 * <p>
 * 这个类中的每个方法都可以“安全”地处理<code>null</code>，而不会抛出<code>NullPointerException</code>。
 * </p>
 * <p>
 * 本工具类是对JDK <code>Arrays</code>的补充。
 * </p>
 * 
 * @author Michael Zhou
 */
public class ArrayUtil {
    // ==========================================================================
    // 取得数组长度。 
    // ==========================================================================

    /**
     * 取得数组的长度。
     * <p>
     * 此方法比<code>Array.getLength()</code>要快得多。
     * </p>
     * 
     * @param array 要检查的数组
     * @return 如果为空，或者非数组，则返回<code>0</code>。
     */
    public static int arrayLength(Object array) {
        return arrayLength(array, 0, 0);
    }

    private static int arrayLength(Object array, int defaultIfNull, int defaultIfNotArray) {
        if (array == null) {
            return defaultIfNull; // null
        } else if (array instanceof Object[]) {
            return ((Object[]) array).length;
        } else if (array instanceof long[]) {
            return ((long[]) array).length;
        } else if (array instanceof int[]) {
            return ((int[]) array).length;
        } else if (array instanceof short[]) {
            return ((short[]) array).length;
        } else if (array instanceof byte[]) {
            return ((byte[]) array).length;
        } else if (array instanceof double[]) {
            return ((double[]) array).length;
        } else if (array instanceof float[]) {
            return ((float[]) array).length;
        } else if (array instanceof boolean[]) {
            return ((boolean[]) array).length;
        } else if (array instanceof char[]) {
            return ((char[]) array).length;
        } else {
            return defaultIfNotArray; // not an array
        }
    }

    // ==========================================================================
    // 判空函数。                                                                  
    //  
    // 判断一个数组是否为null或包含0个元素。                                       
    // ==========================================================================

    /**
     * 检查数组是否为<code>null</code>或空数组<code>[]</code>。
     * 
     * <pre>
     * ArrayUtil.isEmptyArray(null)              = true
     * ArrayUtil.isEmptyArray(new int[0])        = true
     * ArrayUtil.isEmptyArray(new int[10])       = false
     * </pre>
     * 
     * @param array 要检查的数组
     * @return 如果为空, 则返回<code>true</code>
     */
    public static boolean isEmptyArray(Object array) {
        return arrayLength(array, 0, -1) == 0;
    }

    // ==========================================================================
    // 默认值函数。 
    //  
    // 当数组为空时，取得默认数组值。
    // 注：判断数组为null时，可用更通用的ObjectUtil.defaultIfNull。
    // ==========================================================================

    /**
     * 如果数组是<code>null</code>或空数组<code>[]</code>，则返回指定数组默认值。
     * 
     * <pre>
     * ArrayUtil.defaultIfEmpty(null, defaultArray)           = defaultArray
     * ArrayUtil.defaultIfEmpty(new String[0], defaultArray)  = 数组本身
     * ArrayUtil.defaultIfEmpty(new String[10], defaultArray) = 数组本身
     * </pre>
     * 
     * @param array 要转换的数组
     * @param defaultArray 默认数组
     * @return 数组本身或默认数组
     */
    public static <T, S extends T> T defaultIfEmptyArray(T array, S defaultValue) {
        return isEmptyArray(array) ? defaultValue : array;
    }

    // ==========================================================================
    // 将数组转换成集合类。                                                        
    // ==========================================================================

    /**
     * 将数组转换成<code>Iterable</code>列表。
     * <p>
     * 如果输入数组为<code>null</code>，则视作空数组。
     * </p>
     * <p>
     * 该方法返回的<code>Iterable</code>对象是轻量而高效的，不会产生复制数组的开销。你可以使用
     * <code>CollectionUtil.createArrayList(asIterable(componentType, array))</code>或
     * <code>CollectionUtil.createLinkedList(asIterable(componentType, array))</code>
     * 来进一步将 <code>Iterable</code>转换成指定类型的 <code>List</code>对象。
     * </p>
     * 
     * @param componentType <code>Iterable</code>元素的类型，必须和数组类型兼容。例如对于
     *            <code>int[]</code>数组， <code>componentType</code>必须为
     *            <code>Integer.class</code>。
     * @param array 要转换的数组
     * @return 被创建的<code>Iterable</code>对象
     */
    public static <T> Iterable<T> arrayAsIterable(final Class<T> componentType, Object array) {
        assertNotNull(componentType, "componentType");

        if (array == null) {
            return new ArrayIterable<T>(0) {
                @Override
                protected T get(int i) {
                    unreachableCode();
                    return null;
                }
            };
        } else if (array instanceof Object[]) {
            final Object[] objectArray = (Object[]) array;

            return new ArrayIterable<T>(objectArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(objectArray[i]);
                }
            };
        } else if (array instanceof int[]) {
            final int[] intArray = (int[]) array;

            return new ArrayIterable<T>(intArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(intArray[i]);
                }
            };
        } else if (array instanceof long[]) {
            final long[] longArray = (long[]) array;

            return new ArrayIterable<T>(longArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(longArray[i]);
                }
            };

        } else if (array instanceof short[]) {
            final short[] shortArray = (short[]) array;

            return new ArrayIterable<T>(shortArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(shortArray[i]);
                }
            };
        } else if (array instanceof byte[]) {
            final byte[] byteArray = (byte[]) array;

            return new ArrayIterable<T>(byteArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(byteArray[i]);
                }
            };
        } else if (array instanceof double[]) {
            final double[] doubleArray = (double[]) array;

            return new ArrayIterable<T>(doubleArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(doubleArray[i]);
                }
            };
        } else if (array instanceof float[]) {
            final float[] floatArray = (float[]) array;

            return new ArrayIterable<T>(floatArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(floatArray[i]);
                }
            };
        } else if (array instanceof boolean[]) {
            final boolean[] booleanArray = (boolean[]) array;

            return new ArrayIterable<T>(booleanArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(booleanArray[i]);
                }
            };
        } else if (array instanceof char[]) {
            final char[] charArray = (char[]) array;

            return new ArrayIterable<T>(charArray.length) {
                @Override
                protected T get(int i) {
                    return componentType.cast(charArray[i]);
                }
            };
        } else {
            throw new IllegalArgumentException(array + " is not an array");
        }
    }

    private static abstract class ArrayIterable<T> implements Iterable<T> {
        private final int length;

        public ArrayIterable(int length) {
            this.length = length;
        }

        public final Iterator<T> iterator() {
            return new Iterator<T>() {
                private int index;

                public final boolean hasNext() {
                    return index < length;
                }

                public final T next() {
                    if (index >= length) {
                        throw new ArrayIndexOutOfBoundsException(index);
                    }

                    return get(index++);
                }

                public final void remove() {
                    unsupportedOperation("remove");
                }
            };
        }

        protected abstract T get(int i);
    }

    /**
     * 将数组转换成<code>Map</code>。数组的元素必须是元素个数多于2的子数组。
     * 
     * <pre>
     * Map colorMap = ArrayUtil.toMap(new String[][] {{
     *     {"RED", 0xFF0000},
     *     {"GREEN", 0x00FF00},
     *     {"BLUE", 0x0000FF}}, String.class, Integer.class);
     * </pre>
     * 
     * @param keyValueArray 要转换的数组
     * @param keyType key的类型，数组元素<code>keyValueArray[n][0]</code>的类型必须与之兼容
     * @param valueType value的类型，数组元素<code>keyValueArray[n][1]</code>的类型必须与之兼容
     * @return 被创建的map
     */
    public static <K, V> Map<K, V> arrayToMap(Object[][] keyValueArray, Class<K> keyType, Class<V> valueType) {
        return arrayToMap(keyValueArray, keyType, valueType, null);
    }

    /**
     * 将数组转换成<code>Map</code>。数组的元素必须是元素个数多于2的子数组。
     * 
     * <pre>
     * Map colorMap = ArrayUtil.toMap(new String[][] {{
     *     {"RED", 0xFF0000},
     *     {"GREEN", 0x00FF00},
     *     {"BLUE", 0x0000FF}}, String.class, Integer.class, map);
     * </pre>
     * 
     * @param keyValueArray 要转换的数组
     * @param keyType key的类型，数组元素<code>keyValueArray[n][0]</code>的类型必须与之兼容
     * @param valueType value的类型，数组元素<code>keyValueArray[n][1]</code>的类型必须与之兼容
     * @param map 要填充的map，如果为<code>null</code>则自动创建之
     * @return 被创建或填充的map
     */
    public static <K, V> Map<K, V> arrayToMap(Object[][] keyValueArray, Class<K> keyType, Class<V> valueType,
                                              Map<K, V> map) {
        assertNotNull(keyType, "keyType");
        assertNotNull(valueType, "valueType");

        if (keyValueArray == null) {
            return map;
        }

        if (map == null) {
            map = createLinkedHashMap((int) (keyValueArray.length * 1.5));
        }

        for (int i = 0; i < keyValueArray.length; i++) {
            Object[] keyValue = keyValueArray[i];
            Object[] entry = keyValue;

            if (entry == null || entry.length < 2) {
                throw new IllegalArgumentException("Array element " + i + " is not an array of 2 elements");
            }

            map.put(keyType.cast(entry[0]), valueType.cast(entry[1]));
        }

        return map;
    }

    // ==========================================================================
    // 比较数组的长度。                                                            
    // ==========================================================================

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(Object[] array1, Object[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(long[] array1, long[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(int[] array1, int[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(short[] array1, short[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(byte[] array1, byte[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(double[] array1, double[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(float[] array1, float[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(boolean[] array1, boolean[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    /**
     * 判断两个数组是否具有相同的长度。如果数组为<code>null</code>则被看作长度为<code>0</code>。
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果两个数组长度相同，则返回<code>true</code>
     */
    public static boolean isArraySameLength(char[] array1, char[] array2) {
        int length1 = array1 == null ? 0 : array1.length;
        int length2 = array2 == null ? 0 : array2.length;

        return length1 == length2;
    }

    // ==========================================================================
    // 反转数组的元素顺序。                                                        
    // ==========================================================================

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(Object[] array) {
        if (array == null) {
            return;
        }

        Object tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(long[] array) {
        if (array == null) {
            return;
        }

        long tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(int[] array) {
        if (array == null) {
            return;
        }

        int tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(short[] array) {
        if (array == null) {
            return;
        }

        short tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(byte[] array) {
        if (array == null) {
            return;
        }

        byte tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(double[] array) {
        if (array == null) {
            return;
        }

        double tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(float[] array) {
        if (array == null) {
            return;
        }

        float tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(boolean[] array) {
        if (array == null) {
            return;
        }

        boolean tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * 反转数组的元素顺序。如果数组为<code>null</code>，则什么也不做。
     * 
     * @param array 要反转的数组
     */
    public static void arrayReverse(char[] array) {
        if (array == null) {
            return;
        }

        char tmp;

        for (int i = 0, j = array.length - 1; j > i; i++, j--) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：Object[]                                                              
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param objectToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(Object[] array, Object objectToFind) {
        return arrayIndexOf(array, objectToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(Object[] array, Object[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param objectToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(Object[] array, Object[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        Object first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && !ObjectUtil.isEquals(array[i], first)) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (!ObjectUtil.isEquals(array[j++], arrayToFind[k++])) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param objectToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(Object[] array, Object objectToFind) {
        return arrayLastIndexOf(array, objectToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(Object[] array, Object[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param objectToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        if (objectToFind == null) {
            for (int i = startIndex; i >= 0; i--) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i >= 0; i--) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(Object[] array, Object[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        Object last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && !ObjectUtil.isEquals(array[i], last)) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (!ObjectUtil.isEquals(array[j--], arrayToFind[k--])) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param objectToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(Object[] array, Object objectToFind) {
        return arrayIndexOf(array, objectToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(Object[] array, Object[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：long[]                                                                
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param longToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(long[] array, long longToFind) {
        return arrayIndexOf(array, longToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(long[] array, long[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param longToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(long[] array, long longToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        for (int i = startIndex; i < array.length; i++) {
            if (longToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(long[] array, long[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        long first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && array[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (array[j++] != arrayToFind[k++]) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param longToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(long[] array, long longToFind) {
        return arrayLastIndexOf(array, longToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(long[] array, long[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param longToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(long[] array, long longToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        for (int i = startIndex; i >= 0; i--) {
            if (longToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(long[] array, long[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        long last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && array[i] != last) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (array[j--] != arrayToFind[k--]) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param longToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(long[] array, long longToFind) {
        return arrayIndexOf(array, longToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(long[] array, long[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：int[]                                                                 
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param intToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(int[] array, int intToFind) {
        return arrayIndexOf(array, intToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(int[] array, int[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param intToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(int[] array, int intToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        for (int i = startIndex; i < array.length; i++) {
            if (intToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(int[] array, int[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && array[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (array[j++] != arrayToFind[k++]) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param intToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(int[] array, int intToFind) {
        return arrayLastIndexOf(array, intToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(int[] array, int[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param intToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(int[] array, int intToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        for (int i = startIndex; i >= 0; i--) {
            if (intToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(int[] array, int[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        int last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && array[i] != last) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (array[j--] != arrayToFind[k--]) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param intToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(int[] array, int intToFind) {
        return arrayIndexOf(array, intToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(int[] array, int[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：short[]                                                               
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param shortToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(short[] array, short shortToFind) {
        return arrayIndexOf(array, shortToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(short[] array, short[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param shortToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(short[] array, short shortToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        for (int i = startIndex; i < array.length; i++) {
            if (shortToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(short[] array, short[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        short first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && array[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (array[j++] != arrayToFind[k++]) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param shortToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(short[] array, short shortToFind) {
        return arrayLastIndexOf(array, shortToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(short[] array, short[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param shortToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(short[] array, short shortToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        for (int i = startIndex; i >= 0; i--) {
            if (shortToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(short[] array, short[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        short last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && array[i] != last) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (array[j--] != arrayToFind[k--]) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param shortToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(short[] array, short shortToFind) {
        return arrayIndexOf(array, shortToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(short[] array, short[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：byte[]                                                                
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param byteToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(byte[] array, byte byteToFind) {
        return arrayIndexOf(array, byteToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(byte[] array, byte[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param byteToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(byte[] array, byte byteToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        for (int i = startIndex; i < array.length; i++) {
            if (byteToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(byte[] array, byte[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        byte first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && array[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (array[j++] != arrayToFind[k++]) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param byteToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(byte[] array, byte byteToFind) {
        return arrayLastIndexOf(array, byteToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(byte[] array, byte[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param byteToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(byte[] array, byte byteToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        for (int i = startIndex; i >= 0; i--) {
            if (byteToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(byte[] array, byte[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        byte last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && array[i] != last) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (array[j--] != arrayToFind[k--]) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param byteToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(byte[] array, byte byteToFind) {
        return arrayIndexOf(array, byteToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(byte[] array, byte[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：double[]                                                              
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double doubleToFind) {
        return arrayIndexOf(array, doubleToFind, 0, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double doubleToFind, double tolerance) {
        return arrayIndexOf(array, doubleToFind, 0, tolerance);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double[] arrayToFind, double tolerance) {
        return arrayIndexOf(array, arrayToFind, 0, tolerance);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double doubleToFind, int startIndex) {
        return arrayIndexOf(array, doubleToFind, startIndex, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double doubleToFind, int startIndex, double tolerance) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        double min = doubleToFind - tolerance;
        double max = doubleToFind + tolerance;

        for (int i = startIndex; i < array.length; i++) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double[] arrayToFind, int startIndex) {
        return arrayIndexOf(array, arrayToFind, startIndex, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(double[] array, double[] arrayToFind, int startIndex, double tolerance) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        double firstMin = arrayToFind[0] - tolerance;
        double firstMax = arrayToFind[0] + tolerance;
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && (array[i] < firstMin || array[i] > firstMax)) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (Math.abs(array[j++] - arrayToFind[k++]) > tolerance) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double doubleToFind) {
        return arrayLastIndexOf(array, doubleToFind, Integer.MAX_VALUE, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double doubleToFind, double tolerance) {
        return arrayLastIndexOf(array, doubleToFind, Integer.MAX_VALUE, tolerance);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double[] arrayToFind, double tolerance) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE, tolerance);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double doubleToFind, int startIndex) {
        return arrayLastIndexOf(array, doubleToFind, startIndex, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double doubleToFind, int startIndex, double tolerance) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        double min = doubleToFind - tolerance;
        double max = doubleToFind + tolerance;

        for (int i = startIndex; i >= 0; i--) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double[] arrayToFind, int startIndex) {
        return arrayLastIndexOf(array, arrayToFind, startIndex, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(double[] array, double[] arrayToFind, int startIndex, double tolerance) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        double lastMin = arrayToFind[lastIndex] - tolerance;
        double lastMax = arrayToFind[lastIndex] + tolerance;
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && (array[i] < lastMin || array[i] > lastMax)) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (Math.abs(array[j--] - arrayToFind[k--]) > tolerance) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(double[] array, double doubleToFind) {
        return arrayIndexOf(array, doubleToFind) != -1;
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param doubleToFind 要查找的元素
     * @param tolerance 误差
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(double[] array, double doubleToFind, double tolerance) {
        return arrayIndexOf(array, doubleToFind, tolerance) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(double[] array, double[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param tolerance 误差
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(double[] array, double[] arrayToFind, double tolerance) {
        return arrayIndexOf(array, arrayToFind, tolerance) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：float[]                                                               
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float floatToFind) {
        return arrayIndexOf(array, floatToFind, 0, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float floatToFind, float tolerance) {
        return arrayIndexOf(array, floatToFind, 0, tolerance);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float[] arrayToFind, float tolerance) {
        return arrayIndexOf(array, arrayToFind, 0, tolerance);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float floatToFind, int startIndex) {
        return arrayIndexOf(array, floatToFind, startIndex, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float floatToFind, int startIndex, float tolerance) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        float min = floatToFind - tolerance;
        float max = floatToFind + tolerance;

        for (int i = startIndex; i < array.length; i++) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float[] arrayToFind, int startIndex) {
        return arrayIndexOf(array, arrayToFind, startIndex, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(float[] array, float[] arrayToFind, int startIndex, float tolerance) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        float firstMin = arrayToFind[0] - tolerance;
        float firstMax = arrayToFind[0] + tolerance;
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && (array[i] < firstMin || array[i] > firstMax)) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (Math.abs(array[j++] - arrayToFind[k++]) > tolerance) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float floatToFind) {
        return arrayLastIndexOf(array, floatToFind, Integer.MAX_VALUE, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float floatToFind, float tolerance) {
        return arrayLastIndexOf(array, floatToFind, Integer.MAX_VALUE, tolerance);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float[] arrayToFind, float tolerance) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE, tolerance);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float floatToFind, int startIndex) {
        return arrayLastIndexOf(array, floatToFind, startIndex, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float floatToFind, int startIndex, float tolerance) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        float min = floatToFind - tolerance;
        float max = floatToFind + tolerance;

        for (int i = startIndex; i >= 0; i--) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float[] arrayToFind, int startIndex) {
        return arrayLastIndexOf(array, arrayToFind, startIndex, 0);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @param tolerance 误差
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(float[] array, float[] arrayToFind, int startIndex, float tolerance) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        float lastMin = arrayToFind[lastIndex] - tolerance;
        float lastMax = arrayToFind[lastIndex] + tolerance;
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && (array[i] < lastMin || array[i] > lastMax)) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (Math.abs(array[j--] - arrayToFind[k--]) > tolerance) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(float[] array, float floatToFind) {
        return arrayIndexOf(array, floatToFind) != -1;
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param floatToFind 要查找的元素
     * @param tolerance 误差
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(float[] array, float floatToFind, float tolerance) {
        return arrayIndexOf(array, floatToFind, tolerance) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(float[] array, float[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param tolerance 误差
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(float[] array, float[] arrayToFind, float tolerance) {
        return arrayIndexOf(array, arrayToFind, tolerance) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：boolean[]                                                             
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param booleanToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(boolean[] array, boolean booleanToFind) {
        return arrayIndexOf(array, booleanToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(boolean[] array, boolean[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param booleanToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(boolean[] array, boolean booleanToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        for (int i = startIndex; i < array.length; i++) {
            if (booleanToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(boolean[] array, boolean[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        boolean first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && array[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (array[j++] != arrayToFind[k++]) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param booleanToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(boolean[] array, boolean booleanToFind) {
        return arrayLastIndexOf(array, booleanToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(boolean[] array, boolean[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param booleanToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(boolean[] array, boolean booleanToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        for (int i = startIndex; i >= 0; i--) {
            if (booleanToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(boolean[] array, boolean[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        boolean last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && array[i] != last) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (array[j--] != arrayToFind[k--]) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param booleanToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(boolean[] array, boolean booleanToFind) {
        return arrayIndexOf(array, booleanToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(boolean[] array, boolean[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }

    // ==========================================================================
    // 在数组中查找一个元素或一个元素序列。                                        
    //  
    // 类型：char[]                                                                
    // ==========================================================================

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param charToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(char[] array, char charToFind) {
        return arrayIndexOf(array, charToFind, 0);
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(char[] array, char[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind, 0);
    }

    /**
     * 在数组中查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param charToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(char[] array, char charToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        for (int i = startIndex; i < array.length; i++) {
            if (charToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则看作<code>0</code>，超出数组长度的起始索引则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayIndexOf(char[] array, char[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        if (startIndex >= sourceLength) {
            return targetLength == 0 ? sourceLength : -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        char first = arrayToFind[0];
        int i = startIndex;
        int max = sourceLength - targetLength;

        startSearchForFirst: while (true) {
            // 查找第一个元素
            while (i <= max && array[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            // 已经找到第一个元素，接着找
            int j = i + 1;
            int end = j + targetLength - 1;
            int k = 1;

            while (j < end) {
                if (array[j++] != arrayToFind[k++]) {
                    i++;

                    // 重新查找第一个元素
                    continue startSearchForFirst;
                }
            }

            // 找到了
            return i;
        }
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param charToFind 要查找的元素
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(char[] array, char charToFind) {
        return arrayLastIndexOf(array, charToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(char[] array, char[] arrayToFind) {
        return arrayLastIndexOf(array, arrayToFind, Integer.MAX_VALUE);
    }

    /**
     * 在数组中从末尾开始查找一个元素。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param charToFind 要查找的元素
     * @param startIndex 起始索引
     * @return 该元素在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(char[] array, char charToFind, int startIndex) {
        if (array == null) {
            return -1;
        }

        if (startIndex < 0) {
            return -1;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }

        for (int i = startIndex; i >= 0; i--) {
            if (charToFind == array[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中从末尾开始查找一个元素序列。
     * <p>
     * 如果未找到或数组为<code>null</code>则返回<code>-1</code>。
     * </p>
     * <p>
     * 起始索引小于<code>0</code>则返回<code>-1</code>，超出数组长度的起始索引则从数组末尾开始找。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @param startIndex 起始索引
     * @return 该元素序列在数组中的序号，如果数组为<code>null</code>或未找到，则返回<code>-1</code>。
     */
    public static int arrayLastIndexOf(char[] array, char[] arrayToFind, int startIndex) {
        if (array == null || arrayToFind == null) {
            return -1;
        }

        int sourceLength = array.length;
        int targetLength = arrayToFind.length;

        int rightIndex = sourceLength - targetLength;

        if (startIndex < 0) {
            return -1;
        }

        if (startIndex > rightIndex) {
            startIndex = rightIndex;
        }

        if (targetLength == 0) {
            return startIndex;
        }

        int lastIndex = targetLength - 1;
        char last = arrayToFind[lastIndex];
        int min = targetLength - 1;
        int i = min + startIndex;

        startSearchForLast: while (true) {
            while (i >= min && array[i] != last) {
                i--;
            }

            if (i < min) {
                return -1;
            }

            int j = i - 1;
            int start = j - (targetLength - 1);
            int k = lastIndex - 1;

            while (j > start) {
                if (array[j--] != arrayToFind[k--]) {
                    i--;
                    continue startSearchForLast;
                }
            }

            return start + 1;
        }
    }

    /**
     * 判断指定对象是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param charToFind 要查找的元素
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(char[] array, char charToFind) {
        return arrayIndexOf(array, charToFind) != -1;
    }

    /**
     * 判断指定元素序列是否存在于指定数组中。
     * <p>
     * 如果数组为<code>null</code>则返回<code>false</code>。
     * </p>
     * 
     * @param array 要扫描的数组
     * @param arrayToFind 要查找的元素序列
     * @return 如果找到则返回<code>true</code>
     */
    public static boolean arrayContains(char[] array, char[] arrayToFind) {
        return arrayIndexOf(array, arrayToFind) != -1;
    }
}
