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
package com.alibaba.citrus.generictype.demo.simple;

import static com.alibaba.citrus.generictype.TypeInfo.*;

import java.util.ArrayList;
import java.util.Map;

import com.alibaba.citrus.generictype.ClassTypeInfo;
import com.alibaba.citrus.generictype.MethodInfo;

public class GenericTest {
    public static void main(String[] args) throws Exception {
        // Class3
        ClassTypeInfo class3 = factory.getClassType(Class3.class);

        System.out.printf("Context class:                   %s\n", class3);

        // Class1.getArrayList()
        MethodInfo getArrayList = factory.getMethod(Class1.class.getMethod("getArrayList"));

        System.out.printf("Method:                          %s\n", getArrayList);

        // ArrayList<A>
        ClassTypeInfo returnType = (ClassTypeInfo) getArrayList.getReturnType();

        System.out.printf("Return type of method:           %s\n", returnType);

        // resolve ArrayList<A>
        ClassTypeInfo resolvedType = returnType.resolve(class3);

        System.out.printf("Resolved returnType:             %s\n", resolvedType);
        System.out.printf("Argument of resolved returnType: %s\n", resolvedType.getActualTypeArguments().get(0));
    }
}

interface Class1<A> {
    ArrayList<A> getArrayList();
}

abstract class Class2<B> implements Class1<Map<String, B>> {
}

abstract class Class3 extends Class2<Integer> {
}
