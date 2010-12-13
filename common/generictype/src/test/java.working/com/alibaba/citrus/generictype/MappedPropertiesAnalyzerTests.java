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

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.generictype.introspect.PropertyInfo;

/**
 * 测试<code>MappedPropertiesAnalyzer</code>。
 * 
 * @author Michael Zhou
 */
public class MappedPropertiesAnalyzerTests extends AbstractPropertiesAnalyzerTests {
    /**
     * 测试不同的访问控制。
     */
    @Test
    public void accessible() {
        @SuppressWarnings("unused")
        class MyClass {
            private String getPrivateString(String key) {
                return null;
            }

            private void setPrivateString(String key, String s) {
            }

            String getPackageString(String key) {
                return null;
            }

            void setPackageString(String key, String s) {
            }

            protected String getProtectedString(String key) {
                return null;
            }

            protected void setProtectedString(String key, String s) {
            }

            public String getPublicString(String key) {
                return null;
            }

            public void setPublicString(String key, String s) {
            }
        }

        TypeIntrospectionInfo ci = getClassInfo(MyClass.class);
        Map<String, List<PropertyInfo>> props = ci.getProperties();

        assertNull(props.get("privateString"));
        assertNull(props.get("packageString"));
        assertNull(props.get("protectedString"));

        List<PropertyInfo> pubStrs = props.get("publicString");

        assertEquals(1, pubStrs.size());
        assertPropertyInfo(pubStrs.get(0), MyClass.class, "publicString", String.class, false,
                "getPublicString(Ljava/lang/String;)Ljava/lang/String;",
                "setPublicString(Ljava/lang/String;Ljava/lang/String;)V");
    }

    /**
     * 测试不同的方法形态。
     */
    @Test
    public void signatures() {
        @SuppressWarnings("unused")
        class MyClass {
            // get但没有返回值
            public void getNoReturn(String key) {
            }

            // 不支持is
            public String isNotBoolean(String key) {
                return null;
            }

            // get带参数
            public String getWithParams(String key, String s) {
                return null;
            }

            // set带有返回值
            public String setWithReturn(String key, int i) {
                return null;
            }

            // set带有双参数
            public void setWith2Params(String key, int i, long j) {
            }

            // 正常的set
            public void setNormal(String key, String s) {
            }

            // 正常的get
            public String getNormal(String key) {
                return null;
            }

            // 不支持is
            public boolean isNormal(String key) {
                return false;
            }

            // 大写的property
            public URL getURL(String key) {
                return null;
            }

            // 小写的property
            public URL getUrl(String key) {
                return null;
            }

            // 正常的set，但和Object.getClass类型不同
            public void setClass(String key, String s) {
            }

            // 不支持is
            public boolean isBoolean(String key) {
                return false;
            }

            // 正常的boolean get
            public boolean getBoolean(String key) {
                return false;
            }

            // generic property
            public <T> List<T> getList(String key) {
                return null;
            }

            // generic property
            public <T> void setList(String key, List<T> list) {
            }
        }

        TypeIntrospectionInfo ci = getClassInfo(MyClass.class);
        Map<String, List<PropertyInfo>> props = ci.getProperties();
        List<PropertyInfo> pis;

        assertNull(props.get("noReturn"));
        assertNull(props.get("notBoolean"));
        assertNull(props.get("withParams"));

        pis = props.get("withReturn");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "withReturn", int.class, false, null,
                "setWithReturn(Ljava/lang/String;I)Ljava/lang/String;");

        assertNull(props.get("with2Params"));

        pis = props.get("normal");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "normal", String.class, false,
                "getNormal(Ljava/lang/String;)Ljava/lang/String;", "setNormal(Ljava/lang/String;Ljava/lang/String;)V");

        pis = props.get("URL");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "URL", URL.class, false,
                "getURL(Ljava/lang/String;)Ljava/net/URL;", null);

        pis = props.get("url");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "url", URL.class, false,
                "getUrl(Ljava/lang/String;)Ljava/net/URL;", null);

        pis = props.get("class");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "class", String.class, false, null,
                "setClass(Ljava/lang/String;Ljava/lang/String;)V");

        pis = props.get("boolean");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "boolean", boolean.class, false,
                "getBoolean(Ljava/lang/String;)Z", null);

        pis = props.get("list");
        assertEquals(1, pis.size());
        assertPropertyInfo(pis.get(0), MyClass.class, "list", List.class, true,
                "getList(Ljava/lang/String;)Ljava/util/List;", "setList(Ljava/lang/String;Ljava/util/List;)V");
    }

    @Override
    protected ClassAnalyzer[] getAnalyzers() {
        return new ClassAnalyzer[] { new MappedPropertiesAnalyzer() };
    }
}
