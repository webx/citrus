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

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.generictype.introspect.PropertyInfo;

/**
 * ≤‚ ‘<code>ArrayAnalyzer</code>°£
 * 
 * @author Michael Zhou
 */
public class ArrayAnalyzerTests extends AbstractPropertiesAnalyzerTests {
    @Test
    public void array() {
        TypeIntrospectionInfo ci = getClassInfo(int[].class);
        Map<String, List<PropertyInfo>> props = ci.getProperties();

        List<PropertyInfo> pis = props.get(null);

        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), int[].class, null, int.class, false, (String[]) null, null);
    }

    @Test
    public void notArray() {
        TypeIntrospectionInfo ci = getClassInfo(String.class);
        Map<String, List<PropertyInfo>> props = ci.getProperties();

        assertNull(props.get(null));
    }

    @Override
    protected ClassAnalyzer[] getAnalyzers() {
        return new ClassAnalyzer[] { new ArrayAnalyzer() };
    }
}
