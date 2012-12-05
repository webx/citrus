/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.support.SchemaSet;

/**
 * 创建一个schema set，其中包含以下所有的schemas：
 * <ul>
 * <li>所有的SpringExt扩展点、捐献的schemas。</li>
 * <li>所有的Spring原有的schemas。</li>
 * </ul>
 *
 * @author Michael Zhou
 */
public class SpringExtSchemaSet extends SchemaSet {
    /** 通过默认的<code>ClassLoader</code>来装载schemas。 */
    public SpringExtSchemaSet() {
        super(new ConfigurationPointsImpl(), new SpringPluggableSchemas());
    }

    /** 通过指定的<code>ClassLoader</code>来装载schemas。 */
    public SpringExtSchemaSet(ClassLoader classLoader) {
        super(new ConfigurationPointsImpl(classLoader), new SpringPluggableSchemas(classLoader));
    }

    /** 通过指定的<code>ResourceResolver</code>来装载schemas（IDE plugins mode）。 */
    public SpringExtSchemaSet(ResourceResolver resourceResolver) {
        super(new ConfigurationPointsImpl(resourceResolver), new SpringPluggableSchemas(resourceResolver));
    }

    public ConfigurationPoints getConfigurationPoints() {
        for (Schemas schemas : this) {
            if (schemas instanceof ConfigurationPoints) {
                return (ConfigurationPoints) schemas;
            }
        }

        unreachableCode("no ConfigurationPoints found");
        return null;
    }
}
