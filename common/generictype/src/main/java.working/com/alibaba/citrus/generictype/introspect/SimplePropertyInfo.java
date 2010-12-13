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
package com.alibaba.citrus.generictype.introspect;

/**
 * 代表一个简单的property。常见的形式是： </p> *
 * 
 * <pre>
 * public String getName();
 * 
 * public void setName(String name);
 * </pre>
 * <p>
 * 以上这对方法定义了一个可读、可写的simple property，名字叫<code>name</code>，类型为 <code>String</code>
 * 。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface SimplePropertyInfo extends PropertyInfo {
    /**
     * 取得property的值。
     */
    Object getValue(Object object);

    /**
     * 设置property的值。
     */
    void setValue(Object object, Object value);
}
