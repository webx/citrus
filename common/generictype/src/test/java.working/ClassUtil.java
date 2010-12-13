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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.alibaba.citrus.util.internal.ArrayUtil;
import com.alibaba.citrus.util.internal.StringUtil;

/**
 * 有关类型的工具类。
 * 
 * @author Michael Zhou
 */
public class ClassUtil {
    private static final Map<String, PrimitiveInfo<?>> PRIMITIVES = createHashMap();

    static {
        PRIMITIVES.put("boolean", new PrimitiveInfo<Boolean>(boolean.class, "Z", Boolean.class, "booleanValue"));
        PRIMITIVES.put("short", new PrimitiveInfo<Short>(short.class, "S", Short.class, "shortValue"));
        PRIMITIVES.put("int", new PrimitiveInfo<Integer>(int.class, "I", Integer.class, "intValue"));
        PRIMITIVES.put("long", new PrimitiveInfo<Long>(long.class, "J", Long.class, "longValue"));
        PRIMITIVES.put("float", new PrimitiveInfo<Float>(float.class, "F", Float.class, "floatValue"));
        PRIMITIVES.put("double", new PrimitiveInfo<Double>(double.class, "D", Double.class, "doubleValue"));
        PRIMITIVES.put("char", new PrimitiveInfo<Character>(char.class, "C", Character.class, "charValue"));
        PRIMITIVES.put("byte", new PrimitiveInfo<Byte>(byte.class, "B", Byte.class, "byteValue"));
        PRIMITIVES.put("void", new PrimitiveInfo<Void>(void.class, "V", Void.class, null));
    }

    /**
     * 代表一个primitive类型的信息。
     */
    private static class PrimitiveInfo<T> {
        final Class<T> type;
        final String typeCode;
        final Class<T> wrapperType;
        final String unwrapMethod;

        public PrimitiveInfo(Class<T> type, String typeCode, Class<T> wrapperType, String unwrapMethod) {
            this.type = type;
            this.typeCode = typeCode;
            this.wrapperType = wrapperType;
            this.unwrapMethod = unwrapMethod;
        }
    }

    /**
     * 取得primitive类。
     * <p>
     * 例如：
     * 
     * <pre>
     * ClassUtil.getPrimitiveType(&quot;int&quot;) = int.class;
     * ClassUtil.getPrimitiveType(&quot;long&quot;) = long.class;
     * </pre>
     * 
     * </p>
     */
    public static Class<?> getPrimitiveType(String name) {
        PrimitiveInfo<?> info = PRIMITIVES.get(name);

        if (info != null) {
            return info.type;
        }

        return null;
    }

    /**
     * 取得primitive类型的wrapper。如果不是primitive，则原样返回。
     * <p>
     * 例如：
     * 
     * <pre>
     * ClassUtil.getPrimitiveWrapperType(int.class) = Integer.class;
     * ClassUtil.getPrimitiveWrapperType(int[].class) = int[].class;
     * ClassUtil.getPrimitiveWrapperType(int[][].class) = int[][].class;
     * ClassUtil.getPrimitiveWrapperType(String[][].class) = String[][].class;
     * </pre>
     * 
     * </p>
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getPrimitiveWrapperType(Class<T> type) {
        if (type.isPrimitive()) {
            return ((PrimitiveInfo<T>) PRIMITIVES.get(type.getName())).wrapperType;
        }

        return type;
    }

    /**
     * 代表Array的信息。
     */
    public static final class ArrayInfo {
        public final Class<?> componentType;
        public final int dimension;

        private ArrayInfo(Class<?> componentType, int dimension) {
            this.componentType = componentType;
            this.dimension = dimension;
        }
    }

    /**
     * 取得数组的维度，非数组则返回<code>0</code>。
     */
    public static ArrayInfo getArrayInfo(Class<?> arrayType) {
        assertNotNull(arrayType, "arrayType");

        int dimension = 0;

        while (arrayType.isArray()) {
            arrayType = arrayType.getComponentType();
            dimension++;
        }

        return new ArrayInfo(arrayType, dimension);
    }

    /**
     * 取得指定component类型和维度的数组类。
     */
    public static Class<?> getArrayType(Class<?> componentType, int dimension) {
        assertTrue(dimension >= 0, "dimension");

        if (dimension == 0) {
            return componentType;
        }

        return Array.newInstance(componentType, new int[dimension]).getClass();
    }

