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
import java.util.List;
import java.util.Map;

import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.introspect.Introspector;
import com.alibaba.citrus.generictype.introspect.PropertyInfo;

public class Main {
    public static void main(String[] args) throws Exception {
        TypeInfo listBType = TypeInfo.factory.getType(ClassB.class.getMethod("getList").getGenericReturnType());

        Introspector intro = Introspector.getInstance(listBType);
        Map<String, List<PropertyInfo>> props = intro.getProperties();
        System.out.println(props);
    }
}

class ClassA<A> {
}

class ClassB<B> extends ClassA<B> {
    public List<B> getList() {
        return null;
    }

    public Map<Object, B> getMap() {
        return null;
    }

    public int[] getInts() {
        return null;
    }

    public <A> List<A> getTest() {
        return null;
    }
}
