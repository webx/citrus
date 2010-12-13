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
package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;

/**
 * 通过generic参数来取得beanInterface的bean基类。
 * <p>
 * 需要注意的是，解析generic参数有一定的性能开销，所以应该避免将此基类用于非singleton的对象。
 * </p>
 */
public class GenericBeanSupport<T> extends BeanSupport {
    @Override
    protected final Class<?> resolveBeanInterface() {
        return resolveParameter(getClass(), GenericBeanSupport.class, 0).getRawType();
    }
}
