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
package com.alibaba.citrus.service.dataresolver;

/**
 * 创建数据解析器的工厂。
 * 
 * @author Michael Zhou
 */
public interface DataResolverFactory {
    /**
     * 取得指定generic类型、指定annotations的参数或property的数据解析器。
     * <p>
     * 假如当前factory不能接受指定的类型和annotation，则返回<code>null</code>，
     * <code>DataResolverService</code>会尝试下一个factory，直到找到合适的为止。
     * </p>
     */
    DataResolver getDataResolver(DataResolverContext context);
}
