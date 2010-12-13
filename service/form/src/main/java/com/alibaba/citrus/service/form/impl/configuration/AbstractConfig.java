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
package com.alibaba.citrus.service.form.impl.configuration;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.springext.support.GenericBeanSupport;
import com.alibaba.citrus.util.StringUtil;

/**
 * 所有form配置对象的基类。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractConfig<T> extends GenericBeanSupport<T> {
    /**
     * 取得忽略大小写的名称。
     */
    protected String caseInsensitiveName(String name) {
        assertNotNull(name, "name");
        return StringUtil.toLowerCase(name);
    }
}
