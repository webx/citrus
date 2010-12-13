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
package com.alibaba.citrus.expr;

/**
 * 代表表达式的上下文。
 * 
 * @author Michael Zhou
 */
public interface ExpressionContext {
    /**
     * 取得指定值。
     * 
     * @param key 键
     * @return 键对应的值
     */
    Object get(String key);

    /**
     * 添加一个值。
     * 
     * @param key 键
     * @param value 对应的值
     */
    void put(String key, Object value);
}
