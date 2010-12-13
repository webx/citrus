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
package com.alibaba.citrus.generictype;

import static com.alibaba.citrus.util.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * 代表一个类型的信息。<code>TypeInfo</code>是和Java {@link Type}相对应的，但比Java Types更易用。
 * <ul>
 * <li>{@link RawTypeInfo}和{@link Class}对应，但是不包括数组类型。</li>
 * <li>{@link ParameterizedTypeInfo}和{@link ParameterizedType}对应。</li>
 * <li>{@link TypeVariableInfo}和{@link TypeVariable}对应。</li>
 * <li>{@link WildcardTypeInfo}和{@link WildcardType}对应。</li>
 * <li>{@link ArrayTypeInfo}和{@link GenericArrayType}以及代表数组的{@link Class}对应。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface TypeInfo {
    /** 用来取得<code>TypeInfo</code>的工厂。 */
    Factory factory = Factory.newFactory();

    /** 类型{@link Object}的<code>TypeInfo</code>。 */
    RawTypeInfo OBJECT = (RawTypeInfo) factory.getType(Object.class);

    /** 基本类型{@link boolean}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_BOOLEAN = (RawTypeInfo) factory.getType(boolean.class);

    /** 基本类型{@link byte}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_BYTE = (RawTypeInfo) factory.getType(byte.class);

    /** 基本类型{@link char}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_CHAR = (RawTypeInfo) factory.getType(char.class);

    /** 基本类型{@link double}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_DOUBLE = (RawTypeInfo) factory.getType(double.class);

    /** 基本类型{@link float}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_FLOAT = (RawTypeInfo) factory.getType(float.class);

    /** 基本类型{@link int}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_INT = (RawTypeInfo) factory.getType(int.class);

    /** 基本类型{@link long}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_LONG = (RawTypeInfo) factory.getType(long.class);

    /** 基本类型{@link short}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_SHORT = (RawTypeInfo) factory.getType(short.class);

    /** 基本类型{@link void}的<code>TypeInfo</code>。 */
    RawTypeInfo PRIMITIVE_VOID = (RawTypeInfo) factory.getType(void.class);

    /**
     * 取得经过类型擦除（type erasure）之后的类型信息。
     * <p>
     * <table border="1">
     * <tr>
     * <th>TypeInfo</th>
     * <th>返回值</th>
     * <th>示例</th>
     * </tr>
     * <tr>
     * <td>{@link RawTypeInfo}</td>
     * <td>自身（<code>this</code>）</td>
     * <td><code>List</code> 将返回： <code>List</code></td>
     * </tr>
     * <tr>
     * <td>{@link ParameterizedTypeInfo}</td>
     * <td>raw类型</td>
     * <td><code>List&lt;Integer&gt;</code> 将返回： <code>List</code></td>
     * </tr>
     * <tr>
     * <td>{@link TypeVariableInfo}</td>
     * <td>第一个upper bound类型</td>
     * <td><code>&lt;E&gt;</code> 将返回： <code>Object</code><br>
     * <code>&lt;E extends Number & Comparable&gt;</code> 将返回：
     * <code>Number</code></td>
     * </tr>
     * <tr>
     * <td>{@link WildcardTypeInfo}</td>
     * <td>第一个upper bound类型</td>
     * <td><code>&lt;?&gt;</code> 将返回： <code>Object</code><br>
     * <code>&lt;? extends Number&gt;</code> 将返回： <code>Number</code></td>
     * </tr>
     * <tr>
     * <td>{@link ArrayTypeInfo}</td>
     * <td>rawType的类名</td>
     * <td><code>List&lt;Integer&gt;[][]</code> 将返回：<code>List[][]</code></td>
     * </tr>
     * </table>
     * </p>
     */
    Class<?> getRawType();

    /**
     * 取得类型的名称。
     * <p>
     * <table border="1">
     * <tr>
     * <th>TypeInfo</th>
     * <th>返回值</th>
     * <th>示例</th>
     * </tr>
     * <tr>
     * <td>{@link RawTypeInfo}</td>
     * <td>类名</td>
     * <td><code>java.util.List</code> 将返回： <code>"java.util.List"</code></td>
     * </tr>
     * <tr>
     * <td>{@link ParameterizedTypeInfo}</td>
     * <td>rawType的类名</td>
     * <td><code>java.util.List&lt;Integer&gt;</code> 将返回：
     * <code>"java.util.List"</code></td>
     * </tr>
     * <tr>
     * <td>{@link TypeVariableInfo}</td>
     * <td>变量名</td>
     * <td><code>&lt;E&gt;</code> 将返回： <code>"E"</code></td>
     * </tr>
     * <tr>
     * <td>{@link WildcardTypeInfo}</td>
     * <td>问号</td>
     * <td><code>&lt;? extends Object&gt;</code> 将返回： <code>"?"</code></td>
     * </tr>
     * <tr>
     * <td>{@link ArrayTypeInfo}</td>
     * <td>rawType的类名</td>
     * <td><code>java.util.List&lt;Integer&gt;[][]</code> 将返回：
     * <code>"[[Ljava.util.List;"</code></td>
     * </tr>
     * </table>
     * </p>
     */
    String getName();

    /**
     * 取得类型的简短名称。
     * <p>
     * <table border="1">
     * <tr>
     * <th>TypeInfo</th>
     * <th>返回值</th>
     * <th>示例</th>
     * </tr>
     * <tr>
     * <td>{@link RawTypeInfo}</td>
     * <td>类名</td>
     * <td><code>java.util.List</code> 将返回： <code>"List"</code></td>
     * </tr>
     * <tr>
     * <td>{@link ParameterizedTypeInfo}</td>
     * <td>rawType的类名</td>
     * <td><code>java.util.List&lt;Integer&gt;</code> 将返回： <code>"List"</code></td>
     * </tr>
     * <tr>
     * <td>{@link TypeVariableInfo}</td>
     * <td>变量名</td>
     * <td><code>&lt;E&gt;</code> 将返回： <code>"E"</code></td>
     * </tr>
     * <tr>
     * <td>{@link WildcardTypeInfo}</td>
     * <td>问号</td>
     * <td><code>&lt;? extends Object&gt;</code> 将返回： <code>"?"</code></td>
     * </tr>
     * <tr>
     * <td>{@link ArrayTypeInfo}</td>
     * <td>rawType的类名</td>
     * <td><code>java.util.List&lt;Integer&gt;[][]</code> 将返回：
     * <code>"List[][]"</code></td>
     * </tr>
     * </table>
     * </p>
     */
    String getSimpleName();

    /**
     * 判断当前类型是否为原子类型，例如：<code>int</code>、<code>boolean</code>等。
     */
    boolean isPrimitive();

    /**
     * 是否为数组？只有两种类型的{@link TypeInfo}才有可能是数组：
     * <ol>
     * <li>{@link ArrayTypeInfo} - 例如：<code>int[]</code>,
     * <code>String[][]</code>, <code>List&lt;Integer&gt;[]</code>等。</li>
     * <li>{@link WildcardTypeInfo} - 例如：<code>&lt;? extends int[]&gt;</code>,
     * <code>&lt;? extends String[][]&gt;</code>,
     * <code>&lt;? extends List&lt;Integer&gt;[]&gt;</code>等。</li>
     * </ol>
     */
    boolean isArray();

    /**
     * 判断当前类型是否为接口。
     */
    boolean isInterface();

    /**
     * 假如当前{@link TypeInfo}是primtive类型（如<code>int</code>），则返回其包装类型（如
     * <code>Integer</code>），否则返回<code>this</code>本身。
     */
    TypeInfo getPrimitiveWrapperType();

    /**
     * 取得数组元素的类型。
     * <p>
     * 对于多维数组，返回最终元素类型。例如：<code>int[][]</code>返回<code>int</code>。
     * </p>
     * <p>
     * 如果不是数组，则返回本身<code>this</code>。
     * </p>
     */
    TypeInfo getComponentType();

    /**
     * 取得直接的数组元素的类型。
     * <p>
     * 对于多维数组，返回上一级元素类型。例如：<code>int[][]</code>返回<code>int[]</code>。
     * </p>
     * <p>
     * 如果不是数组，则返回本身<code>this</code>。
     * </p>
     */
    TypeInfo getDirectComponentType();

    /**
     * 返回数组的维度，如果不是数组，则返回<code>0</code>。
     */
    int getDimension();

    /**
     * 取得所有的接口，如果当前类是接口的话，包括当前类。
     * 
     * @see #getSupertypes()
     */
    List<TypeInfo> getInterfaces();

    /**
     * 取得所有的基类，从当前类向上推，如果当前类不是接口的话，包括当前类。
     * 
     * @see #getSupertypes()
     */
    List<TypeInfo> getSuperclasses();

    /**
     * 取得所有的基类和接口，从当前类向上推，包括当前类。
     * <p>
     * 对于八个primitive类型和特殊类型<code>void</code>，它们没有任何父类。
     * </p>
     * <p>
     * 如果一个类型，既不是接口，也不是数组，则按如下次序列出该类型的父类及接口。 例如对于
     * <code>java.util.ArrayList</code>类型，将得到以下列表：（顺序为：本类、父类、父接口、Object类）
     * </p>
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
     * <p>
     * 对于一个接口类型，则按如下次序列出该类的父接口。 例如对于<code>java.util.List</code>
     * 类型，将得到如下列表：（顺序为：本接口、父接口、Object类）
     * </p>
     * <ol>
     * <li>本接口 - <code>java.util.List</code></li>
     * <li>父接口 - <code>java.util.Collection</code></li>
     * <li>父接口 - <code>java.util.Iterable</code></li>
     * <li>Object类 - <code>java.lang.Object</code></li>
     * </ol>
     * <p>
     * 对于一个数组，此方法返回一个列表，列出所有component类型的父类和接口的维数相同的数组类型。 例如：
     * <code>java.util.ArrayList[][]</code>
     * 将取得以下列表：（顺序为：本数组、父类数组、父接口数组、Object类数组、父接口数组、Object类数组、数组父接口、Object类）：
     * </p>
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
     * <li>父接口数组 - <code>java.lang.Cloneable[]</code></li>
     * <li>父接口数组 - <code>java.io.Serializable[]</code></li>
     * <li>Object类数组 - <code>java.lang.Object[]</code></li>
     * <li>数组父接口 - <code>java.lang.Cloneable</code></li>
     * <li>数组父接口 - <code>java.io.Serializable</code></li>
     * <li>Object类 - <code>java.lang.Object</code></li>
     * </ol>
     * <p>
     * 原子类型的数组也是类似。例如：<code>int[][]</code>将得到以下列表：
     * </p>
     * <ol>
     * <li><code>int[][]</code></li>
     * <li><code>java.lang.Cloneable[]</code></li>
     * <li><code>java.io.Serializable[]</code></li>
     * <li><code>Object[]</code></li>
     * <li><code>java.lang.Cloneable</code></li>
     * <li><code>java.io.Serializable</code></li>
     * <li><code>java.lang.Object</code></li>
     * </ol>
     */
    List<TypeInfo> getSupertypes();

    /**
     * 在所有基类和接口中，查找rawClass为<code>equivalentClass</code>类型的{@link TypeInfo}。
     * <p>
     * 例如，
     * <code>ArrayList&lt;Integer&gt;.getSupertype(List.class) =&gt; List&lt;Integer&gt;</code>
     * </p>
     */
    TypeInfo getSupertype(Class<?> equivalentClass);

    /**
     * 在指定上下文中分析实际类型。
     * <p>
     * 相当于{@link resolve(context, true)}。
     * </p>
     */
    TypeInfo resolve(GenericDeclarationInfo context);

    /**
     * 在指定上下文中分析实际类型。
     * <p>
     * 假如<code>context</code>为<code>List&lt;E=Integer&gt;</code>，<br>
     * 那么解析<code>List&lt;E&gt;</code>的结果为：<code>Integer</code>。
     * </p>
     * <p>
     * 如果<code>includeBaseType==false</code>，那么解析类型变量时，将不会取得其baseType。 例如：
     * </p>
     * 
     * <pre>
     * class MyClass&lt;A&gt; {
     *     List&lt;A&gt; listA;
     * }
     * 
     * interface Collection&lt;E&gt; extends Iterable&lt;E&gt; {
     * }
     * 
     * interface Iterable&lt;T&gt; {
     * }
     * </pre>
     * <p>
     * 那么，<code>Iterable&lt;T=E&gt;.resolve(List&lt;A&gt;)</code>，将返回
     * <code>Iterable&lt;T=A&gt;</code>。
     * </p>
     * <p>
     * 如果<code>includeBaseType==true</code>，那么，上述调用将返回
     * <code>Iterable&lt;T=Object&gt;</code>。
     * </p>
     */
    TypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType);

    /**
     * 用来创建<code>TypeInfo</code>的工厂。
     */
    abstract class Factory {
        /**
         * 取得指定{@link Type}对应的{@link TypeInfo}对象。
         */
        public abstract TypeInfo getType(Type type);

        /**
         * 取得指定非数组{@link Class}对应的{@link ClassTypeInfo}对象。
         * <p>
         * 本方法不接受数组类，如果为数组，则抛出<code>IllegalArgumentException</code>。
         * </p>
         * <p>
         * 本方法和{@link getType(Type)}是完全等同的，只是省去了一些cast而已。
         * </p>
         * <p>
         * 由于{@link ClassTypeInfo}也是{@link GenericDeclarationInfo}的子类，<br>
         * 因此当参数为{@link Class}时，本方法的结果和{@link
         * getGenericDeclaration(GenericDeclaration)}也等同。
         * </p>
         */
        public final ClassTypeInfo getClassType(Class<?> type) {
            assertTrue(type != null && !type.isArray(), "type should not be array: %s", type.getName());
            return (ClassTypeInfo) getType(type);
        }

        /**
         * 取得指定{@link ParameterizedType}对应的{@link ClassTypeInfo}对象。
         * <p>
         * 本方法和{@link getType(Type)}是完全等同的，只是免去了一些cast而已。
         * </p>
         */
        public final ClassTypeInfo getClassType(ParameterizedType type) {
            return (ClassTypeInfo) getType(type);
        }

        /**
         * 取得一组{@link TypeInfo}对象。
         */
        public abstract TypeInfo[] getTypes(Type[] types);

        /**
         * 取得指定{@link GenericDeclaration}对应的{@link GenericDeclarationInfo}对象。 *
         * <p>
         * 本方法不接受数组类，如果为数组，则抛出<code>IllegalArgumentException</code>。
         * </p>
         */
        public abstract GenericDeclarationInfo getGenericDeclaration(GenericDeclaration declaration);

        /**
         * 创建一个参数化类型。
         */
        public abstract ParameterizedTypeInfo getParameterizedType(TypeInfo type, TypeInfo... args);

        /**
         * 创建一个参数化类型。
         */
        public final ParameterizedTypeInfo getParameterizedType(TypeInfo type, Type... args) {
            return getParameterizedType(type, getTypes(args));
        }

        /**
         * 创建一个参数化类型。
         */
        public final ParameterizedTypeInfo getParameterizedType(Class<?> type, Type... args) {
            return getParameterizedType(getType(type), getTypes(args));
        }

        /**
         * 创建一个数组类型。
         */
        public abstract ArrayTypeInfo getArrayType(TypeInfo componentType, int dimension);

        /**
         * 创建一个数组类型。
         */
        public final ArrayTypeInfo getArrayType(Class<?> componentType, int dimension) {
            return getArrayType(getType(componentType), dimension);
        }

        /**
         * 取得指定{@link Method}对应的{@link MethodInfo}对象。
         * <p>
         * 本方法和{@link getGenericDeclaration(GenericDeclaration)}
         * 是完全等同的，只是免去了一些cast而已。
         * </p>
         */
        public final MethodInfo getMethod(Method method) {
            return (MethodInfo) getGenericDeclaration(method);
        }

        /**
         * 以指定类型作为上下文，取得指定{@link Method}对应的{@link MethodInfo}对象。
         */
        public final MethodInfo getMethod(Method method, TypeInfo type) {
            MethodInfo result = getMethod(method);

            if (type != null) {
                Class<?> rawType = type.getRawType();

                assertTrue(method.getDeclaringClass().isAssignableFrom(rawType),
                        "method \"%s\" does not belong to type \"%s\"", method, type);

                if (type instanceof ClassTypeInfo) {
                    result = result.resolve((ClassTypeInfo) type, false);
                }
            }

            return result;
        }

        /**
         * 取得指定{@link Constructor}对应的{@link MethodInfo}对象。
         * <p>
         * 本方法和{@link getGenericDeclaration(GenericDeclaration)}
         * 是完全等同的，只是免去了一些cast而已。
         * </p>
         */
        public final MethodInfo getConstructor(Constructor<?> constructor) {
            return (MethodInfo) getGenericDeclaration(constructor);
        }

        /**
         * 以指定类型作为上下文，取得指定{@link Constructor}对应的{@link MethodInfo}对象。
         */
        public final MethodInfo getConstructor(Constructor<?> constructor, TypeInfo type) {
            MethodInfo result = getConstructor(constructor);

            if (type != null) {
                Class<?> rawType = type.getRawType();

                assertTrue(constructor.getDeclaringClass().equals(rawType),
                        "constructor \"%s\" does not belong to type \"%s\"", constructor, type);

                if (type instanceof ClassTypeInfo) {
                    result = result.resolve((ClassTypeInfo) type, false);
                }
            }

            return result;
        }

        /**
         * 取得指定{@link Field}对应的{@link FieldInfo}对象。
         */
        public abstract FieldInfo getField(Field field);

        /**
         * 以指定类型作为上下文，取得指定{@link Field}对应的{@link FieldInfo}对象。
         */
        public final FieldInfo getField(Field field, TypeInfo type) {
            FieldInfo result = getField(field);

            if (type != null) {
                Class<?> rawType = type.getRawType();

                assertTrue(field.getDeclaringClass().isAssignableFrom(rawType),
                        "field \"%s\" does not belong to type \"%s\"", field, type);

                if (type instanceof ClassTypeInfo) {
                    result = result.resolve((ClassTypeInfo) type, false);
                }
            }

            return result;
        }

        /**
         * 创建factory，但避免在compile时刻依赖impl package。
         */
        private static Factory newFactory() {
            String factoryImplName = Factory.class.getPackage().getName() + ".impl.TypeInfoFactory";
            Factory factoryImpl = null;

            try {
                factoryImpl = (Factory) Factory.class.getClassLoader().loadClass(factoryImplName).newInstance();
            } catch (Exception e) {
                unexpectedException(e, "Failed to create TypeInfo.Factory");
            }

            return factoryImpl;
        }
    }
}
