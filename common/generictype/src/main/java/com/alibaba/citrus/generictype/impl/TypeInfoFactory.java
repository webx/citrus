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
package com.alibaba.citrus.generictype.impl;

import static com.alibaba.citrus.generictype.TypeInfo.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.generictype.ArrayTypeInfo;
import com.alibaba.citrus.generictype.BoundedTypeInfo;
import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.FieldInfo;
import com.alibaba.citrus.generictype.FieldNotFoundException;
import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.MethodInfo;
import com.alibaba.citrus.generictype.MethodNotFoundException;
import com.alibaba.citrus.generictype.ParameterizedTypeInfo;
import com.alibaba.citrus.generictype.RawTypeInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.TypeVariableInfo;
import com.alibaba.citrus.generictype.WildcardTypeInfo;
import com.alibaba.citrus.util.ClassUtil;

/**
 * 用来创建{@link TypeInfo}的工厂。
 * 
 * @author Michael Zhou
 */
public class TypeInfoFactory extends TypeInfo.Factory {
    private final Map<Class<?>, TypeInfo> classCache = createConcurrentHashMap(); // XXX! use weak ref instead
    private static Logger log = LoggerFactory.getLogger(TypeInfo.class);

    /**
     * 取得指定{@link Type}对应的{@link TypeInfo}对象。
     */
    @Override
    public final TypeInfo getType(Type type) {
        return buildType(type, null);
    }

    /**
     * 取得一组{@link TypeInfo}对象。
     */
    @Override
    public final TypeInfo[] getTypes(Type[] types) {
        return buildTypes(types, null);
    }

    /**
     * 取得指定{@link GenericDeclaration}对应的{@link GenericDeclarationInfo}对象。
     */
    @Override
    public final GenericDeclarationInfo getGenericDeclaration(GenericDeclaration declaration) {
        return buildGenericDeclaration(declaration, null);
    }

    /**
     * 取得指定{@link Field}对应的{@link FieldInfo}对象。
     */
    @Override
    public final FieldInfo getField(Field field) {
        ClassTypeInfo declaringType = getClassType(field.getDeclaringClass());
        TypeInfo type = getType(field.getGenericType());

        return new FieldImpl(field, declaringType, type);
    }

    /**
     * 创建一个参数化类型。
     */
    @Override
    public final ParameterizedTypeInfo getParameterizedType(TypeInfo type, TypeInfo... args) {
        assertTrue(type instanceof RawTypeInfo, "type should be RawTypeInfo");

        ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl((RawTypeInfo) type);

        parameterizedType.init(args);

        return parameterizedType;
    }

    /**
     * 创建一个数组类型。
     */
    @Override
    public final ArrayTypeInfo getArrayType(TypeInfo componentType, int dimension) {
        assertTrue(componentType instanceof RawTypeInfo || componentType instanceof ParameterizedTypeInfo
                || componentType instanceof TypeVariableInfo, "unsupported componentType: %s", componentType);
        assertTrue(dimension > 0, "dimension");

        ArrayTypeInfo arrayType;
        TypeInfo directComponentType = dimension == 1 ? componentType : getArrayType(componentType, dimension - 1);

        if (componentType instanceof RawTypeInfo) {
            Class<?> type = ClassUtil.getArrayClass(componentType.getRawType(), dimension);

            arrayType = (ArrayTypeInfo) getFromClassCache(type);

            if (arrayType == null) {
                arrayType = new ArrayTypeImpl(componentType, directComponentType, dimension, type);
                saveToClassCache(type, arrayType);
            }
        } else {
            arrayType = new ArrayTypeImpl(componentType, directComponentType, dimension, null);
        }

        return arrayType;
    }

