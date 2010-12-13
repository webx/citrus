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

import java.util.HashMap;
import java.util.List;

import com.alibaba.citrus.generictype.introspect.PropertyUtil;

public class PropertyEvaluationTests {
    public static void main(String[] args) throws Exception {
        System.out.println(PropertyUtil.getProperty("test", "class.name", null));
        // Method method = Bean.class.getMethod("getList");
        // // TypeHierarchy h = typeInfo(Bean.class).getHierarchy();
        // TypeInfo type = typeInfo(method.getGenericReturnType());
        //
        // System.out.println(type.getComponentType());
    }

    abstract class Bean extends HashMap<String, Integer> {
        private static final long serialVersionUID = -1657102645555924688L;

        public List<String>[] getList() {
            return null;
        }
    }
}