    /**
     * 取得JVM内部的类名。
     * <p>
     * 例如：
     * 
     * <pre>
     *  ClassUtil.getJVMClassName(&quot;int[]&quot;) = &quot;[I&quot;
     *  ClassUtil.getJVMClassName(&quot;java.lang.Integer[][]&quot;) = &quot;[[Ljava.lang.Integer;&quot;
     * </pre>
     * 
     * </p>
     * <p>
     * 该方法所返回的类名可用于 <code>Class.forName</code> 操作。
     * </p>
     */
    public static String getJVMClassName(String name) {
        return getJVMClassName(name, 0);
    }

    /**
     * 取得JVM内部的数组类名。
     * <p>
     * 例如：
     * 
     * <pre>
     *  ClassUtil.getJVMClassName(&quot;int&quot;, 1) = &quot;[I&quot;  // int[]
     *  ClassUtil.getJVMClassName(&quot;java.lang.Integer&quot;, 2) = &quot;[[Ljava.lang.Integer;&quot; // Integer[][]
     * 
     *  ClassUtil.getJVMClassName(&quot;int[]&quot;, 1) = &quot;[[I&quot;  // int[][]
     *  ClassUtil.getJVMClassName(&quot;java.lang.Integer[]&quot;, 1) = &quot;[[Ljava.lang.Integer;&quot; // Integer[][]
     * </pre>
     * 
     * </p>
     * <p>
     * 该方法所返回的类名可用于 <code>Class.forName</code> 操作。
     * </p>
     */
    public static String getJVMClassName(String name, int dimension) {
        assertTrue(dimension >= 0, "dimension");

        if (StringUtil.isEmpty(name)) {
            return name;
        }

        if (!name.endsWith("[]") && dimension == 0) {
            return name;
        }

        StringBuilder buffer = new StringBuilder();

        while (name.endsWith("[]")) {
            buffer.append("[");
            name = name.substring(0, name.length() - 2);
        }

        for (int i = 0; i < dimension; i++) {
            buffer.append("[");
        }

        PrimitiveInfo<?> pi = PRIMITIVES.get(name);

        if (pi != null) {
            buffer.append(pi.typeCode);
        } else {
            buffer.append("L");
            buffer.append(name);
            buffer.append(";");
        }

        return buffer.toString();
    }

