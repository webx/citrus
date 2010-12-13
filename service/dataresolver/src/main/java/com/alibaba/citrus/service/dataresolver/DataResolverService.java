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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 用来取得<code>DataResolver</code>的服务。
 * <p>
 * 一个<code>DataResolver</code>可以取得指定类型或指定annotation所定义的数据。
 * <code>DataResolver</code>被用来将适当的数据注入到方法的参数、对象的property中。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface DataResolverService {
    /**
     * 取得指定generic类型、指定annotations的参数或property的数据解析器。
     */
    DataResolver getDataResolver(Type type, Annotation[] annotations, Object... extraInfo)
            throws DataResolverNotFoundException;

    /**
     * 取得指定方法的参数类型的数据解析器。
     */
    DataResolver[] getParameterResolvers(Method method, Object... extraInfo) throws DataResolverNotFoundException;
}