    /**
     * 从<code>java.lang.reflect.Type</code>创建<code>TypeInfo</code>。
     */
    private TypeInfo buildType(Type type, BuildingCache buildingCache) {
        assertNotNull(type, "type");

        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;

            if (clazz.isArray()) {
                return buildArrayType(clazz, buildingCache);
            } else {
                return buildRawType((Class<?>) type, buildingCache);
            }
        } else if (type instanceof GenericArrayType) {
            return buildArrayType((GenericArrayType) type, buildingCache);
        } else if (type instanceof ParameterizedType) {
            return buildParameterizedType((ParameterizedType) type, buildingCache);
        } else if (type instanceof TypeVariable<?>) {
            return buildTypeVariable((TypeVariable<?>) type, buildingCache);
        } else if (type instanceof WildcardType) {
            return buildWildcardType((WildcardType) type, buildingCache);
        }

        unreachableCode("Unknown type: %s", type);

        return null;
    }

    /**
     * 创建一组<code>TypeInfo</code>。
     */
    private TypeInfo[] buildTypes(Type[] types, BuildingCache buildingCache) {
        if (isEmptyArray(types)) {
            return new TypeInfo[0];
        }

        if (buildingCache == null) {
            buildingCache = new BuildingCache();
        }

        TypeInfo[] typeInfos = new TypeInfo[types.length];

        for (int i = 0; i < types.length; i++) {
            typeInfos[i] = buildType(types[i], buildingCache);
        }

        return typeInfos;
    }

    /**
     * 从<code>java.lang.reflect.GenericDeclaration</code>创建
     * <code>GenericDeclarationInfo</code>。
     */
    private GenericDeclarationInfo buildGenericDeclaration(GenericDeclaration declaration, BuildingCache buildingCache) {
        assertNotNull(declaration, "declaration");

        if (declaration instanceof Class<?>) {
            Class<?> clazz = (Class<?>) declaration;
            assertTrue(!clazz.isArray(), "declaration should not be array class: %s", clazz.getName());
            return buildRawType(clazz, buildingCache);
        } else if (declaration instanceof Method) {
            return buildMethod((Method) declaration, buildingCache);
        } else if (declaration instanceof Constructor<?>) {
            return buildConstructor((Constructor<?>) declaration, buildingCache);
        } else {
            unreachableCode("Unknown generic declaration: %s", declaration);
            return null;
        }
    }

    /**
     * 从非数组<code>Class<?></code>中创建<code>RawTypeInfo</code>。
     */
    private RawTypeInfo buildRawType(Class<?> type, BuildingCache buildingCache) {
        RawTypeImpl rawType;

        // 先查找building cache，假如当前类型已经在build了，则直接返回之。
        if (buildingCache != null) {
            rawType = buildingCache.getRawType(type);

            if (rawType != null) {
                return rawType;
            }
        }

        // 否则，查看永久cache，防止重复创建。
        rawType = (RawTypeImpl) getFromClassCache(type);

        if (rawType == null) {
            log.debug("Buiding type info: {}", type);

            rawType = new RawTypeImpl(type);
            rawType.init(buildTypeVariables(type, rawType, buildingCache));

            saveToClassCache(type, rawType);
        }

        return rawType;
    }

    /**
     * 创建type parameters，如果有的话
     */
    private TypeVariableInfo[] buildTypeVariables(GenericDeclaration declaration, GenericDeclarationInfo declInfo,
                                                  BuildingCache buildingCache) {
        TypeVariable<?>[] params = declaration.getTypeParameters();
        TypeVariableInfo[] vars = new TypeVariableInfo[params.length];

        if (buildingCache == null && params.length > 0) {
            buildingCache = new BuildingCache();
        }

        if (buildingCache != null) {
            buildingCache.setGenericDeclaration(declaration, declInfo);
        }

        for (int i = 0; i < params.length; i++) {
            vars[i] = buildTypeVariable(params[i], buildingCache);
        }

        return vars;
    }

    /**
     * 从数组<code>Class<?></code>中创建<code>ArrayTypeInfo</code>。
     */
    private ArrayTypeInfo buildArrayType(Class<?> type, BuildingCache buildingCache) {
        ArrayTypeImpl arrayType = (ArrayTypeImpl) getFromClassCache(type);

        if (arrayType == null) {
            int dimension = 0;
            Class<?> componentClass = type;
            Class<?> directComponentClass = null;

            while (componentClass.isArray()) {
                componentClass = componentClass.getComponentType();
                dimension++;

                if (directComponentClass == null) {
                    directComponentClass = componentClass;
                }
            }

            TypeInfo componentType = buildRawType(componentClass, buildingCache);
            TypeInfo directComponentType = componentClass.equals(directComponentClass) ? componentType : buildType(
                    directComponentClass, buildingCache);

            arrayType = new ArrayTypeImpl(componentType, directComponentType, dimension, type);
            saveToClassCache(type, arrayType);
        }

        return arrayType;
    }

    /**
     * 从数组<code>GenericArrayType</code>中创建<code>ArrayTypeInfo</code>。
     */
    private ArrayTypeInfo buildArrayType(GenericArrayType type, BuildingCache buildingCache) {
        int dimension = 0;
        Type component = type;
        Type directComponent = null;

        // GenericArrayType的成员类型，只可能是三种：Class，TypeVariable和GenericArrayType
        for (;;) {
            if (component instanceof Class<?>) {
                if (((Class<?>) component).isArray()) {
                    component = ((Class<?>) component).getComponentType();
                } else {
                    break;
                }
            } else if (component instanceof GenericArrayType) {
                component = ((GenericArrayType) component).getGenericComponentType();
            } else {
                break;
            }

            dimension++;

            if (directComponent == null) {
                directComponent = component;
            }
        }

        TypeInfo componentType = buildType(component, buildingCache);
        TypeInfo directComponentType = component.equals(directComponent) ? componentType : buildType(directComponent,
                buildingCache);

        return new ArrayTypeImpl(componentType, directComponentType, dimension, null);
    }

    /**
     * 从<code>ParameterizedType</code>中创建<code>ParameterizedTypeInfo</code>。
     */
    private ParameterizedTypeInfo buildParameterizedType(ParameterizedType type, BuildingCache buildingCache) {
        if (buildingCache == null) {
            buildingCache = new BuildingCache();
        }

        ParameterizedTypeImpl parameterizedTypeInfo = buildingCache.getParameterizedType(type);

        if (parameterizedTypeInfo == null) {
            RawTypeInfo rawType = buildRawType((Class<?>) type.getRawType(), buildingCache);

            parameterizedTypeInfo = new ParameterizedTypeImpl(rawType);
            buildingCache.setParameterizedType(type, parameterizedTypeInfo);

            // 取得actual type arguments
            TypeInfo[] args = buildTypes(type.getActualTypeArguments(), buildingCache);

            // 修正wildcard的upper bounds为对应var的upper bounds
            // 例如，var为<T extends Number>，而wildcard未指定upper bounds，那么修正wildcard的upper bounds为Number
            for (int i = 0; i < args.length; i++) {
                TypeInfo arg = args[i];

                if (arg instanceof WildcardTypeInfo && ((WildcardTypeInfo) arg).isUnknown()) {
                    TypeVariable<?> var = rawType.getRawType().getTypeParameters()[i];
                    TypeInfo[] upperBounds = buildTypes(var.getBounds(), buildingCache);

                    args[i] = new UnknownWildcardTypeImpl((WildcardTypeInfo) arg, upperBounds);
                }
            }

            parameterizedTypeInfo.init(args);
        }

        return parameterizedTypeInfo;
    }

    /**
     * 从<code>TypeVariable</code>中创建<code>TypeVariableInfo</code>。
     */
    private TypeVariableInfo buildTypeVariable(TypeVariable<?> type, BuildingCache buildingCache) {
        if (buildingCache == null) {
            buildingCache = new BuildingCache();
        }

        String name = type.getName();
        GenericDeclarationInfo declaration = buildGenericDeclaration(type.getGenericDeclaration(), buildingCache);
        TypeInfo[] upperBounds = buildTypes(type.getBounds(), buildingCache);

        return new TypeVariableImpl(name, declaration, upperBounds);
    }

    /**
     * 从<code>WildcardType</code>中创建<code>WildcardTypeInfo</code>。
     */
    private WildcardTypeInfo buildWildcardType(WildcardType type, BuildingCache buildingCache) {
        if (buildingCache == null) {
            buildingCache = new BuildingCache();
        }

        TypeInfo[] upperBounds = buildTypes(type.getUpperBounds(), buildingCache);
        TypeInfo[] lowerBounds = buildTypes(type.getLowerBounds(), buildingCache);

        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }

    private MethodInfo buildMethod(Method method, BuildingCache buildingCache) {
        if (buildingCache == null) {
            buildingCache = new BuildingCache();
        }

        MethodImpl methodInfo = buildingCache.getMethod(method);

        if (methodInfo == null) {
            methodInfo = new MethodImpl(method);
            buildingCache.setGenericDeclaration(method, methodInfo);

            buildMethodOrConstructor(methodInfo, method.getGenericReturnType(), method.getGenericParameterTypes(),
                    method.getGenericExceptionTypes(), method.getDeclaringClass(), buildingCache);
        }

        return methodInfo;
    }

    private MethodInfo buildConstructor(Constructor<?> constructor, BuildingCache buildingCache) {
        if (buildingCache == null) {
            buildingCache = new BuildingCache();
        }

        MethodImpl methodInfo = buildingCache.getConstructor(constructor);

        if (methodInfo == null) {
            methodInfo = new MethodImpl(constructor);
            buildingCache.setGenericDeclaration(constructor, methodInfo);

            buildMethodOrConstructor(methodInfo, null, constructor.getGenericParameterTypes(),
                    constructor.getGenericExceptionTypes(), constructor.getDeclaringClass(), buildingCache);
        }

        return methodInfo;
    }

    private void buildMethodOrConstructor(MethodImpl method, Type returnType, Type[] parameterTypes,
                                          Type[] exceptionTypes, Class<?> declaringClass, BuildingCache buildingCache) {
        TypeVariableInfo[] vars = buildTypeVariables(method.declaration, method, buildingCache);
        TypeInfo returnTypeInfo = returnType == null ? TypeInfo.PRIMITIVE_VOID : buildType(returnType, buildingCache);
        TypeInfo[] parameterTypeInfos = buildTypes(parameterTypes, buildingCache);
        TypeInfo[] exceptionTypeInfos = buildTypes(exceptionTypes, buildingCache);
        ClassTypeInfo declaringType = buildRawType(declaringClass, buildingCache);

        method.init(vars, returnTypeInfo, parameterTypeInfos, exceptionTypeInfos, declaringType);
    }

    private TypeInfo getFromClassCache(Class<?> type) {
        return classCache.get(type);
    }

    private void saveToClassCache(Class<?> type, TypeInfo typeInfo) {
        classCache.put(type, typeInfo);
    }

    /**
     * 缓存{@link ParameterizedTypeInfo}、{@link Method}和{@link Constructor}。
     */
    private static class BuildingCache extends HashMap<Object, GenericDeclarationInfo> {
        private static final long serialVersionUID = -2169655543193524884L;

        public RawTypeImpl getRawType(Class<?> type) {
            return (RawTypeImpl) super.get(type);
        }

        public MethodImpl getMethod(Method method) {
            return (MethodImpl) super.get(method);
        }

        public MethodImpl getConstructor(Constructor<?> constructor) {
            return (MethodImpl) super.get(constructor);
        }

        public ParameterizedTypeImpl getParameterizedType(ParameterizedType type) {
            return (ParameterizedTypeImpl) super.get(type);
        }

        public void setGenericDeclaration(GenericDeclaration decl, GenericDeclarationInfo declInfo) {
            super.put(decl, declInfo);
        }

        public void setParameterizedType(ParameterizedType type, ParameterizedTypeImpl typeInfo) {
            super.put(type, typeInfo);
        }
    }

    /**
     * 在super类型中查找等同于指定类的类型。
     */
    static TypeInfo findSupertype(TypeInfo type, Class<?> equivalentClass) {
        for (TypeInfo supertype : type.getSupertypes()) {
            if (supertype.getRawType().equals(equivalentClass)) {
                return supertype;
            }
        }

        return null;
    }

    /**
     * 在context中查找类型变量对应的实际类型。
     */
    static TypeInfo resolveTypeVariable(TypeVariableInfo var, GenericDeclarationInfo context, boolean includeBaseType) {
        GenericDeclarationInfo declaration = assertNotNull(var, "var").getGenericDeclaration();
        TypeInfo result = null;

        // 当前var的declaration可能是： Class，带类型参数的method，带类型参数的constructor。
        //
        // 情形1. delcaration是Class，context也是Class，则在context.
        // supertypes中查找和declaration匹配的type
        if (declaration instanceof ClassTypeInfo && context instanceof ClassTypeInfo) {
            TypeInfo declarationEquivalent = findSupertype((ClassTypeInfo) context,
                    ((ClassTypeInfo) declaration).getRawType());

            if (declarationEquivalent != null) {
                if (!includeBaseType && declarationEquivalent instanceof RawTypeInfo) {
                    result = var;
                } else {
                    TypeInfo declarationResolved = declarationEquivalent.resolve(context, includeBaseType);

                    assertTrue(declarationResolved instanceof GenericDeclarationInfo,
                            "Unexpected declarationResolved: %s", declarationResolved);

                    result = ((GenericDeclarationInfo) declarationResolved).getActualTypeArgument(var.getName());
                }
            }
        }

        // 情形2. declaration是Method，context也是method
        // 但目前MethodInfo没有实现ParameterizedMethod，故跳过。

        // 最后，如果找不到，则返回baseType。
        if (result == null) {
            if (includeBaseType) {
                result = var.getUpperBounds().get(0).resolve(context, includeBaseType); // baseType.resolve()
            } else {
                result = var;
            }
        }

        return result;
    }

    /**
     * 查找非{@link BoundedTypeInfo}。
     * <p>
     * 例如：{@link TypeVariableInfo}：<code>&lt;A extends B&gt;</code>，
     * <code>&lt;B extends Number&gt;</code>，那么，查找<code>A</code>的结果为
     * <code>Number</code>。
     * </p>
     * <p>
     * 再如：{@link WildcardTypeInfo}：<code>&lt;? extends B&gt;</code>，
     * <code>&lt;B extends Number&gt;</code>，那么，查找<code>?</code>的结果为
     * <code>Number</code>。
     * </p>
     */
    static TypeInfo findNonBoundedType(TypeInfo type) {
        while (type instanceof BoundedTypeInfo) {
            type = ((BoundedTypeInfo) type).getBaseType();
        }

        return type;
    }

    /**
     * 取得类型中的方法。
     */
    static MethodInfo getMethod(ClassTypeInfo type, String methodName, Class<?>... paramTypes) {
        Class<?> rawType = assertNotNull(type, "type").getRawType();

        Method method = null;
        Exception notFound = null;

        // 先找public方法
        try {
            method = rawType.getMethod(methodName, paramTypes);
        } catch (java.lang.NoSuchMethodException e) {
            notFound = e;
        }

        // 再找protected/package/private方法
        if (method == null) {
            try {
                method = rawType.getDeclaredMethod(methodName, paramTypes);
            } catch (java.lang.NoSuchMethodException e) {
                notFound = e;
            }
        }

        if (method == null) {
            throw new MethodNotFoundException(notFound);
        }

        return factory.getMethod(method, type);
    }

    /**
     * 取得类型中的构造函数。
     */
    static MethodInfo getConstructor(ClassTypeInfo type, Class<?>... paramTypes) {
        Class<?> rawType = assertNotNull(type, "type").getRawType();

        Constructor<?> constructor = null;
        Exception notFound = null;

        try {
            constructor = rawType.getDeclaredConstructor(paramTypes);
        } catch (java.lang.NoSuchMethodException e) {
            notFound = e;
        }

        if (constructor == null) {
            throw new MethodNotFoundException(notFound);
        }

        return factory.getConstructor(constructor, type);
    }

    /**
     * 取得类型中的字段。
     */
    static FieldInfo getField(ClassTypeInfo type, ClassTypeInfo declaringType, String name) {
        Field field = null;

        try {
            field = declaringType.getRawType().getDeclaredField(name);
        } catch (Exception e) {
            throw new FieldNotFoundException(e);
        }

        return factory.getField(field, type);
    }
}