    /**
     * 取得完整的类名。
     * <p>
     * 对于数组，将返回以“<code>[]</code>”结尾的名字。
     * </p>
     * <p>
     * 本方法和<code>Class.getCanonicalName()</code>的区别在于，本方法会保留inner类的
     * <code>$</code>符号。
     * </p>
     */
    public static String getJavaClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }

        return clazz.getName();
    }

    /**
     * 取得完整的类名。
     * <p>
     * 对于数组，将返回以“<code>[]</code>”结尾的名字。
     * </p>
     * <p>
     * 本方法和<code>Class.getSimpleName()</code>的区别在于，本方法会保留inner类的<code>$</code>
     * 符号。
     * </p>
     */
    public static String getSimpleJavaClassName(Class<?> clazz) {
        String className = getJavaClassName(clazz);

        return className.substring(className.lastIndexOf(".") + 1);
    }

    /**
     * 判断方法是不是<code>String toString()</code>方法。
     */
    public static boolean isToString(Method method) {
        return isToString(new MethodSignature(method));
    }

    /**
     * 判断方法是不是<code>String toString()</code>方法。
     */
    public static boolean isToString(MethodSignature method) {
        if (!"toString".equals(method.getName())) {
            return false;
        }

        if (method.getParameterTypes().length > 0) {
            return false;
        }

        return String.class == method.getReturnType();
    }

    /**
     * 取得指定类的所有父类和接口。
     * <p>
     * 对于一个<code>Class</code>对象，如果它即不是接口，也不是数组，则按如下次序列出该类的父类及接口。
     * </p>
     * <p>
     * 例如<code>ClassUtil.getSupertypes(java.util.ArrayList.class)</code>返回以下列表：
     * （顺序为：本类、父类、父接口、Object类）
     * <ol>
     * <li>本类 - <code>java.util.ArrayList</code></li>
     * <li>父类 - <code>java.util.AbstractList</code></li>
     * <li>父类 - <code>java.util.AbstractCollection</code></li>
     * <li>父接口 - <code>java.util.List</code></li>
     * <li>父接口 - <code>java.util.Collection</code></li>
     * <li>父接口 - <code>java.util.RandomAccess</code></li>
     * <li>父接口 - <code>java.lang.Cloneable</code></li>
     * <li>父接口 - <code>java.io.Serializable</code></li>
     * <li>父接口 - <code>java.io.Iterable</code></li>
     * <li>Object类 - <code>java.lang.Object</code></li>
     * </ol>
     * </p>
     * <p>
     * 对于一个<code>Class</code>对象，如果它是接口，则按如下次序列出该类的父接口。
     * </p>
     * <p>
     * 例如<code>ClassUtil.getSupertypes(java.util.List.class)</code>将返回如下列表：
     * （顺序为：本接口、父接口、Object类）
     * </p>
     * <ol>
     * <li>本接口 - <code>java.util.List</code></li>
     * <li>父接口 - <code>java.util.Collection</code></li>
     * <li>父接口 - <code>java.util.Iterable</code></li>
     * <li>Object类 - <code>java.lang.Object</code></li>
     * </ol>
     * <p>
     * 对于一个数组，此方法返回一个列表，列出所有component类型的父类和接口的维数相同的数组类型。 例如：
     * <code>ClassUtil.getSupertypes(java.util.ArrayList[][].class)</code>
     * 返回以下列表：（顺序为：本数组、父类数组、父接口数组、Object类数组、数组父类、数组父接口、Object类）
     * <ol>
     * <li>本数组 - <code>java.util.ArrayList[][]</code></li>
     * <li>父类数组 - <code>java.util.AbstractList[][]</code></li>
     * <li>父类数组 - <code>java.util.AbstractCollection[][]</code></li>
     * <li>父接口数组 - <code>java.util.List[][]</code></li>
     * <li>父接口数组 - <code>java.util.Collection[][]</code></li>
     * <li>父接口数组 - <code>java.util.RandomAccess[][]</code></li>
     * <li>父接口数组 - <code>java.lang.Cloneable[][]</code></li>
     * <li>父接口数组 - <code>java.io.Serializable[][]</code></li>
     * <li>父接口数组 - <code>java.io.Iterable[][]</code></li>
     * <li>Object类数组 - <code>java.lang.Object[][]</code></li>
     * <li>数组父类 - <code>java.lang.Object[]</code></li>
     * <li>数组父接口 - <code>java.lang.Cloneable</code></li>
     * <li>数组父接口 - <code>java.io.Serializable</code></li>
     * <li>Object类 - <code>java.lang.Object</code></li>
     * </ol>
     * 特殊类型<code>void</code>、<code>Void</code>没有任何父类。
     * <ol>
     * <li><code>java.lang.Void</code></li>
     * </ol>
     * 最后，原子类型将会被转换成包装类。 例如：<code>ClassUtil.getSupertypes(int.class)</code>
     * 返回以下列表：
     * <ol>
     * <li><code>java.lang.Integer</code></li>
     * <li><code>java.lang.Number</code></li>
     * <li><code>java.lang.Comparable</code></li>
     * <li><code>java.io.Serializable</code></li>
     * <li><code>java.lang.Object</code></li>
     * </ol>
     * 但是原子类型的数组并不会被转成包装类。例如：<code>ClassUtil.getSupertypes(int[][].class)</code>
     * 返回以下列表：
     * <ol>
     * <li><code>int[][]</code></li>
     * <li><code>Object[]</code></li>
     * <li><code>java.lang.Comparable</code></li>
     * <li><code>java.io.Serializable</code></li>
     * <li><code>java.lang.Object</code></li>
     * </ol>
     * </p>
     */
    public static Iterable<Class<?>> getSupertypes(Class<?> clazz) {
        return new Supertypes(clazz);
    }

    /**
     * 遍历所有父类和接口。
     */
    private static class Supertypes implements Iterable<Class<?>> {
        private Class<?> clazz;

        public Supertypes(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Iterator<Class<?>> iterator() {
            return new SupertypeIterator(clazz);
        }
    }

    /**
     * 遍历所有父类和接口的遍历器。
     */
    private static class SupertypeIterator implements Iterator<Class<?>> {
        private static enum State {
            CLASSES,
            INTERFACES,
            ARRAYS,
            END
        }

        private final Set<Class<?>> processedInterfaces = createHashSet();
        private final LinkedList<Class<?>> interfaceQueue = createLinkedList();

        private Class<?> clazz;
        private int dimension;
        private Iterator<Class<?>> componentTypes;

        private State state;

        public SupertypeIterator(Class<?> clazz) {
            this(clazz, true);
        }

        public SupertypeIterator(Class<?> clazz, boolean convertPrimitive) {
            assertNotNull(clazz, "clazz");

            clazz = convertPrimitive ? getPrimitiveWrapperType(clazz) : clazz;

            queueInterfaces(clazz);

            // 是否为数组？
            ArrayInfo ai = getArrayInfo(clazz);

            this.clazz = clazz = ai.componentType;
            this.dimension = ai.dimension;

            // 设置初始状态
            if (dimension > 0) {
                componentTypes = new SupertypeIterator(clazz, false);
                state = State.ARRAYS;
            } else if (clazz.isInterface() || clazz == Object.class) {
                state = State.INTERFACES;
            } else {
                state = State.CLASSES;
            }
        }

        public boolean hasNext() {
            return state != State.END;
        }

        public Class<?> next() {
            Class<?> result;

            switch (state) {
                case ARRAYS:
                    result = getArrayType(componentTypes.next(), dimension);

                    if (!componentTypes.hasNext()) {
                        if (--dimension > 0) {
                            state = State.CLASSES;
                            clazz = Object.class;
                        } else {
                            state = State.INTERFACES;
                        }
                    }

                    break;

                case CLASSES:
                    if (dimension > 0) {
                        result = getArrayType(clazz, dimension);

                        if (--dimension == 0) {
                            state = State.INTERFACES;
                        }
                    } else {
                        result = clazz;

                        if (clazz == Void.class) {
                            clazz = null;
                        } else {
                            clazz = clazz.getSuperclass();
                        }

                        if (clazz == null) {
                            if (interfaceQueue.isEmpty()) {
                                state = State.END;
                            }
                        } else {
                            queueInterfaces(clazz);

                            if (clazz == Object.class) {
                                state = State.INTERFACES;
                            }
                        }
                    }

                    break;

                case INTERFACES:
                    if (interfaceQueue.isEmpty()) {
                        state = State.END;
                        result = Object.class;
                    } else {
                        result = interfaceQueue.removeFirst();
                        queueInterfaces(result);
                    }

                    break;

                default:
                    throw new NoSuchElementException();
            }

            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void queueInterfaces(Class<?> clazz) {
            if (clazz.isInterface() && !processedInterfaces.contains(clazz)) {
                interfaceQueue.addLast(clazz);
                processedInterfaces.add(clazz);
            }

            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                if (!processedInterfaces.contains(interfaceClass)) {
                    interfaceQueue.addLast(interfaceClass);
                    processedInterfaces.add(interfaceClass);
                }
            }
        }

    }

    /**
     * 取得类名对应的资源名。
     */
    public static String getResourceNameOfClass(Class<?> clazz) {
        String className = clazz == null ? null : clazz.getName();

        return getResourceNameOfClass(className);
    }

    /**
     * 取得类名对应的资源名。
     */
    public static String getResourceNameOfClass(String className) {
        if (className == null) {
            return null;
        }

        return className.trim().replace('.', '/');
    }

    /**
     * 取得指定类或接口的所有<code>public</code>方法签名。
     * <p>
     * 该方法返回的签名是被排序的。
     * </p>
     */
    public static Map<MethodSignature, Class<?>> getMethodSignatures(Class<?>... classes)
            throws IncompatibleMethodSignatureException {
        class Tuple implements Comparable<Tuple> {
            public final MethodSignature signature;
            public final Class<?> declaringClass;

            public Tuple(MethodSignature signature, Class<?> declaringClass) {
                this.signature = signature;
                this.declaringClass = declaringClass;
            }

            public int compareTo(Tuple other) {
                return signature.compareTo(other.signature);
            }
        }

        Map<MethodSignature, Tuple> set = createHashMap();

        for (Class<?> clazz : classes) {
            for (Method method : clazz.getMethods()) {
                MethodSignature signature = new MethodSignature(method);
                Tuple existing = set.get(signature);

                if (existing == null) {
                    set.put(signature, new Tuple(signature, method.getDeclaringClass()));
                } else {
                    if (existing.signature.isOverridingSignatureOf(signature)) {
                        set.put(signature, new Tuple(signature, method.getDeclaringClass()));
                    } else if (!signature.isOverridingSignatureOf(existing.signature)) {
                        throw new IncompatibleMethodSignatureException(incompatibleMethodSignaturesDetected(
                                existing.signature, signature));
                    }
                }
            }
        }

        List<Tuple> tuples = createArrayList(set.size());

        tuples.addAll(set.values());

        Collections.sort(tuples);

        Map<MethodSignature, Class<?>> signatures = createLinkedHashMap();

        for (Tuple tuple : tuples) {
            signatures.put(tuple.signature, tuple.declaringClass);
        }

        return signatures;
    }

    /**
     * 调用构造函数创建对象。
     */
    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, null, null);
    }

    /**
     * 调用构造函数创建对象。
     */
    public static <T> T newInstance(Class<T> clazz, Class<?>[] paramTypes, Object[] paramValues) {
        try {
            if (ArrayUtil.isEmpty(paramTypes)) {
                return clazz.newInstance();
            }

            Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);

            return constructor.newInstance(paramValues);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                unexpectedException(e.getCause());
                return null;
            }
        } catch (Exception e) {
            unexpectedException(e);
            return null;
        }
    }
}
