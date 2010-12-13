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
package com.alibaba.citrus.turbine.dataresolver.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.util.internal.ToStringBuilder;

abstract class AbstractDataResolver implements DataResolver {
    private final String desc;
    protected final DataResolverContext context;

    public AbstractDataResolver(String desc, DataResolverContext context) {
        this.desc = assertNotNull(trimToNull(desc), "desc");
        this.context = assertNotNull(context, "data resolver context");
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append(desc).start().append(context).end().toString();
    }
}
